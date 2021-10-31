/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.localization;

import de.sebthom.eclipse.commons.localization.MessagesInitializer;

/**
 * @author Sebastian Thomschke
 */
public final class Messages {

   private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages";

   // Keys with default values directly assigned in this class are only used by Java classes.
   // Keys without default values are loaded from messages.properties, because they are also referenced in plugin.xml

   // CHECKSTYLE:IGNORE .* FOR NEXT 100 LINES

   public static String FindView_NoMatchNotFound;
   public static String FindView_GotoNextButton;
   public static String FindView_GotoPrevButton;
   public static String FindView_ReplaceButton;
   public static String FindView_ReplaceAllButton;
   public static String FindView_HighlightAll;
   public static String FindView_MatchCase;
   public static String FindView_MatchRegEx;
   public static String FindView_MatchWholeWord;
   public static String FindView_FindLabel;

   static {
      MessagesInitializer.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {
   }
}
