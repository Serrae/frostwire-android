/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(R). All rights reserved.
 
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

package com.frostwire.android.tests.vuze;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import android.test.suitebuilder.annotation.LargeTest;

import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.tests.TestUtils;
import com.frostwire.torrent.TOTorrent;
import com.frostwire.vuze.VuzeDownloadAdapter;
import com.frostwire.vuze.VuzeDownloadFactory;
import com.frostwire.vuze.VuzeDownloadManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class PartialDownloadTest extends AbstractTorrentTest {

    @LargeTest
    public void testDownloadFirstFile() throws Exception {
        String url = "http://dl.frostwire.com/torrents/audio/music/Kings_of_the_City__The_FrostWire_EP__FROSTCLICK_MP3_256K_2013_JUNE_11.torrent";
        File f = new File(SystemUtils.getTorrentsDirectory(), FilenameUtils.getName(url));
        TOTorrent t = TestUtils.downloadTorrent(url);

        t.serialiseToBEncodedFile(f);

        File saveDir = new File(SystemUtils.getTorrentDataDirectory(), "tests");
        FileUtils.deleteDirectory(saveDir);

        String firstRelativePath = t.getFiles()[0].getRelativePath();
        File endFile = new File(new File(saveDir, FilenameUtils.getBaseName(url)), firstRelativePath);

        Set<String> fileSelection = new HashSet<String>();
        fileSelection.add(firstRelativePath);

        final CountDownLatch downloadFinished = new CountDownLatch(1);

        VuzeDownloadFactory.create(f.getAbsolutePath(), t.getHash(), fileSelection, saveDir.getAbsolutePath(), new VuzeDownloadAdapter() {

            @Override
            public void stateChanged(VuzeDownloadManager dm, int state) {
                System.out.println("DM - State=" + state);
            }

            @Override
            public void downloadComplete(VuzeDownloadManager dm) {
                downloadFinished.countDown();
            }
        });

        TestUtils.await(downloadFinished, 5, TimeUnit.MINUTES);

        assertTrue(firstRelativePath + " does not exist", endFile.exists());
    }
}
