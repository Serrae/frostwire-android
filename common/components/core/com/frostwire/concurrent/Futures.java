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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class Futures {

    private Futures() {
    }

    public static <V> AsyncFuture<V> successful(V value) {
        return new Successful<V>(value);
    }

    public static <V> AsyncFuture<V> failed(Throwable e) {
        return new Failed<V>(e);
    }

    private static final class Successful<V> implements AsyncFuture<V> {

        private final V value;

        public Successful(V value) {
            this.value = value;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return value;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return value;
        }

        @Override
        public void setListener(AsyncFutureListener<V> listener) {
            listener.onComplete(this);
        }
    }

    private static final class Failed<V> implements AsyncFuture<V> {

        private final ExecutionException e;

        public Failed(Throwable e) {
            this.e = new ExecutionException(e);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            throw e;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw e;
        }

        @Override
        public void setListener(AsyncFutureListener<V> listener) {
            listener.onComplete(this);
        }
    }
}
