/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Sebastian Thomschke
 */
public final class SearchBarPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

   @Override
   protected void createFieldEditors() {
      final var parent = getFieldEditorParent();

      addField(new RadioGroupFieldEditor( //
         SearchBarPreferences.PREF_CLOSE_WITH_ESC, //
         "When ESC key is pressed", //
         1, //
         new String[][] { //
            {"close the search bar", "true"}, //
            {"switch focus to the active editor", "false"} //
         }, //
         parent //
      ));
   }

   @Override
   public void init(final IWorkbench workbench) {
      setPreferenceStore(SearchBarPreferences.STORE);
   }
}
