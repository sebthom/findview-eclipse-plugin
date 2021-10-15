/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * @author Sebastian Thomschke
 */
public final class SearchBarPreferenceInitializer extends AbstractPreferenceInitializer {

   @Override
   public void initializeDefaultPreferences() {
      SearchBarPreferences.STORE.setDefault(SearchBarPreferences.PREF_CLOSE_WITH_ESC, false);
      SearchBarPreferences.STORE.setDefault(SearchBarPreferences.PREF_HIGHLIGHT_ALL, false);
      SearchBarPreferences.STORE.setDefault(SearchBarPreferences.PREF_MATCH_CASE, false);
   }
}
