package ru.rian.riamessenger.riaevents.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 8/4/2015.
 */
@AllArgsConstructor
public class InternetConnEvent {
    @Getter
    final boolean isConnectionAvailable;
}
