/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.apkzlib.zip;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Factory for verification logs.
 */
final class VerifyLogs {

    private VerifyLogs() {}

    /**
     * Creates a {@link VerifyLog} that ignores all messages logged.
     *
     * @return the log
     */
    @Nonnull
    static VerifyLog devNull() {
        return new VerifyLog() {
            @Override
            public void log(@Nonnull String message) {}

            @Nonnull
            @Override
            public ImmutableList<String> getLogs() {
                return ImmutableList.of();
            }
        };
    }

    /**
     * Creates a {@link VerifyLog} that stores all log messages.
     *
     * @return the log
     */
    @Nonnull
    static VerifyLog unlimited() {
        return new VerifyLog() {

            /**
             * All saved messages.
             */
            @Nonnull
            private final List<String> messages = new ArrayList<>();

            @Override
            public void log(@Nonnull String message) {
                messages.add(message);
            }

            @Nonnull
            @Override
            public ImmutableList<String> getLogs() {
                return ImmutableList.copyOf(messages);
            }
        };
    }
}
