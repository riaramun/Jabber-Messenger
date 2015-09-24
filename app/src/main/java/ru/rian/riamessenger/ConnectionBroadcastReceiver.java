package ru.rian.riamessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.riaevents.connection.InternetConnEvent;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.SysUtils;

/**
 * Created by Roman on 8/4/2015.
 */
public class ConnectionBroadcastReceiver extends BroadcastReceiver {

    State state = State.EIdle;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvailable = NetworkStateManager.isNetworkAvailable(context);
        State state = isNetworkAvailable ? State.EConnected : State.EDisconnected;
        if (this.state != state) {
            EventBus.getDefault().post(new InternetConnEvent(isNetworkAvailable));
        }
        if (!SysUtils.isMyServiceRunning(RiaXmppService.class, context)) {
            context.startService(new Intent(context, RiaXmppService.class));
        }
        this.state = state;
    }

    enum State {
        EIdle,
        EConnected,
        EDisconnected
    }
}
