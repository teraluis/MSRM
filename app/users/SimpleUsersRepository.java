package users;

import core.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleUsersRepository extends BaseRepository<User, String> implements UsersRepository {

    protected final Database database;

    protected static final Logger logger = LoggerFactory.getLogger(SimpleUsersRepository.class);

    private Boolean addGroup(String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"groups\"(\"name\") values(?)");
                ps.setString(1, name);
                return ps.executeUpdate() == 1;
            } catch (SQLException e) {
                logger.error("Failed to add group " + name + ".", e);
                return false;
            }
        });
    }

    private Boolean addGroupUser(String name, String group) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"groupusers\"(\"user\", \"group\") values(?, ?)");
                ps.setString(1, name);
                ps.setString(2, group);
                return ps.executeUpdate() == 1;
            } catch (SQLException e) {
                logger.error("Failed to add user group for user " + name + " and group " + group + ".", e);
                return false;
            }
        });
    }

    @Inject
    SimpleUsersRepository(@NamedDatabase("crm") Database db) {
        super(db, logger);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(Connection transaction, String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"users\"");

        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(Connection transaction, String organization, Integer offset, Integer length) throws SQLException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected User parseOneItem(ResultSet result, String organization) throws SQLException {
        final String login = result.getString("login");
        final Optional<String> registration_number = Optional.ofNullable(result.getString("registration_number"));
        final String first_name = result.getString("first_name");
        final String last_name = result.getString("last_name");
        final Optional<String> office = Optional.ofNullable(result.getString("office"));
        final Optional<String> phone = Optional.ofNullable(result.getString("phone"));
        final Optional<String> description = Optional.ofNullable(result.getString("description"));

        return new User(login, registration_number, first_name, last_name, office, phone, description);
    }

    @Override
    protected PreparedStatement buildGetRequest(Connection transaction, String organization, String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"users\" WHERE login=?");
        ps.setString(1, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(Connection transaction, String organization, String query) throws SQLException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(Connection transaction, String organization, String query, Integer offset, Integer length) throws SQLException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected PreparedStatement buildAddRequest(Connection transaction, String organization, User item) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"users\"(login, registration_number, first_name, last_name, office, phone, description) values(?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, item.login);
        ps.setString(2, item.registration_number.orElse(null));
        ps.setString(3, item.first_name);
        ps.setString(4, item.last_name);
        ps.setString(5, item.office.orElse(null));
        ps.setString(6, item.phone.orElse(null));
        ps.setString(7, item.description.orElse(null));
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(Connection transaction, String organization, String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"users\" set deleted = TRUE where login = ?");
        ps.setString(1, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(Connection transaction, String organization, User item) throws SQLException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Boolean addGroupIfNotExists(String group) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"groups\" WHERE \"name\" = ?");
                ps.setString(1, group);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return true;
                } else {
                    return addGroup(group);
                }
            } catch (SQLException e) {
                logger.error("Failed to add group if not exist " + group + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean deleteGroupsForUser(String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"groupusers\" WHERE \"user\" = ?");
                ps.setString(1, name);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to set groups for user " + name + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean deleteGroup(String groupName) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"groupusers\" WHERE \"group\" = ?");
                ps.setString(1, groupName);
                ps.execute();
                final PreparedStatement ps2 = transaction.prepareStatement("DELETE FROM \"groups\" WHERE \"name\" = ?");
                ps2.setString(1, groupName);
                ps2.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to delete group " + groupName + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean setGroupsForUser(String name, List<String> groups) {
        Integer count = 0;
        for (String group : groups) {
            Boolean res = addGroupUser(name, group);
            if (res) {
                count = count + 1;
            }
        }
        return count == groups.size();
    }

    @Override
    public Optional<User> getFromRegistrationNumber(String organization, String registration_number) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"users\" WHERE registration_number = ? AND deleted = FALSE;");
                ps.setString(1, registration_number);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed get user from registration number " + registration_number, e);
                return Optional.empty();
            }
        });
    }

    @Override
    public List<UserWithGroups> getUserWithGroups(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"groupusers\"");
                final ResultSet result = ps.executeQuery();
                List<UserWithGroups> usersWithGroups = new ArrayList<>();
                while (result.next()) {
                    String username = result.getString("user");
                    String group = result.getString("group");
                    Optional<UserWithGroups> userWithGroup = usersWithGroups.stream().filter(u -> u.username.equals(username)).findFirst();
                    if (userWithGroup.isPresent()) {
                        userWithGroup.get().groups.add(group);
                    } else {
                        List<String> groups = new ArrayList<>();
                        groups.add(group);
                        usersWithGroups.add(new UserWithGroups(username, groups));
                    }
                }
                List<User> allUsers = getAll(organization);
                for (User user : allUsers) {
                    Optional<UserWithGroups> userWithGroup = usersWithGroups.stream().filter(u -> u.username.equals(user.login)).findFirst();
                    if (!userWithGroup.isPresent()) {
                        usersWithGroups.add(new UserWithGroups(user.login, new ArrayList<>()));
                    }
                }
                return usersWithGroups;
            } catch (SQLException e) {
                logger.error("Failed get user groups.", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Boolean setActive(String user) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"users\" SET deleted = FALSE where login = ?");
                ps.setString(1, user);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed set user active.", e);
                return false;
            }
        });
    }

    @Override
    public List<User> searchByFirstNameAndLastName(String organization, String value) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"users\" WHERE CONCAT(LOWER(\"first_name\"), ' ',LOWER(\"last_name\")) like LOWER(?) LIMIT 5");
                ps.setString(1, "%" + value + "%");
                final ResultSet result = ps.executeQuery();
                List<User> users = new ArrayList<>();
                while (result.next()) {
                    users.add(parseOneItem(result, organization));
                }
                return users;
            } catch (SQLException e) {
                logger.error("Failed get users.", e);
                return new ArrayList<>();
            }
        });
    }
}
