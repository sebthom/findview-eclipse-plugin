/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.localization;

import static net.sf.jstuff.core.validation.NullAnalysisHelper.*;

import de.sebthom.eclipse.commons.localization.MessagesInitializer;

/**
 * @author Sebastian Thomschke
 */
public final class Messages {

   private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages";

   // Keys with default values directly assigned in this class are only used by Java classes.
   // Keys without default values are loaded from messages.properties, because they are also referenced in plugin.xml

   // CHECKSTYLE:IGNORE .* FOR NEXT 100 LINES

   public static String FindView_NoMatchNotFound = lazyNonNull();
   public static String FindView_GotoNextButton = lazyNonNull();
   public static String FindView_GotoPrevButton = lazyNonNull();
   public static String FindView_ReplaceButton = lazyNonNull();
   public static String FindView_ReplaceAllButton = lazyNonNull();
   public static String FindView_HighlightAll = lazyNonNull();
   public static String FindView_MatchCase = lazyNonNull();
   public static String FindView_MatchRegEx = lazyNonNull();
   public static String FindView_MatchWholeWord = lazyNonNull();
   public static String FindView_FindLabel = lazyNonNull();

   static {
      MessagesInitializer.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {
   }
}
