package ru.rian.riamessenger.di;


import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.xmpp.RiaRosterListener;
import ru.rian.riamessenger.xmpp.SendMsgBroadcastReceiver;

/**
 * This class is in debug/ folder. You can use it to define injects or getters for dependencies only in debug variant
 */

public interface XmppServiceGraph {


    void inject(RiaRosterListener listener);

    void inject(RiaXmppService xmppConnectionService);

    void inject(SendMsgBroadcastReceiver sendMsgBroadcastReceiver);

}
