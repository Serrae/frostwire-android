/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.frostwire.android.gui.services;

import java.util.concurrent.ExecutorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.player.CoreMediaPlayer;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.NetworkManager;
//import com.frostwire.android.gui.transfers.AzureusManager;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.util.concurrent.ExecutorsHelper;

/**
 * Receives and controls messages from the external world. Depending on the
 * status it attempts to control what happens with the Engine.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class EngineBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "FW.EngineBroadcastReceiver";

    private final ExecutorService engineExecutor;

    private boolean wasPlaying;

    public EngineBroadcastReceiver() {
        engineExecutor = ExecutorsHelper.newFixedSizeThreadPool(1, "BroadcastReceiver-EngineExecutor");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                if (Engine.instance().isDisconnected()) {
                    engineExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Engine.instance().startServices();
                        }
                    });
                }
            } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                handleActionPhoneStateChanged(intent);
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                Librarian.instance().syncMediaStore();
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                if (networkInfo.getDetailedState() == DetailedState.DISCONNECTED) {
                    handleDisconnectedNetwork(networkInfo);
                } else if (networkInfo.getDetailedState() == DetailedState.CONNECTED) {
                    handleConnectedNetwork(networkInfo);
                }
            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                if (Engine.instance().getMediaPlayer().isPlaying()) {
                    Engine.instance().getMediaPlayer().togglePause();
                }
            } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                Librarian.instance().syncApplicationsProvider();
            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                // no sure about this case
            }

            if (!Librarian.instance().isExternalStorageMounted()) {// && AzureusManager.isCreated()) {
                Log.v(TAG, "Halting process due to lack of external storage");
                Librarian.instance().halt();
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error processing broadcast message", e);
        }
    }

    private void handleActionPhoneStateChanged(Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String msg = "Phone state changed to " + state;
        Log.v(TAG, msg);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) || TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            CoreMediaPlayer mediaPlayer = Engine.instance().getMediaPlayer();
            if (mediaPlayer.isPlaying()) {
                wasPlaying = true;
                mediaPlayer.togglePause();
            } else if (wasPlaying && TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                mediaPlayer.seekTo(Math.max(0, mediaPlayer.getPosition() - 2000));
                wasPlaying = false;
                mediaPlayer.togglePause();
            }
        }
    }

    private void handleDisconnectedNetwork(NetworkInfo networkInfo) {
        Log.v(TAG, "Disconnected from network (" + networkInfo.getTypeName() + ")");
        engineExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Engine.instance().stopServices(true);
            }
        });
    }

    private void handleConnectedNetwork(NetworkInfo networkInfo) {
        if (NetworkManager.instance().isDataUp()) {
            Log.v(TAG, "Connected to " + networkInfo.getTypeName());
            if (Engine.instance().isDisconnected()) {
                // avoid ANR error inside a broadcast receiver
                engineExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Engine.instance().startServices();

                        if (!NetworkManager.instance().isDataWIFIUp() && ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY)) {
                            TransferManager.instance().stopSeedingTorrents();
                        }
                    }
                });
            }

            NetworkManager.instance().printNetworkInfo();
        }
    }
}
