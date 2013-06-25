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

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class AsyncExecutorService extends AbstractExecutorService implements AsyncExecutor {

    private final ExecutorService e;

    AsyncExecutorService(ExecutorService e) {
        this.e = e;
    }

    @Override
    public void shutdown() {
        e.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return e.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return e.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return e.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return e.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        e.execute(command);
    }

    @Override
    public <T> AsyncFuture<T> submit(Callable<T> task) {
        AsyncFutureTask<T> asyncTask = new AsyncFutureTask<T>(task);
        e.submit(asyncTask);
        return asyncTask;
    }
}
