/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.exbin;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.framework.api.Preferences;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.api.XBModuleRepositoryUtils;
import org.exbin.xbup.client.XBCatalogNetServiceClient;
import org.exbin.framework.exbin.dialog.LoginDialog;
import org.exbin.framework.exbin.panel.ExbinMainPanel;
import org.exbin.framework.file.api.FileModuleApi;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.api.XBApplicationModule;
import org.exbin.xbup.plugin.XBModuleHandler;

/**
 * XBUP service manager module.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class ExbinModule implements XBApplicationModule {

    public static final String MODULE_ID = XBModuleRepositoryUtils.getModuleIdByApi(ExbinModule.class);

    private XBApplication application;
    private ExbinMainPanel mainPanel;
    private Preferences preferences;

    public ExbinModule() {
    }

    @Override
    public void init(XBModuleHandler moduleHandler) {
        this.application = (XBApplication) moduleHandler;

        // Register file types
        FileModuleApi fileModule = application.getModuleRepository().getModuleByInterface(FileModuleApi.class);
//        fileModule.addFileType(new XBTFileType());
//        fileModule.addFileType(new TXTFileType());
    }

    @Override
    public void unregisterModule(String moduleId) {
    }

    public void openConnectionDialog() {
        final LoginDialog loginDialog = new LoginDialog(WindowUtils.getFrame(mainPanel), true);
        loginDialog.setLocationRelativeTo(loginDialog.getParent());
        loginDialog.loadConnectionList(preferences);
        loginDialog.setConnectionListener(new LoginDialog.ConnectionListener() {
            @Override
            public boolean connect() {
                String connectionString = loginDialog.getSelectedConnection();
                String connectionHost;
                int connectionPort = 22594; // is 0x5842 (XB)
                int pos = connectionString.indexOf(":");
                if (pos >= 0) {
                    connectionHost = connectionString.substring(0, pos);
                    connectionPort = Integer.valueOf(connectionString.substring(pos + 1));
                } else {
                    connectionHost = connectionString;
                }

                loginDialog.setConnectionStatus(Color.ORANGE, "Connecting", "Connecting to server " + connectionHost + ":" + connectionPort);

                XBCatalogNetServiceClient service = new XBCatalogNetServiceClient(connectionHost, connectionPort);
                try {
                    service.ping();
                    loginDialog.setConnectionStatus(Color.GREEN, "Connected", null);
                    return true;
                } catch (Exception ex) {
                    Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                    loginDialog.setConnectionStatus(Color.RED, "Failed", null);
                }

                return false;
            }
        });
        loginDialog.setVisible(true);
        loginDialog.saveConnectionList(preferences);
        getExbinMainPanel().setService(loginDialog.getService());
    }

    public ExbinMainPanel getExbinMainPanel() {
        if (mainPanel == null) {
            mainPanel = new ExbinMainPanel();
        }

        return mainPanel;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }
}
