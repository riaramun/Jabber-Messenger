package ru.rian.riamessenger.riaevents.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/8/2015.
 */

@AllArgsConstructor
public class RiaServiceEvent {

    @Getter
    RiaEvent eventId;

    public enum RiaEvent {
        SIGN_IN,
        SIGN_OUT,
        GET_ROSTER
    }
}
