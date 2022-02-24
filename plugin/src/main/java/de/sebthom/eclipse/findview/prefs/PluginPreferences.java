/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.prefs;

import java.io.IOException;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

import de.sebthom.eclipse.findview.Plugin;
import net.sf.jstuff.core.io.RuntimeIOException;

/**
 * @author Sebastian Thomschke
 */
public final class PluginPreferences {

   public static final String PREF_CLOSE_WITH_ESC = "closeWithEsc";
   public static final String PREF_HIGHLIGHT_ALL = "highlightAll";
   public static final String PREF_MATCH_CASE = "matchCase";
   public static final String PREF_MATCH_REGEX = "matchRegEx";
   public static final String PREF_MATCH_WHOLEWORD = "matchWholeWord";

   static final IPersistentPreferenceStore STORE = Plugin.get().getPreferenceStore();

   public static void addListener(final IPropertyChangeListener listener) {
      STORE.addPropertyChangeListener(listener);
   }

   public static boolean isCloseWithEsc() {
      return STORE.getBoolean(PREF_CLOSE_WITH_ESC);
   }

   public static boolean isHighlightAll() {
      return STORE.getBoolean(PREF_HIGHLIGHT_ALL);
   }

   public static boolean isMatchCase() {
      return STORE.getBoolean(PREF_MATCH_CASE);
   }

   public static boolean isMatchRegEx() {
      return STORE.getBoolean(PREF_MATCH_REGEX);
   }

   public static boolean isMatchWholeWord() {
      return STORE.getBoolean(PREF_MATCH_WHOLEWORD);
   }

   public static void removeListener(final IPropertyChangeListener listener) {
      STORE.removePropertyChangeListener(listener);
   }

   public static void save() {
      if (STORE.needsSaving()) {
         try {
            STORE.save();
         } catch (final IOException ex) {
            throw new RuntimeIOException(ex);
         }
      }
   }

   public static void setCloseWithEsc(final boolean value) {
      STORE.setValue(PREF_CLOSE_WITH_ESC, value);
   }

   public static void setHighlightAll(final boolean value) {
      STORE.setValue(PREF_HIGHLIGHT_ALL, value);
   }

   public static void setMatchCase(final boolean value) {
      STORE.setValue(PREF_MATCH_CASE, value);
   }

   public static void setMatchRegEx(final boolean value) {
      STORE.setValue(PREF_MATCH_REGEX, value);
   }

   public static void setMatchWholeWord(final boolean value) {
      STORE.setValue(PREF_MATCH_WHOLEWORD, value);
   }

   private PluginPreferences() {
   }
}
