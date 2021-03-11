package missionclient;

import core.models.Prestation;

import java.util.List;

public class SetPrestations {

    public final List<String> oldPrestations;
    public final List<Prestation> newPrestations;

    public SetPrestations(List<String> oldPrestations, List<Prestation> newPrestations) {
        this.oldPrestations = oldPrestations;
        this.newPrestations = newPrestations;
    }

}
