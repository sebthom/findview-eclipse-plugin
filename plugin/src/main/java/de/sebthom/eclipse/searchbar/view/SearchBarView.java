/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.view;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.sebthom.eclipse.searchbar.search.SearchEngine;

/**
 * @author Sebastian Thomschke
 */
public final class SearchBarView extends ViewPart {

   public static final String ID = SearchBarView.class.getName();

   private SearchBar searchBar;

   public SearchBarView() {
      SearchEngine.INSTANCE.setView(this);
   }

   @Override
   public void createPartControl(final Composite parent) {
      final var composite = new Composite(parent, SWT.NONE);
      composite.setLayout(GridLayoutFactory.fillDefaults().create());
      searchBar = new SearchBar(composite, this, SearchEngine.INSTANCE);
   }

   @Override
   public void dispose() {
      SearchEngine.INSTANCE.unmarkResults();
      searchBar.dispose();
      super.dispose();
   }

   public String getSearchString() {
      return searchBar.getSearchTextField().getText();
   }

   @Override
   public void setFocus() {
      searchBar.setFocus();
   }

   public void setSearchString(final String text) {
      searchBar.getSearchTextField().setText(text);
   }
}
