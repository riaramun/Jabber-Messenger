package ru.rian.riamessenger.riaevents.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/30/2015.
 */

@AllArgsConstructor
public class RoomEditEvent {

    @Getter
    final String roomThreadId;

    @Getter
    final String userJid;

    @Getter
    final int command;

    static public final int INVITE_USER = 1;
    static public final int KICK_USER = 2;
}
