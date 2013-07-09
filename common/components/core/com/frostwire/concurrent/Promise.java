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

package com.frostwire.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class Promise<V> {

    private final CountDownLatch latch;
    private final Callable<V> callable;

    private V result;
    private Exception error;

    public Promise() {
        this.latch = new CountDownLatch(1);
        this.callable = new Callable<V>() {
            @Override
            public V call() throws Exception {
                latch.await();

                // there is a minor change here of bad use from the client,
                // calling success and failure at the same time in a bad logic
                if (error != null) {
                    throw error;
                }

                return result;
            }
        };
    }

    public void success(V result) {
        this.result = result;
        latch.countDown();
    }

    public void failure(Exception e) {
        this.error = e;
        latch.countDown();
    }

    public AsyncFuture<V> future() {
        AsyncFutureTask<V> task = new AsyncFutureTask<V>(callable);
        task.run();
        return task;
    }
}
