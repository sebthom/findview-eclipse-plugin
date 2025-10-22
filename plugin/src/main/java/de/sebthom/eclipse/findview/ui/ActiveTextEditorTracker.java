/*
 * SPDX-FileCopyrightText: © Sebastian Thomschke and contributors.
 * SPDX-FileContributor: Sebastian Thomschke
 * SPDX-License-Identifier: EPL-2.0
 * SPDX-ArtifactOfProjectHomePage: https://github.com/sebthom/findview-eclipse-plugin
 */
package de.sebthom.eclipse.findview.ui;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import de.sebthom.eclipse.commons.ui.Editors;
import de.sebthom.eclipse.commons.ui.listener.WindowListener;
import net.sf.jstuff.core.ref.MutableObservableRef;

/**
 * @author Sebastian Thomschke
 */
public final class ActiveTextEditorTracker implements IPartListener2, WindowListener {

   public static final MutableObservableRef<@Nullable ITextEditor> ACTIVE_TEXT_EDITOR = MutableObservableRef.ofNullable(null);

   private static final ActiveTextEditorTracker INSTANCE = new ActiveTextEditorTracker();

   public static void install() {
      PlatformUI.getWorkbench().addWindowListener(INSTANCE);
      for (final var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
         window.getPartService().addPartListener(INSTANCE);
      }
   }

   public static void uninstall() {
      PlatformUI.getWorkbench().removeWindowListener(INSTANCE);
      for (final var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
         window.getPartService().removePartListener(INSTANCE);
      }
   }

   @Override
   public void windowOpened(final IWorkbenchWindow window) {
      window.getPartService().addPartListener(this);
   }

   @Override
   public void windowClosed(final IWorkbenchWindow window) {
      window.getPartService().removePartListener(this);
   }

   @Override
   public void partActivated(final IWorkbenchPartReference partRef) {
      ACTIVE_TEXT_EDITOR.set(Editors.getActiveTextEditor());
   }

   @Override
   public void partDeactivated(final IWorkbenchPartReference partRef) {
      ACTIVE_TEXT_EDITOR.set(Editors.getActiveTextEditor());
   }
}
