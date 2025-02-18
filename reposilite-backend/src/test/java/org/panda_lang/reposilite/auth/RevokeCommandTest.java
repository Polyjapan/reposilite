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

package org.panda_lang.reposilite.auth;

import org.junit.jupiter.api.Test;
import org.panda_lang.reposilite.ReposiliteIntegrationTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevokeCommandTest extends ReposiliteIntegrationTest {

    @Test
    void shouldRevokeExistingToken() {
        TokenService tokenService = reposilite.getTokenService();
        tokenService.addToken(new Token("path", "alias", "secret"));

        assertTrue(new RevokeCommand("alias").execute(reposilite));
        assertNull(tokenService.getToken("alias"));
    }

    @Test
    void shouldFalseIfTokenDoesNotExist() {
        assertFalse(new RevokeCommand("unknown_token").execute(super.reposilite));
    }

    @Test
    void shouldFalseIfFileIsNotAvailable() throws Exception {
        super.reposilite.getTokenService().addToken(new Token("path", "alias", "secret"));
        File tokensFile = new File(super.workingDirectory, "tokens.dat");
        executeOnLocked(tokensFile, () -> assertFalse(new RevokeCommand("alias").execute(reposilite)));
        assertTrue(new RevokeCommand("alias").execute(reposilite));
    }

}