package ru.rian.riamessenger.riaevents.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Roman on 7/8/2015.
 */

@AllArgsConstructor
public class RiaServiceEvent {

    @Getter
    RiaEvent eventId;

    public enum RiaEvent {
        TO_SIGN_IN,
        TO_SIGN_OUT,
        TO_GET_ROSTER
    }
}
