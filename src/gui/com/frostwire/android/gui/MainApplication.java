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

package com.frostwire.android.gui;

import android.app.Application;
import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.gui.views.ImageLoader;
import com.frostwire.util.FileUtils;
import com.frostwire.vuze.AndroidVuzeEngine;
import com.frostwire.vuze.VuzeEngine;
import com.frostwire.vuze.VuzeManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // important initial setup here
            ConfigurationManager.create(this);
            NetworkManager.create(this);
            Librarian.create(this);
            Engine.create(this);

            loadVuzeMessages();

            ImageLoader.createDefaultInstance(this);

            FileUtils.deleteFolderRecursively(SystemUtils.getTempDirectory());

            Librarian.instance().syncMediaStore();
            Librarian.instance().syncApplicationsProvider();
        } catch (Throwable e) {
            String stacktrace = Log.getStackTraceString(e);
            throw new RuntimeException("MainApplication Create exception: " + stacktrace, e);
        }
    }

    private void loadVuzeMessages() {
        VuzeEngine engine = VuzeManager.getInstance().getEngine();
        if (engine instanceof AndroidVuzeEngine) {
            ((AndroidVuzeEngine) engine).loadMessages(this);
        }
    }
}
