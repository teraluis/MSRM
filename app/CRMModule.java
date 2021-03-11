import accounts.AccountsRepository;
import accounts.AccountsService;
import accounts.SimpleAccountsRepository;
import accounts.SimpleAccountsService;
import activities.ActivitiesRepository;
import activities.ActivitiesService;
import activities.SimpleActivitiesRepository;
import activities.SimpleActivitiesService;
import addresses.AddressesRepository;
import addresses.AddressesService;
import addresses.SimpleAddressesRepository;
import addresses.SimpleAddressesService;
import agencies.AgenciesRepository;
import agencies.AgenciesService;
import agencies.SimpleAgenciesRepository;
import agencies.SimpleAgenciesService;
import attachments.AttachmentsRepository;
import attachments.AttachmentsService;
import bills.BillsRepository;
import bills.BillsService;
import bills.SimpleBillsRepository;
import bills.SimpleBillsService;
import core.search.SearchService;
import entities.EntitiesRepository;
import entities.EntitiesService;
import entities.SimpleEntitiesRepository;
import entities.SimpleEntitiesService;
import establishments.EstablishmentsRepository;
import establishments.EstablishmentsService;
import establishments.SimpleEstablishmentsRepository;
import establishments.SimpleEstablishmentsService;
import estimates.EstimatesRepository;
import estimates.EstimatesService;
import estimates.SimpleEstimatesRepository;
import estimates.SimpleEstimatesService;
import groups.GroupsRepository;
import groups.GroupsService;
import groups.SimpleGroupsRepository;
import groups.SimpleGroupsService;
import markets.MarketsRepository;
import markets.MarketsService;
import markets.SimpleMarketsRepository;
import markets.SimpleMarketsService;
import office.OfficeRepository;
import office.OfficeService;
import orders.OrdersRepository;
import orders.OrdersService;
import orders.SimpleOrdersRepository;
import orders.SimpleOrdersService;
import people.PeopleRepository;
import people.PeopleService;
import people.SimplePeopleRepository;
import people.SimplePeopleService;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import scala.collection.Seq;
import services.FileService;
import users.SimpleUsersRepository;
import users.SimpleUsersService;
import users.UsersRepository;
import users.UsersService;
import utils.Scheduler;

public class CRMModule extends play.api.inject.Module {

    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(MarketsService.class).to(SimpleMarketsService.class),
                bind(MarketsRepository.class).to(SimpleMarketsRepository.class).eagerly(),
                bind(PeopleService.class).to(SimplePeopleService.class),
                bind(PeopleRepository.class).to(SimplePeopleRepository.class).eagerly(),
                bind(OrdersService.class).to(SimpleOrdersService.class),
                bind(OrdersRepository.class).to(SimpleOrdersRepository.class).eagerly(),
                bind(EstimatesService.class).to(SimpleEstimatesService.class),
                bind(EstimatesRepository.class).to(SimpleEstimatesRepository.class).eagerly(),
                bind(PeopleRepository.class).to(SimplePeopleRepository.class).eagerly(),
                bind(BillsService.class).to(SimpleBillsService.class),
                bind(BillsRepository.class).to(SimpleBillsRepository.class).eagerly(),
                bind(UsersService.class).to(SimpleUsersService.class),
                bind(UsersRepository.class).to(SimpleUsersRepository.class).eagerly(),
                bind(GroupsService.class).to(SimpleGroupsService.class),
                bind(GroupsRepository.class).to(SimpleGroupsRepository.class),
                bind(AccountsService.class).to(SimpleAccountsService.class),
                bind(AccountsRepository.class).to(SimpleAccountsRepository.class),
                bind(ActivitiesService.class).to(SimpleActivitiesService.class),
                bind(ActivitiesRepository.class).to(SimpleActivitiesRepository.class),
                bind(AddressesService.class).to(SimpleAddressesService.class),
                bind(AddressesRepository.class).to(SimpleAddressesRepository.class),
                bind(EntitiesService.class).to(SimpleEntitiesService.class),
                bind(EntitiesRepository.class).to(SimpleEntitiesRepository.class),
                bind(EstablishmentsService.class).to(SimpleEstablishmentsService.class),
                bind(EstablishmentsRepository.class).to(SimpleEstablishmentsRepository.class),
                bind(AgenciesRepository.class).to(SimpleAgenciesRepository.class),
                bind(AgenciesService.class).to(SimpleAgenciesService.class),
                bind(SearchService.class).toSelf(),
                bind(AttachmentsService.class).toSelf(),
                bind(AttachmentsRepository.class).toSelf(),
                bind(OfficeService.class).toSelf(),
                bind(OfficeRepository.class).toSelf(),
                bind(FileService.class).toSelf(),
                /* Application scheduler */
                bind(Scheduler.class).toSelf().eagerly()
        );
    }
}
