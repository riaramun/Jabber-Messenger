package ru.rian.riamessenger.xmpp;

import java.util.Timer;
import java.util.TimerTask;

import ru.rian.riamessenger.common.RiaConstants;

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
