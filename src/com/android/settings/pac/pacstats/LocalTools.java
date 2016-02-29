/*
 * Copyright (C) 2016 The PAC-ROM Project
 *
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
