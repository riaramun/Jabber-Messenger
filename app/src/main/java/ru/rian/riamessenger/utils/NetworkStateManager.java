package ru.rian.riamessenger.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jivesoftware.smack.packet.Presence;

import ru.rian.riamessenger.model.RosterEntryModel;

public class NetworkStateManager {

    public static boolean isNetworkAvailable(Context context) {
        boolean isMobile = false, isWifi = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infoAvailableNetworks = connectivityManager.getAllNetworkInfo();
        if (infoAvailableNetworks != null) {
            for (NetworkInfo network : infoAvailableNetworks) {
                if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (network.isConnected() && network.isAvailable())
                        isWifi = true;
                }
                if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (network.isConnected() && network.isAvailable())
                        isMobile = true;
                }
            }
        }
        return isMobile || isWifi;
    }

    public static void setCurrentUserPresence(Presence presence, String bareJid) {
        RosterEntryModel rosterEntryModel = new RosterEntryModel();
        rosterEntryModel.bareJid = bareJid;
        rosterEntryModel.setPresence(presence);
        rosterEntryModel.save();
    }
}
