/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(TM). All rights reserved.
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

package com.frostwire.vuze;

import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.DisplayFormatters;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeFormatter {

    private VuzeFormatter() {
    }

    public static String formatShareRatio(int sr) {
        if (sr == Integer.MAX_VALUE) {
            sr = Integer.MAX_VALUE - 1;
        }
        if (sr == -1) {
            sr = Integer.MAX_VALUE;
        }

        String shareRatio = "";

        if (sr == Integer.MAX_VALUE) {
            shareRatio = Constants.INFINITY_STRING;
        } else {
            shareRatio = DisplayFormatters.formatDecimal((double) sr / 1000, 3);
        }

        return shareRatio;
    }
}
