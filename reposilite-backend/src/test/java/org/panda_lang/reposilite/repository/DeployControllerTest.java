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

package org.panda_lang.reposilite.repository;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.panda_lang.reposilite.ReposiliteIntegrationTest;
import org.panda_lang.utilities.commons.IOUtils;
import org.panda_lang.utilities.commons.StringUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DeployControllerTest extends ReposiliteIntegrationTest {

    private final HttpClient client = HttpClients.createDefault();

    @BeforeEach
    void configure() {
        super.reposilite.getTokenService().createToken("/releases/auth/test", "authtest", "secure");
    }

    @Test
    void shouldReturn401AndArtifactDeploymentIsDisabledMessage() throws IOException, AuthenticationException {
        super.reposilite.getConfiguration().deployEnabled = false;
        shouldReturn401AndGivenMessage("/releases/groupId/artifactId/file", "authtest", "secure", "content", "Artifact deployment is disabled");
    }

    @Test
    void shouldReturn401AndInvalidCredentialsMessage() throws IOException, AuthenticationException {
        shouldReturn401AndGivenMessage("/releases/groupId/artifactId/file", "authtest", "invalid_token", "content", "Invalid authorization credentials");
    }

    private void shouldReturn401AndGivenMessage(String uri, String username, String password, String content, String message) throws IOException, AuthenticationException {
        HttpResponse response = put(uri, username, password, content);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());

        String result = IOUtils.convertStreamToString(response.getEntity().getContent()).getValue();
        assertNotNull(result);
        assertTrue(result.contains(message));
    }

    @Test
    void shouldReturn200AndSuccessMessageForMetadataFiles() throws IOException, AuthenticationException {
        shouldReturn200AndSuccessMessage("/releases/auth/test/maven-metadata.xml", "authtest", "secure", StringUtils.EMPTY);
    }

    @Test
    void shouldReturn200AndSuccessMessage() throws IOException, AuthenticationException {
        shouldReturn200AndSuccessMessage("/releases/auth/test/pom.xml", "authtest", "secure", "maven metadata content");
    }

    private void shouldReturn200AndSuccessMessage(String uri, String username, String password, String content) throws IOException, AuthenticationException {
        HttpResponse deployResponse = put(uri, username, password, content);
        assertEquals(HttpStatus.SC_OK, deployResponse.getStatusLine().getStatusCode());

        String result = IOUtils.convertStreamToString(deployResponse.getEntity().getContent()).getValue();
        assertNotNull(result);
        assertEquals("Success", result);

        if (StringUtils.isEmpty(content)) {
            return;
        }

        assertEquals(HttpStatus.SC_OK, super.getAuthenticated(uri, username, password).getStatusCode());
        assertEquals(content, super.getAuthenticated(uri, username, password).parseAsString());
    }

    private HttpResponse put(String uri, String username, String password, String content) throws IOException, AuthenticationException {
        HttpPut httpPut = new HttpPut(url(uri).toString());
        httpPut.setEntity(new StringEntity(content));
        httpPut.addHeader(new BasicScheme().authenticate(new UsernamePasswordCredentials(username, password), httpPut, null));
        return client.execute(httpPut);
    }

}