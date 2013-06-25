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
import java.util.concurrent.FutureTask;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class AsyncFutureTask<V> extends FutureTask<V> implements AsyncFuture<V> {

    private AsyncFutureListener<V> listener;

    private final Object lock = new Object();

    public AsyncFutureTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void setListener(AsyncFutureListener<V> listener) {
        synchronized (lock) {
            if (isDone()) {
                listener.onComplete(this);
            } else {
                this.listener = listener;
            }
        }
    }

    @Override
    protected void done() {
        synchronized (lock) {
            if (listener != null) {
                listener.onComplete(this);
            }
        }
    }
}
