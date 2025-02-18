/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.reposilite.api;

import io.javalin.http.Context;

public final class ErrorUtils {

    private ErrorUtils() { }

    public static Context error(Context context, int status, String message) {
        return error(context, status, message, false);
    }

    public static Context error(Context context, int status, String message, boolean requireCredentials) {
        Context ctx = context
                .status(status)
                .json(new ErrorDto(status, message));

        if (requireCredentials) {
            return ctx.header("WWW-Authenticate", "Basic realm=reposilite");
        } else {
            return ctx;
        }
    }
}
