/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview;

import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.BundleContext;

import de.sebthom.eclipse.commons.AbstractEclipsePlugin;
import de.sebthom.eclipse.commons.BundleResources;
import de.sebthom.eclipse.commons.logging.PluginLogger;
import de.sebthom.eclipse.commons.logging.StatusFactory;
import de.sebthom.eclipse.findview.ui.ActiveTextEditorTracker;
import net.sf.jstuff.core.reflection.Fields;
import net.sf.jstuff.core.validation.Assert;

/**
 * @author Sebastian Thomschke
 */
public class Plugin extends AbstractEclipsePlugin {

   /**
    * during runtime you can get ID with getBundle().getSymbolicName()
    */
   public static final String PLUGIN_ID = Plugin.class.getPackage().getName();

   private static Plugin instance;

   /**
    * @return the shared instance
    */
   public static Plugin get() {
      Assert.notNull(instance, "Default plugin instance is still null.");
      return instance;
   }

   public static PluginLogger log() {
      return get().getLogger();
   }

   public static BundleResources resources() {
      return get().getBundleResources();
   }

   public static StatusFactory status() {
      return get().getStatusFactory();
   }

   @Override
   public BundleResources getBundleResources() {
      if (bundleResources == null) {
         bundleResources = new BundleResources(this, "src/main/resources");
      }
      return bundleResources;
   }

   @Override
   protected void initializeImageRegistry(final ImageRegistry registry) {
      for (final var field : Constants.class.getFields()) {
         if (Fields.isStatic(field) && field.getType() == String.class && field.getName().startsWith("IMAGE_")) {
            registerImage(registry, Fields.read(null, field));
         }
      }
   }

   @Override
   public void start(final BundleContext context) throws Exception {
      super.start(context);
      instance = this;

      ActiveTextEditorTracker.install();
   }

   @Override
   public void stop(final BundleContext context) throws Exception {
      ActiveTextEditorTracker.uninstall();

      instance = null;
      super.stop(context);
   }
}
