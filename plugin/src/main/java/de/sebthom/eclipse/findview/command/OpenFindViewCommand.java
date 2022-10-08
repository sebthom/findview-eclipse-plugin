/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.PartInitException;

import de.sebthom.eclipse.commons.ui.Editors;
import de.sebthom.eclipse.commons.ui.UI;
import de.sebthom.eclipse.findview.SearchReplaceEngine;
import de.sebthom.eclipse.findview.ui.FindViewPart;
import net.sf.jstuff.core.Strings;

/**
 * @author Sebastian Thomschke
 */
public class OpenFindViewCommand extends AbstractHandler {

   @Override
   public @Nullable Object execute(final ExecutionEvent event) throws ExecutionException {
      final var page = UI.getActiveWorkbenchPage();
      if (page == null)
         return null;

      // open or select the Find/Replace view
      final var view = (FindViewPart) page.findView(FindViewPart.ID);
      if (view == null) {
         try {
            page.showView(FindViewPart.ID);
         } catch (final PartInitException ex) {
            throw new ExecutionException("Failed to open Find/Replace view", ex);
         }
      } else {
         page.activate(view);
      }

      final var editor = Editors.getActiveTextEditor();
      if (editor == null)
         return null;

      // if text is selected in the current editor then use it as search string
      final var selection = editor.getSelectionProvider().getSelection();
      if (selection instanceof TextSelection) {
         final var selectedText = ((TextSelection) selection).getText();
         if (selectedText != null && !selectedText.isEmpty()) {
            SearchReplaceEngine.get().searchString.set(Strings.substringBefore(selectedText, Strings.LF));
         }
      }
      return null;
   }
}
