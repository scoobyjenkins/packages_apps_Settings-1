/*
 * Copyright (C) 2016 GZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.settings.slim.HardwareKeysSettings;
import com.android.settings.slim.NavigationSettings;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        /* Restore the hardware tunable values */
        HardwareKeysSettings.restore(ctx);
    }
}
