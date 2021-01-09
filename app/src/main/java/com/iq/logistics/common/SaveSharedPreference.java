package com.iq.logistics.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class SaveSharedPreference {
    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setSavedInternet(Context context, @Nullable String ipaddr, @Nullable int port) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(GlobalVariable.SAVED_IN_INFOSET, true);
        editor.putString(GlobalVariable.SAVED_IN_IP, ipaddr);
        editor.putInt(GlobalVariable.SAVED_IN_PORT, port);
        editor.clear();
        editor.apply();
    }

    public static void setSavedMac(Context context, @Nullable String macaddr) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(GlobalVariable.SAVED_IN_INFOSET, true);
        editor.putString(GlobalVariable.SAVED_IN_MAC, macaddr);
        editor.clear();
        editor.apply();
    }

    public static void removeSavedInfo(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(GlobalVariable.SAVED_IN_INFOSET, false);
        editor.remove(GlobalVariable.SAVED_IN_IP);
        editor.remove(GlobalVariable.SAVED_IN_PORT);
        editor.remove(GlobalVariable.SAVED_IN_MAC);

        editor.clear();
        editor.apply();
    }

    /**
     * Get the Login Status
     * @param context
     * @return boolean: login status
     */

    public static boolean getSavedStatus(Context context) {
        return getPreferences(context).getBoolean(GlobalVariable.SAVED_IN_INFOSET, false);
    }

    public static String getSavedIp(Context context) {
        return getPreferences(context).getString(GlobalVariable.SAVED_IN_IP, "192.168.2.6");
    }

    public static int getSavedPort(Context context) {
        return getPreferences(context).getInt(GlobalVariable.SAVED_IN_PORT, 62222);
    }

    public static String getSavedMac(Context context) {
        return getPreferences(context).getString(GlobalVariable.MAC_ADDRESS, null);
    }
}
