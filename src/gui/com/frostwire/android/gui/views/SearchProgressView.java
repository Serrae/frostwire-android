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

package com.frostwire.android.gui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.offercast.android.sdk.OffercastSDK;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchProgressView extends LinearLayout {

    private ProgressBar progressbar;
    private Button buttonCancel;
    private Button buttonFreeApps;
    private TextView textNoResults;

    private boolean progressEnabled;

    public SearchProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.progressEnabled = true;
    }

    public boolean isProgressEnabled() {
        return progressEnabled;
    }

    public void setProgressEnabled(boolean enabled) {
        if (this.progressEnabled != enabled) {
            this.progressEnabled = enabled;

            if (enabled) {
                startProgress();
            } else {
                stopProgress();
            }
        }
    }

    public void setCancelOnClickListener(OnClickListener l) {
        buttonCancel.setOnClickListener(l);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_search_progress, this);

        if (isInEditMode()) {
            return;
        }

        progressbar = (ProgressBar) findViewById(R.id.view_search_progress_progressbar);
        buttonCancel = (Button) findViewById(R.id.view_search_progress_button_cancel);
        buttonFreeApps = (Button) findViewById(R.id.view_search_progress_button_free_apps);
        textNoResults = (TextView) findViewById(R.id.view_search_progress_text_no_results_feedback);
        
        initButtonFreeApps();
    }

    private void initButtonFreeApps() {
        buttonFreeApps.setVisibility(View.GONE);
        buttonFreeApps.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onFreeAppsClick();
            }
        });
    }

    private void startProgress() {
        progressbar.setVisibility(View.VISIBLE);
        buttonCancel.setText(android.R.string.cancel);
        textNoResults.setVisibility(View.GONE);
        buttonFreeApps.setVisibility(View.GONE);
    }

    private void stopProgress() {
        progressbar.setVisibility(View.GONE);
        buttonCancel.setText(R.string.retry_search);
        textNoResults.setVisibility(View.VISIBLE);
        buttonFreeApps.setVisibility(isfreeAppsEnabled() ? View.VISIBLE : View.GONE);
    }
    
    private boolean isfreeAppsEnabled() {
       ConfigurationManager config = ConfigurationManager.instance();
       return config.getBoolean(Constants.PREF_KEY_GUI_SUPPORT_FROSTWIRE) && config.getBoolean(Constants.PREF_KEY_GUI_SHOW_FREE_APPS_MENU_ITEM);
    }
    
    private void onFreeAppsClick() {
        if (isfreeAppsEnabled()) {
            try {
                OffercastSDK instance = OffercastSDK.getInstance(getContext());
                instance.showAppWallAd();
            } catch (Throwable t) {
            }
        }
    }
}