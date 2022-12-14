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
package org.exbin.is;

import java.awt.Dimension;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.exbin.framework.about.api.AboutModuleApi;
import org.exbin.framework.data.api.DataModuleApi;
import org.exbin.framework.editor.api.EditorModuleApi;
import org.exbin.framework.frame.api.ApplicationFrameHandler;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.help.api.HelpModuleApi;
import org.exbin.framework.action.api.ActionModuleApi;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.operation.undo.api.OperationUndoModuleApi;
import org.exbin.framework.api.XBApplicationModuleRepository;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.preferences.PreferencesWrapper;

/**
 * The main class of the ExbinIs application.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
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

                FrameModuleApi frameModule = moduleRepository.getModuleByInterface(FrameModuleApi.class);
                EditorModuleApi editorModule = moduleRepository.getModuleByInterface(EditorModuleApi.class);
                ActionModuleApi actionModule = moduleRepository.getModuleByInterface(ActionModuleApi.class);
                AboutModuleApi aboutModule = moduleRepository.getModuleByInterface(AboutModuleApi.class);
                HelpModuleApi helpModule = moduleRepository.getModuleByInterface(HelpModuleApi.class);
                OperationUndoModuleApi undoModule = moduleRepository.getModuleByInterface(OperationUndoModuleApi.class);
                DataModuleApi dataModule = moduleRepository.getModuleByInterface(DataModuleApi.class);
                OptionsModuleApi optionsModule = moduleRepository.getModuleByInterface(OptionsModuleApi.class);
                ExbinModule exbinModule = moduleRepository.getModuleByInterface(ExbinModule.class);
                EditorXbupModule xbupEditorModule = moduleRepository.getModuleByInterface(EditorXbupModule.class);
                final EditorTextModule textEditorModule = moduleRepository.getModuleByInterface(EditorTextModule.class);

                frameModule.createMainMenu();
                aboutModule.registerDefaultMenuItem();

                frameModule.registerExitAction();
                frameModule.registerStatusBarVisibilityActions();

                // Register clipboard editing actions
                actionModule.registerMenuClipboardActions();

                optionsModule.registerMenuAction();

                ApplicationFrameHandler frameHandler = frameModule.getFrameHandler();

                exbinModule.setPreferences(preferences);
                ExbinMainPanel mainPanel = exbinModule.getExbinMainPanel();
                mainPanel.setPanel(dataModule.getTableEditPanel());
                frameHandler.setMainPanel(mainPanel);
                frameHandler.setDefaultSize(new Dimension(600, 400));
                frameHandler.showFrame();

                exbinModule.openConnectionDialog();
            }
        } catch (ParseException ex) {
            Logger.getLogger(ExbinIs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
