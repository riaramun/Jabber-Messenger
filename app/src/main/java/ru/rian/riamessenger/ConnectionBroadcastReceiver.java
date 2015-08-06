package ru.rian.riamessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.riaevents.connection.InternetConnEvent;
import ru.rian.riamessenger.utils.NetworkStateManager;

/**
 * Created by Roman on 8/4/2015.
 */
public class ConnectionBroadcastReceiver extends BroadcastReceiver {

    enum State {
        EIdle,
        EConnected,
        EDisconnected
    }
    State state = State.EIdle;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvailable = NetworkStateManager.isNetworkAvailable(context);
        State state = isNetworkAvailable ? State.EConnected : State.EDisconnected;
        if (this.state != state) {
            EventBus.getDefault().post(new InternetConnEvent(isNetworkAvailable));
        }
        this.state = state;
    }
}
