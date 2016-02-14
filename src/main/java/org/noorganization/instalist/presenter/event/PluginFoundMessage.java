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
