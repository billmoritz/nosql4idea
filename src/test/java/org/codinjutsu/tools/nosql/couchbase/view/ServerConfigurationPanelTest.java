/*
 * Copyright (c) 2015 David Boissier
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

package org.codinjutsu.tools.nosql.couchbase.view;

import com.intellij.openapi.command.impl.DummyProject;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.view.ServerConfigurationPanel;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerConfigurationPanelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerConfigurationPanel configurationPanel;
    private DatabaseClient databaseClientMock;

    private FrameFixture frameFixture;

    @Before
    public void setUp() throws Exception {
        databaseClientMock = Mockito.mock(DatabaseClient.class);
        configurationPanel = GuiActionRunner.execute(new GuiQuery<ServerConfigurationPanel>() {
            protected ServerConfigurationPanel executeInEDT() {
                return new ServerConfigurationPanel(DummyProject.getInstance(),
                        DatabaseVendor.COUCHBASE,
                        databaseClientMock,
                        new CouchbaseAuthenticationPanel()
                );
            }
        });

        frameFixture = Containers.showInFrame(configurationPanel);
    }

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Test
    public void createCouchbaseConfiguration() throws Exception {
        frameFixture.textBox("labelField").setText("Localhost");

        frameFixture.label("databaseVendorLabel").requireText("Couchbase");
        frameFixture.label("databaseTipsLabel").requireText("format: host:port. If cluster: host:port1,host:port2,...");

        frameFixture.textBox("serverUrlField").setText("localhost:25");
        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");

        frameFixture.textBox("userDatabaseField").setText("mybucket");
        frameFixture.checkBox("autoConnectField").check();

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals("Localhost", configuration.getLabel());
        assertEquals(DatabaseVendor.COUCHBASE, configuration.getDatabaseVendor());
        assertEquals("localhost:25", configuration.getServerUrl());

        AuthenticationSettings authenticationSettings = configuration.getAuthenticationSettings();
        assertEquals("john", authenticationSettings.getUsername());
        assertEquals("johnpassword", authenticationSettings.getPassword());

        assertEquals("mybucket", configuration.getUserDatabase());
        assertTrue(configuration.isConnectOnIdeStartup());
    }

    @Test
    public void loadCouchbaseConfiguration() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setLabel("Localhost");
        configuration.setDatabaseVendor(DatabaseVendor.COUCHBASE);
        configuration.setServerUrl("localhost:25");

        AuthenticationSettings authenticationSettings = new AuthenticationSettings();
        authenticationSettings.setUsername("john");
        authenticationSettings.setPassword("johnpassword");
        configuration.setAuthenticationSettings(authenticationSettings);

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("labelField").requireText("Localhost");
        frameFixture.label("databaseVendorLabel").requireText("Couchbase");
        frameFixture.label("databaseTipsLabel").requireText("format: host:port. If cluster: host:port1,host:port2,...");
        frameFixture.textBox("serverUrlField").requireText("localhost:25");
        frameFixture.textBox("usernameField").requireText("john");
        frameFixture.textBox("passwordField").requireText("johnpassword");
    }
}
