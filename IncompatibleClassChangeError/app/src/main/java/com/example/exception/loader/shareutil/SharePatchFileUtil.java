/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.exception.loader.shareutil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import java.io.File;

public class SharePatchFileUtil {

    /**
     * 获取上一次源码发生变化的日期
     * @param context
     * @return
     */
    public static long getLastSourceModified(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo == null) {
            return 0;
        }

        return new File(applicationInfo.sourceDir).lastModified();
    }



    /**
     * change the jar file path as the makeDexElements do
     *
     * @param path
     * @param optimizedDirectory
     * @return
     */
    public static String optimizedPathFor(File path, File optimizedDirectory) {
        String fileName = path.getName();
        if (!fileName.endsWith(".dex")) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName += ".dex";
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(".dex");
                fileName = sb.toString();
            }
        }

        File result = new File(optimizedDirectory, fileName);
        return result.getPath();
    }
}

