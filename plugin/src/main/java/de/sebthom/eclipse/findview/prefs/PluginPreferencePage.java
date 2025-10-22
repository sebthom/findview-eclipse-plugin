/*
 * SPDX-FileCopyrightText: © Sebastian Thomschke and contributors.
 * SPDX-FileContributor: Sebastian Thomschke
 * SPDX-License-Identifier: EPL-2.0
 * SPDX-ArtifactOfProjectHomePage: https://github.com/sebthom/findview-eclipse-plugin
 */
package de.sebthom.eclipse.findview.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Sebastian Thomschke
 */
public final class PluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

   @Override
   protected void createFieldEditors() {
      final var parent = getFieldEditorParent();

      addField(new RadioGroupFieldEditor( //
         PluginPreferences.PREF_CLOSE_WITH_ESC, //
         "When ESC key is pressed", //
         1, //
         new String[][] { //
            {"close the Find/Replace view", "true"}, //
            {"switch focus to the active editor", "false"} //
         }, //
         parent //
      ));
   }

   @Override
   public void init(final IWorkbench workbench) {
      setPreferenceStore(PluginPreferences.STORE);
   }
}
