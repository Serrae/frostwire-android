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

package com.frostwire.android.gui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.PeerManager;
import com.frostwire.android.gui.adapters.PeerListAdapter;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.views.AbstractActivity;
import com.frostwire.android.gui.views.AbstractListFragment;
import com.frostwire.android.gui.views.Refreshable;
//import com.frostwire.gui.upnp.UPnPManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class BrowsePeersFragment extends AbstractListFragment implements Refreshable, MainFragment {

    private PeerListAdapter adapter;

    private TextView header;

    private int refreshUPnPCount;

    public BrowsePeersFragment() {
        super(R.layout.fragment_browse_peers);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        if (Engine.instance().isStarted() && ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_UPNP)) {
            //UPnPManager.instance().resume();
        }

        setupAdapter();

        if (getActivity() instanceof AbstractActivity) {
            ((AbstractActivity) getActivity()).addRefreshable(this);
        }
    }

    @Override
    public void refresh() {
        List<Peer> peers = PeerManager.instance().getPeers();
        adapter.updateList(peers);
        
        refreshUPnPCount++;

        if (refreshUPnPCount % 10 == 0) {
            if (Engine.instance().isStarted() && ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_UPNP)) {
                //UPnPManager.instance().refreshRemoteDevices();
            }
        }
    }

    @Override
    public void dismissDialogs() {
        super.dismissDialogs();

        if (adapter != null) {
            adapter.dismissDialogs();
        }
    }

    @Override
    public View getHeader(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        header = (TextView) inflater.inflate(R.layout.view_main_fragment_simple_header, null);
        header.setText(R.string.wifi_sharing);

        return header;
    }

    private void setupAdapter() {
        adapter = new PeerListAdapter(this.getActivity(), new ArrayList<Peer>());
        setListAdapter(adapter);
        refresh();
    }
}
