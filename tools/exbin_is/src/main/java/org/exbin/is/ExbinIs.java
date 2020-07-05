/*
 * Copyright (C) ExBin Project
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
package org.exbin.is;

import java.awt.Dimension;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.exbin.framework.XBBaseApplication;
import org.exbin.framework.api.Preferences;
import org.exbin.xbup.core.parser.basic.XBHead;
import org.exbin.framework.editor.text.EditorTextModule;
import org.exbin.framework.editor.xbup.EditorXbupModule;
import org.exbin.framework.exbin.ExbinModule;
import org.exbin.framework.exbin.panel.ExbinMainPanel;
import org.exbin.framework.gui.about.api.GuiAboutModuleApi;
import org.exbin.framework.gui.data.api.GuiDataModuleApi;
import org.exbin.framework.gui.editor.api.GuiEditorModuleApi;
import org.exbin.framework.gui.frame.api.ApplicationFrameHandler;
import org.exbin.framework.gui.frame.api.GuiFrameModuleApi;
import org.exbin.framework.gui.help.api.GuiHelpModuleApi;
import org.exbin.framework.gui.menu.api.GuiMenuModuleApi;
import org.exbin.framework.gui.options.api.GuiOptionsModuleApi;
import org.exbin.framework.gui.undo.api.GuiUndoModuleApi;
import org.exbin.framework.api.XBApplicationModuleRepository;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.preferences.PreferencesWrapper;

/**
 * The main class of the ExbinIs application.
 *
 * @version 0.2.0 2016/08/18
 * @author ExBin Project (http://exbin.org)
 */
public class ExbinIs {

    /**
     * Main method launching the application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Preferences preferences;
        boolean verboseMode;
        boolean devMode;
        final ResourceBundle bundle = LanguageUtils.getResourceBundleByClass(ExbinIs.class);

        try {
            preferences = new PreferencesWrapper(java.util.prefs.Preferences.userNodeForPackage(ExbinIs.class));
        } catch (SecurityException ex) {
            preferences = null;
        }
        try {
            // Parameters processing
            Options opt = new Options();
            opt.addOption("h", "help", false, bundle.getString("cl_option_help"));
            opt.addOption("v", false, bundle.getString("cl_option_verbose"));
            opt.addOption("dev", false, bundle.getString("cl_option_dev"));
            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, args);
            if (cl.hasOption('h')) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp(bundle.getString("cl_syntax"), opt);
            } else {
                verboseMode = cl.hasOption("v");
                devMode = cl.hasOption("dev");
                Logger logger = Logger.getLogger("");
                try {
                    logger.setLevel(Level.ALL);
                    logger.addHandler(new XBHead.XBLogHandler(verboseMode));
                } catch (java.security.AccessControlException ex) {
                    // Ignore it in java webstart
                }

                XBBaseApplication app = new XBBaseApplication();
                app.setAppPreferences(preferences);
                app.setAppBundle(bundle, LanguageUtils.getResourceBaseNameBundleByClass(ExbinIs.class));

                XBApplicationModuleRepository moduleRepository = app.getModuleRepository();
                moduleRepository.addClassPathModules();
                moduleRepository.addModulesFromManifest(ExbinIs.class);
                moduleRepository.initModules();
                app.init();

                GuiFrameModuleApi frameModule = moduleRepository.getModuleByInterface(GuiFrameModuleApi.class);
                GuiEditorModuleApi editorModule = moduleRepository.getModuleByInterface(GuiEditorModuleApi.class);
                GuiMenuModuleApi menuModule = moduleRepository.getModuleByInterface(GuiMenuModuleApi.class);
                GuiAboutModuleApi aboutModule = moduleRepository.getModuleByInterface(GuiAboutModuleApi.class);
                GuiHelpModuleApi helpModule = moduleRepository.getModuleByInterface(GuiHelpModuleApi.class);
                GuiUndoModuleApi undoModule = moduleRepository.getModuleByInterface(GuiUndoModuleApi.class);
                GuiDataModuleApi dataModule = moduleRepository.getModuleByInterface(GuiDataModuleApi.class);
                GuiOptionsModuleApi optionsModule = moduleRepository.getModuleByInterface(GuiOptionsModuleApi.class);
                ExbinModule exbinModule = moduleRepository.getModuleByInterface(ExbinModule.class);
                EditorXbupModule xbupEditorModule = moduleRepository.getModuleByInterface(EditorXbupModule.class);
                final EditorTextModule textEditorModule = moduleRepository.getModuleByInterface(EditorTextModule.class);

                frameModule.createMainMenu();
                aboutModule.registerDefaultMenuItem();

                frameModule.registerExitAction();
                frameModule.registerStatusBarVisibilityActions();

                // Register clipboard editing actions
                menuModule.registerMenuClipboardActions();

                optionsModule.registerMenuAction();

                ApplicationFrameHandler frameHandler = frameModule.getFrameHandler();

                exbinModule.setPreferences(preferences);
                ExbinMainPanel mainPanel = exbinModule.getExbinMainPanel();
                mainPanel.setPanel(dataModule.getTableEditPanel());
                frameHandler.setMainPanel(mainPanel);
                frameHandler.setDefaultSize(new Dimension(600, 400));
                frameHandler.show();

                exbinModule.openConnectionDialog();
            }
        } catch (ParseException ex) {
            Logger.getLogger(ExbinIs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
