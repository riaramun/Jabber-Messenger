package ru.rian.riamessenger.riaevents.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Roman on 7/30/2015.
 */

@RequiredArgsConstructor
public class ChatEvents {

    @Getter
    final int chatEventId;

    @Getter
    final String chatThreadId;

    static public final int SHOW_REMOVE_DIALOG = 1;
    static public final int DO_REMOVE_CHAT = 2;
}
