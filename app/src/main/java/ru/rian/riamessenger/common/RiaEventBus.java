package ru.rian.riamessenger.common;

import de.greenrobot.event.EventBus;
import lombok.val;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;

/**
 * Created by Roman on 7/13/2015.
 */
public class RiaEventBus {

    public static void post(RiaServiceEvent.RiaEvent riaEvent) {
        EventBus.getDefault().post(new RiaServiceEvent(riaEvent));
    }

    public static void post(String exceptionMessage) {
        EventBus.getDefault().post(new XmppErrorEvent(exceptionMessage));
    }

    public static void post(XmppErrorEvent.State state) {
        XmppErrorEvent xmppEvent = new XmppErrorEvent(state);

        if (XmppErrorEvent.State.EDbUpdated == state
                || XmppErrorEvent.State.EDbUpdating == state
                || XmppErrorEvent.State.EAuthenticated == state
                || XmppErrorEvent.State.EAuthenticationFailed == state) {
            EventBus.getDefault().postSticky(xmppEvent);
        } else {
            EventBus.getDefault().post(xmppEvent);
        }

    }
}
