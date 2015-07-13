package ru.rian.riamessenger.xmpp;

import android.text.TextUtils;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import lombok.Getter;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.di.D2EComponent;
import ru.rian.riamessenger.prefs.UserAppPreference;

/**
 * Created by Roman on 7/13/2015.
 */
public class RosterConnectingTimer extends Timer {

    TimerTask timerTask;
    public void schedule(TimerTask timerTask) {

        this.timerTask = timerTask;
        //this is work around the smack exception
        schedule(timerTask, 400, RiaConstants.CONNECTING_TIME_OUT);
        //end work around
    }
    public synchronized void cancel() {
        super.cancel();
        timerTask.cancel();
    }

}
