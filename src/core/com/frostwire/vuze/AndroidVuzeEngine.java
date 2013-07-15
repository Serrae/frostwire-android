/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.vuze;

import java.io.File;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.internat.MessageText;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.util.SystemUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class AndroidVuzeEngine extends VuzeEngine {

    private static final String AZUREUS_CONFIG_KEY_MAX_DOWNLOAD_SPEED = "Max Download Speed KBs";
    private static final String AZUREUS_CONFIG_KEY_MAX_UPLOAD_SPEED = "Max Upload Speed KBs";
    private static final String AZUREUS_CONFIG_KEY_MAX_DOWNLOADS = "max downloads";
    private static final String AZUREUS_CONFIG_KEY_MAX_UPLOADS = "Max Uploads";
    private static final String AZUREUS_CONFIG_KEY_MAX_TOTAL_CONNECTIONS = "Max.Peer.Connections.Total";
    private static final String AZUREUS_CONFIG_KEY_MAX_TORRENT_CONNECTIONS = "Max.Peer.Connections.Per.Torrent";

    private OnSharedPreferenceChangeListener preferenceListener;

    public AndroidVuzeEngine() {
        registerPreferencesChangeListener();
    }

    public void loadMessages(Context context) {

        final Object[][] messages = new Object[][] {// 
        new Object[] { "PeerManager.status.finished", context.getString(R.string.azureus_peer_manager_status_finished) }, //
                new Object[] { "PeerManager.status.finishedin", context.getString(R.string.azureus_peer_manager_status_finishedin) }, //
                new Object[] { "Formats.units.alot", context.getString(R.string.azureus_formats_units_alot) }, //
                new Object[] { "discarded", context.getString(R.string.azureus_discarded) }, //
                new Object[] { "ManagerItem.waiting", context.getString(R.string.azureus_manager_item_waiting) }, //
                new Object[] { "ManagerItem.initializing", context.getString(R.string.azureus_manager_item_initializing) }, //
                new Object[] { "ManagerItem.allocating", context.getString(R.string.azureus_manager_item_allocating) }, //
                new Object[] { "ManagerItem.checking", context.getString(R.string.azureus_manager_item_checking) }, //
                new Object[] { "ManagerItem.finishing", context.getString(R.string.azureus_manager_item_finishing) }, //
                new Object[] { "ManagerItem.ready", context.getString(R.string.azureus_manager_item_ready) }, //
                new Object[] { "ManagerItem.downloading", context.getString(R.string.azureus_manager_item_downloading) }, //
                new Object[] { "ManagerItem.seeding", context.getString(R.string.azureus_manager_item_seeding) }, //
                new Object[] { "ManagerItem.superseeding", context.getString(R.string.azureus_manager_item_superseeding) }, //
                new Object[] { "ManagerItem.stopping", context.getString(R.string.azureus_manager_item_stopping) }, //
                new Object[] { "ManagerItem.stopped", context.getString(R.string.azureus_manager_item_stopped) }, //
                new Object[] { "ManagerItem.paused", context.getString(R.string.azureus_manager_item_paused) }, //
                new Object[] { "ManagerItem.queued", context.getString(R.string.azureus_manager_item_queued) }, //
                new Object[] { "ManagerItem.error", context.getString(R.string.azureus_manager_item_error) }, //
                new Object[] { "ManagerItem.forced", context.getString(R.string.azureus_manager_item_forced) }, //
                new Object[] { "GeneralView.yes", context.getString(R.string.azureus_general_view_yes) }, //
                new Object[] { "GeneralView.no", context.getString(R.string.azureus_general_view_no) } //
        };

        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return messages;
            }
        };

        MessageText.integratePluginMessages(bundle);
    }

    @Override
    protected File getVuzePath() {
        return SystemUtils.getAzureusDirectory();
    }

    @Override
    protected File getTorrentsPath() {
        return SystemUtils.getTorrentsDirectory();
    }

    @Override
    protected void initConfiguration() {
        super.initConfiguration();

        // network parameters, fine tunning for android
        COConfigurationManager.setParameter("network.tcp.write.select.time", 1000);
        COConfigurationManager.setParameter("network.tcp.write.select.min.time", 1000);
        COConfigurationManager.setParameter("network.tcp.read.select.time", 1000);
        COConfigurationManager.setParameter("network.tcp.read.select.min.time", 1000);
        COConfigurationManager.setParameter("network.control.write.idle.time", 1000);
        COConfigurationManager.setParameter("network.control.read.idle.time", 1000);
    }

    private void registerPreferencesChangeListener() {
        preferenceListener = new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Constants.PREF_KEY_TORRENT_MAX_DOWNLOAD_SPEED)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_DOWNLOAD_SPEED);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_UPLOAD_SPEED)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_UPLOAD_SPEED);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_DOWNLOADS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_DOWNLOADS);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_UPLOADS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_UPLOADS);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_TOTAL_CONNECTIONS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_TOTAL_CONNECTIONS);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_TORRENT_CONNECTIONS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_TORRENT_CONNECTIONS);
                }
            }
        };
        ConfigurationManager.instance().registerOnPreferenceChange(preferenceListener);
    }

    private void setAzureusParameter(String key) {
        COConfigurationManager.setParameter(key, ConfigurationManager.instance().getLong(key));
        COConfigurationManager.save();
    }
}
