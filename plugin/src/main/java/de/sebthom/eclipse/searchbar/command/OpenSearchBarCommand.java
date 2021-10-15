/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.PartInitException;

import de.sebthom.eclipse.searchbar.util.ui.UI;
import de.sebthom.eclipse.searchbar.view.SearchBarView;
import net.sf.jstuff.core.Strings;

/**
 * @author Sebastian Thomschke
 */
public class OpenSearchBarCommand extends AbstractHandler {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {
      final var page = UI.getActivePage();
      if (page == null)
         return null;

      // open or select the search view
      var view = (SearchBarView) page.findView(SearchBarView.ID);
      if (view == null) {
         try {
            view = (SearchBarView) page.showView(SearchBarView.ID);
         } catch (final PartInitException ex) {
            throw new ExecutionException("Failed to open Search Bar view", ex);
         }
      } else {
         page.activate(view);
      }

      final var editor = UI.getActiveTextEditor();
      if (editor == null)
         return null;

      // if text is selected in the current editor then use it as search string
      final var selection = editor.getSelectionProvider().getSelection();
      if (selection instanceof TextSelection) {
         final var selectedText = ((TextSelection) selection).getText();
         if (Strings.isNotEmpty(selectedText)) {
            view.setSearchString(Strings.substringBefore(selectedText, Strings.LF));
         }
      }
      return null;
   }
}
