/*
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
package io.trino.plugin.jdbc.credential;

import com.google.common.collect.ImmutableMap;
import io.airlift.bootstrap.Bootstrap;
import io.trino.spi.security.ConnectorIdentity;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExtraCredentialProvider
{
    @Test
    public void testUserNameOverwritten()
    {
        Map<String, String> properties = ImmutableMap.of(
                "connection-user", "default_user",
                "connection-password", "default_password",
                "user-credential-name", "user");

        CredentialProvider credentialProvider = getCredentialProvider(properties);
        Optional<ConnectorIdentity> identity = Optional.of(ConnectorIdentity.forUser("user").withExtraCredentials(ImmutableMap.of("user", "overwritten_user")).build());
        assertThat(credentialProvider.getConnectionUser(identity).get()).isEqualTo("overwritten_user");
        assertThat(credentialProvider.getConnectionPassword(identity).get()).isEqualTo("default_password");
    }

    @Test
    public void testPasswordOverwritten()
    {
        Map<String, String> properties = ImmutableMap.of(
                "connection-user", "default_user",
                "connection-password", "default_password",
                "password-credential-name", "password");

        CredentialProvider credentialProvider = getCredentialProvider(properties);
        Optional<ConnectorIdentity> identity = Optional.of(ConnectorIdentity.forUser("user").withExtraCredentials(ImmutableMap.of("password", "overwritten_password")).build());
        assertThat(credentialProvider.getConnectionUser(identity).get()).isEqualTo("default_user");
        assertThat(credentialProvider.getConnectionPassword(identity).get()).isEqualTo("overwritten_password");
    }

    @Test
    public void testCredentialsOverwritten()
    {
        Map<String, String> properties = ImmutableMap.of(
                "connection-user", "default_user",
                "connection-password", "default_password",
                "user-credential-name", "user",
                "password-credential-name", "password");

        CredentialProvider credentialProvider = getCredentialProvider(properties);
        Optional<ConnectorIdentity> identity = Optional.of(ConnectorIdentity.forUser("user")
                .withExtraCredentials(ImmutableMap.of("user", "overwritten_user", "password", "overwritten_password"))
                .build());
        assertThat(credentialProvider.getConnectionUser(identity).get()).isEqualTo("overwritten_user");
        assertThat(credentialProvider.getConnectionPassword(identity).get()).isEqualTo("overwritten_password");
    }

    @Test
    public void testCredentialsNotOverwritten()
    {
        Map<String, String> properties = ImmutableMap.of(
                "connection-user", "default_user",
                "connection-password", "default_password",
                "user-credential-name", "user",
                "password-credential-name", "password");

        CredentialProvider credentialProvider = getCredentialProvider(properties);
        Optional<ConnectorIdentity> identity = Optional.of(ConnectorIdentity.ofUser("user"));
        assertThat(credentialProvider.getConnectionUser(identity).get()).isEqualTo("default_user");
        assertThat(credentialProvider.getConnectionPassword(identity).get()).isEqualTo("default_password");

        identity = Optional.of(ConnectorIdentity.forUser("user")
                .withExtraCredentials(ImmutableMap.of("connection_user", "overwritten_user", "connection_password", "overwritten_password"))
                .build());
        assertThat(credentialProvider.getConnectionUser(identity).get()).isEqualTo("default_user");
        assertThat(credentialProvider.getConnectionPassword(identity).get()).isEqualTo("default_password");
    }

    private static CredentialProvider getCredentialProvider(Map<String, String> properties)
    {
        return new Bootstrap(new CredentialProviderModule())
                .doNotInitializeLogging()
                .quiet()
                .setRequiredConfigurationProperties(properties)
                .initialize()
                .getInstance(CredentialProvider.class);
    }
}
