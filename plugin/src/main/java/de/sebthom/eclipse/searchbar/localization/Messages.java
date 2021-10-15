/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.localization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.osgi.util.NLS;

import de.sebthom.eclipse.searchbar.util.LOG;
import net.sf.jstuff.core.Strings;
import net.sf.jstuff.core.io.IOUtils;
import net.sf.jstuff.core.reflection.Fields;

/**
 * @author Sebastian Thomschke
 */
public final class Messages extends NLS {

   private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages";

   // Keys with default values directly assigned in this class are only used by Java classes.
   // Keys without default values are loaded from messages.properties, because they are also referenced in plugin.xml

   // CHECKSTYLE:IGNORE .* FOR NEXT 100 LINES

   public static String SearchBar_PhraseNotFound;
   public static String SearchBar_NextButton;
   public static String SearchBar_PrevButton;
   public static String SearchBar_HighlightAll;
   public static String SearchBar_MatchCase;
   public static String SearchBar_FindLabel;

   static {
      //NLS.initializeMessages(BUNDLE_NAME, Messages.class); // does not support field default values
      initMessages();
   }

   private static void initMessages() {
      /*
       * calculate names of message properties files based on current locale
       */
      final List<String> messagePropFiles = new ArrayList<>(4);
      {
         final var root = BUNDLE_NAME.replace('.', '/');
         var locale = Locale.getDefault().toString();
         while (true) {
            messagePropFiles.add(root + '_' + locale + ".properties");
            if (locale.lastIndexOf('_') == -1) {
               break;
            }
            locale = Strings.substringBeforeLast(locale, "_");
         }
         messagePropFiles.add(root + ".properties");
         Collections.reverse(messagePropFiles);
      }

      /*
       * load values from all .properties files
       */
      final var messageProps = new Properties();
      for (final String variant : messagePropFiles) {
         try (var input = Messages.class.getClassLoader().getResourceAsStream(variant)) {
            if (input != null) {
               messageProps.load(input);
            }
         } catch (final IOException ex) {
            LOG.error(ex, "Error loading {0}", variant);
         }
      }

      /*
       * load plugin.xml as String to later check for references to message properties
       */
      var pluginXml = "";
      try (var is = Messages.class.getClassLoader().getResourceAsStream("/plugin.xml")) {
         pluginXml = IOUtils.toString(is);
      } catch (final IOException ex) {
         LOG.error(ex);
      }

      /*
       * apply message properties to fields
       */
      for (final var entry : messageProps.entrySet()) {
         final var fieldName = entry.getKey().toString();
         final var field = Fields.find(Messages.class, fieldName);
         if (field == null) {
            if (!pluginXml.contains("\"%" + fieldName + "\"")) {
               LOG.warn("Unused message property: " + fieldName);
            }
            continue;
         }

         try {
            Fields.write(null, field, entry.getValue());
         } catch (final Exception ex) {
            LOG.error(ex);
         }
      }

      /*
       * ensure all fields are set
       */
      for (final var field : Messages.class.getFields()) {
         if (!Fields.isStatic(field)) {
            continue;
         }
         if (Fields.read(null, field) == null) {
            LOG.error("Uninizialied message property: " + field.getName());
         }
      }
   }

   private Messages() {
   }
}
