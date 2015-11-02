package ru.rian.riamessenger.utils;

import android.text.TextUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import ru.rian.riamessenger.common.RiaConstants;

/**
 * Created by grigoriy on 26.06.15.
 */
public class XmppUtils {

    static public void changeCurrentUserStatus(Presence presence, String jid, XMPPConnection xmppConnection) {
        if(!TextUtils.isEmpty(jid)) {
            try {
                presence.setFrom(JidCreate.from(jid));
                if (NetworkStateManager.setCurrentUserPresence(presence, jid)) {
                    changeCurrentUserPresenceOnServer(presence, xmppConnection);
                }
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
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

    public static String roomNameFromJid(String jidWithRes) {
        int atInd = jidWithRes.indexOf('@');
        return atInd > 0 ? jidWithRes.substring(0, atInd) : jidWithRes;
    }

    public static String entityJid(String jidWithRes) {
        int slashInd = jidWithRes.indexOf('/');
        return slashInd > 0 ? jidWithRes.substring(0, slashInd) : jidWithRes;
    }
}
