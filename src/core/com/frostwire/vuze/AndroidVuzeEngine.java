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

import org.gudy.azureus2.core3.config.COConfigurationManager;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

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
        //loadMessages();
        registerPreferencesChangeListener();
    }

    @Override
    protected File getVuzePath() {
        return SystemUtils.getAzureusDirectory();
    }

    @Override
    protected File getTorrentsPath() {
        return SystemUtils.getTorrentsDirectory();
    }

    /*
    private void loadMessages(Context context) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("PeerManager.status.finished", context.getString(R.string.azureus_peer_manager_status_finished));
        map.put("PeerManager.status.finishedin", context.getString(R.string.azureus_peer_manager_status_finishedin));
        map.put("Formats.units.alot", context.getString(R.string.azureus_formats_units_alot));
        map.put("discarded", context.getString(R.string.azureus_discarded));
        map.put("ManagerItem.waiting", context.getString(R.string.azureus_manager_item_waiting));
        map.put("ManagerItem.initializing", context.getString(R.string.azureus_manager_item_initializing));
        map.put("ManagerItem.allocating", context.getString(R.string.azureus_manager_item_allocating));
        map.put("ManagerItem.checking", context.getString(R.string.azureus_manager_item_checking));
        map.put("ManagerItem.finishing", context.getString(R.string.azureus_manager_item_finishing));
        map.put("ManagerItem.ready", context.getString(R.string.azureus_manager_item_ready));
        map.put("ManagerItem.downloading", context.getString(R.string.azureus_manager_item_downloading));
        map.put("ManagerItem.seeding", context.getString(R.string.azureus_manager_item_seeding));
        map.put("ManagerItem.superseeding", context.getString(R.string.azureus_manager_item_superseeding));
        map.put("ManagerItem.stopping", context.getString(R.string.azureus_manager_item_stopping));
        map.put("ManagerItem.stopped", context.getString(R.string.azureus_manager_item_stopped));
        map.put("ManagerItem.paused", context.getString(R.string.azureus_manager_item_paused));
        map.put("ManagerItem.queued", context.getString(R.string.azureus_manager_item_queued));
        map.put("ManagerItem.error", context.getString(R.string.azureus_manager_item_error));
        map.put("ManagerItem.forced", context.getString(R.string.azureus_manager_item_forced));
        map.put("GeneralView.yes", context.getString(R.string.azureus_general_view_yes));
        map.put("GeneralView.no", context.getString(R.string.azureus_general_view_no));

        //DisplayFormatters.loadMessages(map);
    }*/

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
