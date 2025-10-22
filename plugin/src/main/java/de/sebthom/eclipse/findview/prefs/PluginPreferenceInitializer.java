/*
 * SPDX-FileCopyrightText: © Sebastian Thomschke and contributors.
 * SPDX-FileContributor: Sebastian Thomschke
 * SPDX-License-Identifier: EPL-2.0
 * SPDX-ArtifactOfProjectHomePage: https://github.com/sebthom/findview-eclipse-plugin
 */
package de.sebthom.eclipse.findview.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * @author Sebastian Thomschke
 */
public final class PluginPreferenceInitializer extends AbstractPreferenceInitializer {

   @Override
   public void initializeDefaultPreferences() {
      PluginPreferences.STORE.setDefault(PluginPreferences.PREF_CLOSE_WITH_ESC, false);
      PluginPreferences.STORE.setDefault(PluginPreferences.PREF_HIGHLIGHT_ALL, true);
      PluginPreferences.STORE.setDefault(PluginPreferences.PREF_MATCH_CASE, false);
      PluginPreferences.STORE.setDefault(PluginPreferences.PREF_MATCH_REGEX, false);
      PluginPreferences.STORE.setDefault(PluginPreferences.PREF_MATCH_WHOLEWORD, false);
   }
}
