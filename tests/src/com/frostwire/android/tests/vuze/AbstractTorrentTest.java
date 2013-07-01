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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.android.tests.AbstractApplicationTest;
import com.frostwire.android.tests.TestUtils;
import com.frostwire.concurrent.AsyncFuture;
import com.frostwire.concurrent.AsyncFutureListener;
import com.frostwire.vuze.VuzeManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class AbstractTorrentTest extends AbstractApplicationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        awaitForVuze();
    }

    private void awaitForVuze() {
        // this is a work in progress, we should move to a complete
        // asynchronous pattern.
        //
        // waiting for the core to be in the started state.
        final CountDownLatch ready = new CountDownLatch(1);
        AsyncFuture<AzureusCore> f = VuzeManager.getInstance().getCore();
        f.setListener(new AsyncFutureListener<AzureusCore>() {
            @Override
            public void onComplete(AsyncFuture<AzureusCore> future) {
                ready.countDown();
            }
        });

        TestUtils.await(ready, 1, TimeUnit.MINUTES);
    }
}
