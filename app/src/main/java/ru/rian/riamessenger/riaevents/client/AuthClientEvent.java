package ru.rian.riamessenger.riaevents.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/8/2015.
 */
@AllArgsConstructor
public class AuthClientEvent {
    @Getter
    final boolean isConnected;
    @Getter
    final boolean isAuth;
}
