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

   public static String FindView_NoMatchNotFound = eventuallyNonNull();
   public static String FindView_GotoNextButton = eventuallyNonNull();
   public static String FindView_GotoPrevButton = eventuallyNonNull();
   public static String FindView_ReplaceButton = eventuallyNonNull();
   public static String FindView_ReplaceAllButton = eventuallyNonNull();
   public static String FindView_HighlightAll = eventuallyNonNull();
   public static String FindView_MatchCase = eventuallyNonNull();
   public static String FindView_MatchRegEx = eventuallyNonNull();
   public static String FindView_MatchWholeWord = eventuallyNonNull();
   public static String FindView_FindLabel = eventuallyNonNull();

   static {
      MessagesInitializer.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {
   }
}
