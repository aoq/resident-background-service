/*
 * Copyright (c) 2015 Yu AOKI
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.aokyu.service.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * The wrapper for the default {@link android.content.SharedPreferences}.
 */
public class Settings {

    private class Keys {

        private Keys() {}

        public static final String SERVICE_ENABLED = "service_enabled";
    }

    private static volatile Settings sSettings = null;

    private SharedPreferences mPreferences;

    public static Settings getInstance(Context context) {
        if (sSettings == null) {
            synchronized (Settings.class) {
                sSettings = new Settings(context);
            }
        }
        return sSettings;
    }

    private Settings(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Sets the flag indicating whether the service is enabled.
     * @param enabled true if the service is enabled.
     */
    public void setServiceEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(Keys.SERVICE_ENABLED, enabled)
                .commit();
    }

    /**
     * Determines whether the service should be enabled.
     * @return true if the service should be enabled.
     */
    public boolean isServiceEnabled() {
        return mPreferences.getBoolean(Keys.SERVICE_ENABLED, false);
    }
}
