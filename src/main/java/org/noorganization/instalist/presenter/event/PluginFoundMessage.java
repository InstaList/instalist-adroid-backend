/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.presenter.event;

import android.app.Activity;

/**
 * Event-Message for notifying about found plugins.
 * Created by damihe on 06.01.16.
 */
public class PluginFoundMessage {
    public String mName;
    public String mPackage;
    public Class<?> mMainActivity;
    public Class<?> mSettingsActivity;


    public PluginFoundMessage(String _name, Class<Activity> _mainActivity,
                              Class<Activity> _settingsActivity, String _package) {
        mName = _name;
        mMainActivity = _mainActivity;
        mSettingsActivity = _settingsActivity;
        mPackage = _package;
    }

    public PluginFoundMessage(String _name, String _package) {
        mName = _name;
        mPackage = _package;
    }
}
