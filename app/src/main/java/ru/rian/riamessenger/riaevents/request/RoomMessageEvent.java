package ru.rian.riamessenger.riaevents.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/8/2015.
 */

@AllArgsConstructor
public class RoomMessageEvent {

    @Getter
    final String roomJid;

    @Getter
    final String message;
}
