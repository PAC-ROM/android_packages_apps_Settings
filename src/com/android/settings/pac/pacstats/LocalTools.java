/*
 *    PAC ROM Console. Settings and OTA
 *    Copyright (C) 2014  pvyParts (Aaron Kable)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.android.settings.pac.pacstats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Get prop grabberer
 *
 * @author pvyParts
 *
 */
public class LocalTools {
    public static String getProp(String propKey) {
        Process p = null;
        String propVal = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propKey)
            .redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                propVal = line;
            }
            p.destroy();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return propVal;
    }

}
