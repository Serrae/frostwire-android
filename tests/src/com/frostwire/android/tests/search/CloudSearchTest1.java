/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
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

package com.frostwire.android.tests.search;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.frostwire.android.tests.TestUtils;
import com.frostwire.search.SearchManagerImpl;
import com.frostwire.search.SearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.search.WebSearchPerformer;
import com.frostwire.search.clearbits.ClearBitsSearchPerformer;
import com.frostwire.search.extratorrent.ExtratorrentSearchPerformer;
import com.frostwire.search.isohunt.ISOHuntSearchPerformer;
import com.frostwire.search.mininova.MininovaSearchPerformer;
import com.frostwire.search.soundcloud.SoundcloudSearchPerformer;
import com.frostwire.search.vertor.VertorSearchPerformer;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class CloudSearchTest1 extends TestCase {

    @MediumTest
    public void testSoundcloud() {
        testPerformer(new SoundcloudSearchPerformer(0, "frostclick", 5000));
    }

    @MediumTest
    public void testISOHunt() {
        testPerformer(new ISOHuntSearchPerformer(0, "frostclick", 5000));
    }

    @MediumTest
    public void testVertor() {
        testPerformer(new VertorSearchPerformer(0, "frostclick", 5000));
    }

    @MediumTest
    public void testMininova() {
        testPerformer(new MininovaSearchPerformer(0, "frostclick", 5000));
    }

    @MediumTest
    public void testClearBits() {
        testPerformer(new ClearBitsSearchPerformer(0, "Big Buck Bunny", 5000));
    }

    @MediumTest
    public void testExtratorrent() {
        testPerformer(new ExtratorrentSearchPerformer(0, "frostclick", 5000));
    }

    private void testPerformer(WebSearchPerformer performer) {
        final CountDownLatch signal = new CountDownLatch(1);

        MockSearchResultListener l = new MockSearchResultListener() {
            @Override
            public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
                super.onResults(performer, results);
                signal.countDown();
            }
        };

        SearchManagerImpl manager = new SearchManagerImpl();
        manager.registerListener(l);
        manager.perform(performer);

        TestUtils.await(signal, 10, TimeUnit.SECONDS);

        assertTrue("Did not finish or took too much time", manager.shutdown(5, TimeUnit.SECONDS));

        assertTrue("Didn't get more than one result", l.getNumResults() > 1);

        l.logResults();
    }
}
