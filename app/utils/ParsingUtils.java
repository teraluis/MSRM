package utils;

import addresses.Address;
import api.v1.forms.AddAddressForm;
import api.v1.forms.AddPeopleForm;
import people.People;
import scala.Tuple4;

import java.util.Date;
import java.util.Optional;

public class ParsingUtils {

    public static Address parseAddress(AddAddressForm form) {
        return new Address(
                Optional.ofNullable(form.getUuid()),
                form.getType(),
                Optional.ofNullable(form.getAddress1()),
                Optional.ofNullable(form.getAddress2()),
                Optional.ofNullable(form.getPostCode()),
                Optional.ofNullable(form.getCity()),
                Optional.ofNullable(form.getGpsCoordinates()),
                Optional.ofNullable(form.getInseeCoordinates()),
                Optional.ofNullable(form.getDispatch()),
                Optional.ofNullable(form.getStaircase()),
                Optional.ofNullable(form.getWayType()),
                Optional.ofNullable(form.getCountry()),
                Optional.ofNullable(form.getCreated()).isPresent() ? new Date(form.getCreated()) : new Date());
    }

    public static People parsePeople(AddPeopleForm form) {
        return new People(
                Optional.ofNullable(form.getUuid()),
                form.getTitle(),
                form.getLastname(),
                form.getFirstname(),
                Optional.ofNullable(form.getWorkMail()),
                Optional.ofNullable(form.getEmail()),
                Optional.ofNullable(form.getWorkPhone()),
                Optional.ofNullable(form.getMobilePhone()),
                Optional.ofNullable(form.getJobDescription())
        );
    }
}
