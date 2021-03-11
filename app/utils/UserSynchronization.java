package utils;

import agencies.AgenciesService;
import agencies.Agency;
import com.typesafe.config.Config;
import com.unboundid.ldap.sdk.*;
import core.CompletableFutureUtils;
import models.OfficeDao;
import office.OfficeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import users.User;
import users.UserWithGroups;
import users.UsersService;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class UserSynchronization {

    protected static final Logger logger = LoggerFactory.getLogger(UserSynchronization.class);

    private final UsersService usersService;
    private final AgenciesService agenciesService;
    private final OfficeService officeService;
    private final Optional<String> host;
    private final Optional<Integer> port;
    private final Optional<String> bind;
    private final Optional<String> password;
    private final Optional<String> applicativeBase;
    private final Optional<String> userBase;

    private Optional<String> getPhoneNumber(Entry entry) {
        if (entry.hasAttribute("mobile")) {
            return Optional.of(entry.getAttributeValue("mobile"));
        } else if (entry.hasAttribute("telephoneNumber")) {
            return Optional.of(entry.getAttributeValue("telephoneNumber"));
        } else {
            return Optional.empty();
        }
    }

    private CompletionStage<Boolean> remove(List<String> ldapUsers) {
        return CompletableFuture.completedFuture(ldapUsers.stream().map(user -> usersService.delete("", user).toCompletableFuture()).collect(Collectors.toList()))
                .thenApply(list -> true);
    }

    private CompletionStage<Boolean> handleGroups(List<String> cnsToAdd, List<String> cnsToRemove) {
        return CompletableFutureUtils.sequence(cnsToAdd.stream().map(cn -> {
            String group = cn.split(" - ")[2];
            return usersService.addGroupIfNotExists(group).toCompletableFuture();
        }).collect(Collectors.toList())).thenCompose(added -> CompletableFutureUtils.sequence(cnsToRemove.stream().map(cn -> {
            String group = cn.split(" - ")[2];
            return usersService.deleteGroup(group).toCompletableFuture();
        }).collect(Collectors.toList())))
                .thenApply(list -> list.stream().allMatch(r -> r));
    }

    private CompletionStage<Boolean> handleUserGroups(String userLogin, String userDn, List<Entry> groups) {
        List<String> userGroups = groups.stream().filter(group -> Arrays.asList(group.getAttributeValues("member")).contains(userDn)).map(group -> group.getAttributeValue("cn").split(" - ")[2]).collect(Collectors.toList());
        return usersService.deleteGroupsForUser(userLogin).thenCompose(done -> {
            if (done) {
                return usersService.setGroupsForUser(userLogin, userGroups);
            } else {
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    @Inject
    public UserSynchronization(final Config config, final UsersService usersService, final AgenciesService agenciesService, final OfficeService officeService) {
        this.usersService = usersService;
        this.agenciesService = agenciesService;
        this.officeService = officeService;
        if (config.hasPath("ldap.server") && config.hasPath("ldap.port") && config.hasPath("ldap.bind") && config.hasPath("ldap.password") && config.hasPath("ldap.applicativebase") && config.hasPath("ldap.userbase")) {
            this.host = Optional.of(config.getString("ldap.server"));
            this.port = Optional.of(config.getInt("ldap.port"));
            this.bind = Optional.of(config.getString("ldap.bind"));
            this.password = Optional.of(config.getString("ldap.password"));
            this.applicativeBase = Optional.of(config.getString("ldap.applicativebase"));
            this.userBase = Optional.of(config.getString("ldap.userbase"));
        } else {
            this.host = Optional.empty();
            this.port = Optional.empty();
            this.bind = Optional.empty();
            this.password = Optional.empty();
            this.applicativeBase = Optional.empty();
            this.userBase = Optional.empty();
        }
    }

    public CompletionStage<Boolean> synchronizeUser() {
        try {
            if (this.host.isPresent() && this.port.isPresent() && this.bind.isPresent() && this.password.isPresent() && this.applicativeBase.isPresent() && this.userBase.isPresent()) {
                logger.info("Starting user synchronization");
                try {
                    LDAPConnection connection = new LDAPConnection(this.host.get(), this.port.get());
                    connection.bind(this.bind.get(), this.password.get());
                    LDAPConnectionPool pool = new LDAPConnectionPool(connection, 1);
                    Filter groupFilter = Filter.create("(objectClass=group)");
                    SearchRequest groupRequest = new SearchRequest(this.applicativeBase.get(), SearchScope.SUB, groupFilter, SearchRequest.ALL_USER_ATTRIBUTES);
                    if (pool.getConnection().isConnected()) {
                        SearchResult groups = pool.getConnection().search(groupRequest);
                        List<Entry> filteredGroups = groups.getSearchEntries().stream().filter(e -> e.hasAttribute("cn") && e.getAttribute("cn").getValue().contains("APP - CALYPSO")).collect(Collectors.toList());

                        List<Entry> entriesGroupToAdd = filteredGroups.stream().filter(e -> e.hasAttribute("member")).collect(Collectors.toList());

                        List<String> groupsToAdd = entriesGroupToAdd.stream().map(e -> e.getAttributeValue("cn")).collect(Collectors.toList());
                        List<String> groupsToRemove = filteredGroups.stream().filter(e -> !e.hasAttribute("member")).map(e -> e.getAttributeValue("cn")).collect(Collectors.toList());
                        List<String> allMembers = new ArrayList<>();
                        for (Entry entry : entriesGroupToAdd) {
                            for (String member : entry.getAttributeValues("member")) {
                                if (!allMembers.contains(member)) {
                                    allMembers.add(member);
                                }
                            }
                        }
                        return handleGroups(groupsToAdd, groupsToRemove).thenCompose(ok -> {
                            try {
                                Filter userFilter = Filter.create("(objectClass=user)");
                                SearchRequest userRequest = new SearchRequest(this.userBase.get(), SearchScope.SUB, userFilter, SearchRequest.ALL_USER_ATTRIBUTES);
                                if (pool.getConnection().isConnected()) {
                                    SearchResult users = pool.getConnection().search(userRequest);
                                    List<Entry> filteredUsers = users.getSearchEntries().stream().filter(e -> e.hasAttribute("userPrincipalName") && e.hasAttribute("givenName") && e.hasAttribute("sn") && e.hasAttribute("memberOf") && allMembers.contains(e.getDN())).collect(Collectors.toList());
                                    List<String> usersLogin = filteredUsers.stream().map(entry -> entry.getAttributeValue("userPrincipalName")).collect(Collectors.toList());
                                    return usersService.getUserWithGroups("").thenCompose(usersInDatabase -> {
                                        List<String> usersNotMembers = usersInDatabase.stream().filter(user -> !usersLogin.contains(user.username)).map(user -> user.username).collect(Collectors.toList());
                                        return remove(usersNotMembers).thenCompose(done -> {
                                            if (done) {
                                                return this.agenciesService.getAll("ADX").thenCompose(agencies -> {
                                                    List<Agency> listAgencies = new ArrayList<>(agencies);

                                                    return CompletableFutureUtils.sequence(filteredUsers.stream().map(entry -> {
                                                        String login = entry.getAttributeValue("userPrincipalName");
                                                        Optional<String> registration_number = Optional.ofNullable(entry.getAttributeValue("pager"));
                                                        String first_name = entry.getAttributeValue("givenName");
                                                        String last_name = entry.getAttributeValue("sn");
                                                        Optional<String> office = Optional.ofNullable(entry.getAttributeValue("physicalDeliveryOfficeName"));
                                                        final Optional<String> phone = getPhoneNumber(entry);
                                                        Optional<String> description = Optional.ofNullable(entry.getAttributeValue("description"));
                                                        Optional<UserWithGroups> userWithGroup = usersInDatabase.stream().filter(u -> u.username.equals(login)).findAny();

                                                        try {
                                                            if (office.isPresent()) {
                                                                OfficeDao officeDao = this.officeService.getOneByName(office.get()).toCompletableFuture().get();
                                                                if (officeDao == null) {
                                                                    if (listAgencies.size() == 0) {
                                                                        Agency defaultAgency = this.getDefaultAgency();
                                                                        this.agenciesService.add("ADX", defaultAgency).toCompletableFuture().get();
                                                                        listAgencies.add(defaultAgency);
                                                                    }
                                                                    this.createOffice(office.get(), listAgencies.get(0));
                                                                }
                                                            }

                                                            if (!userWithGroup.isPresent()) {
                                                                return this.createUser(login, registration_number, first_name, last_name, office, phone, description, entry.getDN(), entriesGroupToAdd);
                                                            } else {
                                                                return this.updateUser(login, entry.getDN(), entriesGroupToAdd);
                                                            }
                                                        } catch (InterruptedException | ExecutionException e) {
                                                            logger.error("Error create users.");
                                                            return CompletableFuture.completedFuture(false);
                                                        }
                                                    }).collect(Collectors.toList())).thenApply(list -> list.stream().allMatch(t -> t));
                                                });
                                            } else {
                                                logger.error("Error removing users.");
                                                return CompletableFuture.completedFuture(false);
                                            }
                                        });
                                    });
                                } else {
                                    logger.error("No connection to ldap to get users");
                                    return CompletableFuture.completedFuture(false);
                                }
                            } catch (LDAPException e) {
                                logger.error("Failed to get users from LDAP", e);
                                return CompletableFuture.completedFuture(false);
                            }
                        });
                    } else {
                        logger.error("Cannot connect to LDAP.");
                        return CompletableFuture.completedFuture(false);
                    }
                } catch (LDAPException e) {
                    logger.error("Failed to get groups from LDAP", e);
                    return CompletableFuture.completedFuture(false);
                }
            } else {
                logger.error("Missing config to synchronize user.");
                return CompletableFuture.completedFuture(false);
            }
        } catch (Exception e) {
            logger.error("Failed to synchronize user", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private Agency getDefaultAgency() {
        String name = "ANJOU";
        String code = "A27";
        String iban = "FR7630047142930008031670116";
        String bic = "CMCIFRPP";
        String manager = "jbalavoine@allodiag.fr";

        return new Agency(Optional.empty(), code, name, manager, new Date(), iban, bic);
    }

    private void createOffice(String officeName, Agency agency) {
        OfficeDao office = new OfficeDao();
        office.setName(officeName);
        office.setAgency(agency.getId());

        try {
            this.officeService.add(office).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Problem when adding office " + officeName);
        }
    }

    private CompletableFuture<Boolean> createUser(String login, Optional<String> registration_number, String first_name, String last_name, Optional<String> office, Optional<String> phone, Optional<String> description, String dn, List<Entry> entriesGroupToAdd) {
        return usersService.add("", new User(login, registration_number, first_name, last_name, office, phone, description)).thenCompose(id -> {
            if (id.isPresent()) {
                return handleUserGroups(login, dn, entriesGroupToAdd);
            } else {
                logger.error("Problem when adding user " + login);
                return CompletableFuture.completedFuture(false);
            }
        }).toCompletableFuture();
    }

    private CompletableFuture<Boolean> updateUser(String login, String dn, List<Entry> entriesGroupToAdd) {
        return usersService.setActive(login).thenCompose(active -> handleUserGroups(login, dn, entriesGroupToAdd)).toCompletableFuture();
    }

}
