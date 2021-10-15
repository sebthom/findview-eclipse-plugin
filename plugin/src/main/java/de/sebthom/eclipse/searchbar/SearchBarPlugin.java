/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import de.sebthom.eclipse.searchbar.search.SearchEngine;
import net.sf.jstuff.core.Strings;
import net.sf.jstuff.core.logging.Logger;
import net.sf.jstuff.core.reflection.Fields;

/**
 * @author Sebastian Thomschke
 */
public class SearchBarPlugin extends AbstractUIPlugin {

   private static final Logger LOG = Logger.create();

   /**
    * during runtime you can get ID with getBundle().getSymbolicName()
    */
   public static final String PLUGIN_ID = SearchBarPlugin.class.getPackage().getName();

   /**
    * the shared instance
    */
   private static SearchBarPlugin instance;

   /**
    * @return the shared instance
    */
   public static SearchBarPlugin getDefault() {
      if (instance == null) {
         LOG.error("Default plugin instance is still null.", new Throwable());
      }
      return instance;
   }

   public static ImageDescriptor getImageDescriptor(final String path) {
      return imageDescriptorFromPlugin(PLUGIN_ID, path);
   }

   public static Image getSharedImage(final String path) {
      return instance == null ? null : instance.getImageRegistry().get(path);
   }

   private IPersistentPreferenceStore preferenceStore;

   @Override
   public IPersistentPreferenceStore getPreferenceStore() {
      if (preferenceStore == null) {
         preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
      }
      return preferenceStore;
   }

   @Override
   protected void initializeImageRegistry(final ImageRegistry registry) {
      for (final var field : Constants.class.getFields()) {
         if (Fields.isStatic(field) && field.getType() == String.class && field.getName().startsWith("IMAGE_")) {
            registerImage(registry, Fields.read(null, field));
         }
      }
   }

   private void registerImage(final ImageRegistry registry, final String path) {
      if (path.startsWith("platform:/plugin/")) {
         final var pluginId = Strings.substringBetween(path, "platform:/plugin/", "/");
         final var imagePath = Strings.substringAfter(path, pluginId);
         registry.put(path, AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imagePath));
         return;
      }
      final var url = FileLocator.find(getBundle(), new Path(path), null);
      final var desc = ImageDescriptor.createFromURL(url);
      registry.put(path, desc);
   }

   @Override
   public void start(final BundleContext context) throws Exception {
      getLog().info("starting...");
      super.start(context);
      instance = this;

   }

   @Override
   public void stop(final BundleContext context) throws Exception {
      SearchEngine.INSTANCE.unmarkResults();

      getLog().info("stopping...");
      instance = null;
      super.stop(context);
   }
}
