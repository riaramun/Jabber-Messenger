package ru.rian.riamessenger.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.xmpp.SmackXmppConnection;

/**
 * Created by grigoriy on 26.06.15.
 */
public class XmppUtils {

	static public void changeCurrentUserStatus(Presence presence, String jid, XMPPConnection xmppConnection ) {
		NetworkStateManager.setCurrentUserPresence(presence, jid);
		changeCurrentUserPresenceOnServer(presence, xmppConnection);
	}

	static void changeCurrentUserPresenceOnServer(Presence presence, XMPPConnection xmppConnection) {
		if (xmppConnection != null) {
			try {
				xmppConnection.sendStanza(presence);
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
