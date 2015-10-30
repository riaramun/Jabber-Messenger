package ru.rian.riamessenger.riaevents.response;

import lombok.Getter;

/**
 * Created by Roman on 7/12/2015.
 */

public class XmppErrorEvent {

    public XmppErrorEvent(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public XmppErrorEvent(State state) {
        this.state = state;
    }

    @Getter
    public String exceptionMessage;

    @Getter
    public State state = State.Empty;

    public enum State {
        Empty,
        EConnecting,
        ENotConnecting,
        EConnected,
        EReconnected,
        EReconnectionFailed,
        EConnectionClosed,
        EAuthenticated,
        EAuthenticationFailed,
        EDbUpdating,
        EDbUpdated,
        EMessageReceived,
        EMessageNotSend
    }
}
