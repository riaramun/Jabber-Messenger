package ru.rian.riamessenger.riaevents.client;

import org.jivesoftware.smack.roster.Roster;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/8/2015.
 */
@AllArgsConstructor
public class RosterClientEvent {

    @Getter
    RiaEvent eventId;

    public enum RiaEvent {
        DB_UPDATED
    }
}
