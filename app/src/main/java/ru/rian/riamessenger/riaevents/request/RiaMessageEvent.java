package ru.rian.riamessenger.riaevents.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Roman on 7/8/2015.
 */

@AllArgsConstructor
public class RiaMessageEvent {

    @Getter
    final String jid;

    @Getter
    final String message;
}
