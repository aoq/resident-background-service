/*
 * Copyright (c) 2015 Yu AOKI
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.aokyu.service.receiver;

import com.aokyu.service.ResidentService;
import com.aokyu.service.setting.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        Settings settings = Settings.getInstance(context);
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) && settings.isServiceEnabled()) {
            ResidentService.start(context);
        }
    }
}
