package org.secuso.privacyfriendlytodolist.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Objects;

public class PinUtil {
    public static boolean hasPin(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        // pin activated and valid?
        return pref.getBoolean("pref_pin_enabled", false)
                && pref.getString("pref_pin", null) != null
                && Objects.requireNonNull(pref.getString("pref_pin", "")).length() >= 4;
    }
}