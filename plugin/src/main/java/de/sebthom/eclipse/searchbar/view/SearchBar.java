/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.sebthom.eclipse.searchbar.Constants;
import de.sebthom.eclipse.searchbar.SearchBarPlugin;
import de.sebthom.eclipse.searchbar.localization.Messages;
import de.sebthom.eclipse.searchbar.prefs.SearchBarPreferences;
import de.sebthom.eclipse.searchbar.search.SearchEngine;
import de.sebthom.eclipse.searchbar.search.SearchResultEvent;
import de.sebthom.eclipse.searchbar.util.ui.Buttons;
import de.sebthom.eclipse.searchbar.util.ui.Keys;
import de.sebthom.eclipse.searchbar.util.ui.UI;
import net.sf.jstuff.core.concurrent.Threads;

/**
 * @author Sebastian Thomschke
 */
public final class SearchBar extends Composite {

   private final SearchBarView searchBarView;

   private Text searchText;
   private final Color searchText_defaultBG;
   private final Color searchText_defaultFG;
   private final Color searchText_noResultsBG;
   private final Color searchText_noResultsFG;

   private Button btnPrevMatch;
   private Button btnNextMatch;

   private CLabel lblInfoMessage;

   private volatile boolean mnemonicsVisible = true;
   private SearchEngine searchEngine;

   public SearchBar(final Composite parent, final SearchBarView searchBarView, final SearchEngine searchEngine) {
      super(parent, SWT.NONE);

      this.searchBarView = searchBarView;
      this.searchEngine = searchEngine;

      final var contentArea = this;
      contentArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      final var filterLayout = new GridLayout(10, false);
      filterLayout.marginHeight = 0;
      filterLayout.marginWidth = 0;
      contentArea.setLayout(filterLayout);

      final var closeButton = new Button(contentArea, SWT.PUSH);
      closeButton.setImage(SearchBarPlugin.getSharedImage(Constants.IMAGE_CLOSE_VIEW));
      Buttons.onSelected(closeButton, () -> UI.getActivePage().hideView(searchBarView));
      closeButton.setLayoutData(new GridData(20, 20));

      final var label = new Label(contentArea, SWT.NONE);
      label.setText(Messages.SearchBar_FindLabel);

      createSearchTextField(contentArea);
      searchText_defaultBG = searchText.getBackground();
      searchText_defaultFG = searchText.getForeground();
      final var display = searchText.getShell().getDisplay();
      searchText_noResultsBG = display.getSystemColor(SWT.COLOR_RED);
      searchText_noResultsFG = display.getSystemColor(SWT.COLOR_WHITE);

      btnNextMatch = new Button(contentArea, SWT.PUSH | SWT.NO_FOCUS);
      btnNextMatch.setImage(SearchBarPlugin.getSharedImage(Constants.IMAGE_SEARCH_NEXT));
      btnNextMatch.setText(Messages.SearchBar_NextButton);
      btnNextMatch.setEnabled(false);
      Buttons.onSelected(btnNextMatch, searchEngine::gotoNextMatch);

      btnPrevMatch = new Button(contentArea, SWT.PUSH | SWT.NO_FOCUS);
      btnPrevMatch.setImage(SearchBarPlugin.getSharedImage(Constants.IMAGE_SEARCH_PREV));
      btnPrevMatch.setText(Messages.SearchBar_PrevButton);
      btnPrevMatch.setEnabled(false);
      Buttons.onSelected(btnPrevMatch, searchEngine::gotoPreviousMatch);

      final var btnMatchCase = new Button(contentArea, SWT.CHECK | SWT.NO_FOCUS);
      btnMatchCase.setText(Messages.SearchBar_MatchCase);
      btnMatchCase.setSelection(SearchBarPreferences.isMatchCase());
      Buttons.onSelected(btnMatchCase, () -> {
         SearchBarPreferences.setMatchCase(btnMatchCase.getSelection());
         SearchBarPreferences.save();
         searchEngine.runSearch();
      });

      final var btnHighlightAll = new Button(contentArea, SWT.CHECK | SWT.NO_FOCUS);
      btnHighlightAll.setText(Messages.SearchBar_HighlightAll);
      btnHighlightAll.setSelection(SearchBarPreferences.isHighlightAll());
      Buttons.onSelected(btnHighlightAll, () -> {
         SearchBarPreferences.setHighlightAll(btnHighlightAll.getSelection());
         SearchBarPreferences.save();
         if (btnHighlightAll.getSelection()) {
            searchEngine.markAll();
         } else {
            searchEngine.unmarkResults();
         }
      });

      // using CLabel which supports displaying image and text at the same time
      lblInfoMessage = new CLabel(contentArea, SWT.HORIZONTAL);
      lblInfoMessage.setText(Messages.SearchBar_PhraseNotFound);
      lblInfoMessage.setVisible(false);

      final var gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.horizontalIndent = 8;
      gridData.horizontalSpan = 2;
      gridData.widthHint = 200;
      lblInfoMessage.setLayoutData(gridData);

      searchEngine.addListener(this::onSearchResultEvent);
   }

   private void createSearchTextField(final Composite parent) {
      searchText = new Text(parent, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.FLAT);

      Keys.onKeyPressed(searchText, ev -> {
         switch (ev.keyCode) {
            case SWT.ESC:
               if (SearchBarPreferences.isCloseWithEsc()) {
                  UI.getActivePage().hideView(searchBarView);
                  break;
               }

               final var editor = UI.getActiveEditor();
               if (editor != null) {
                  editor.setFocus();
               }
               break;
            case SWT.CR:
               searchEngine.gotoNextMatch();
               break;
         }
      });

      searchText.addFocusListener(new FocusAdapter() {
         @Override
         public void focusGained(final FocusEvent e) {
            showMnemonics();

            /*
             * Running in an asyncExec because the selectAll() does not appear to work when using mouse to
             * give focus to text.
             */
            final var display = searchText.getDisplay();
            display.asyncExec(() -> {
               if (!searchText.isDisposed()) {
                  searchText.selectAll();
               }
            });
         }
      });

      searchText.addModifyListener(ev -> searchEngine.runSearch());

      final var gc = new GC(searchText);
      final var fm = gc.getFontMetrics();
      final var width = 30 /*chars*/ * (int) fm.getAverageCharacterWidth();
      final var height = fm.getHeight();
      gc.dispose();

      searchText.setLayoutData(new GridData(width, height));
   }

   Text getSearchTextField() {
      return searchText;
   }

   private void onSearchResultEvent(final SearchResultEvent resultEvent) {
      if (isDisposed())
         return;

      // no search pattern entered
      if (resultEvent.occurrencesCount < 0) {
         searchText.setBackground(searchText_defaultBG);
         searchText.setForeground(searchText_defaultFG);

         lblInfoMessage.setVisible(false);

         setControlsEnabled(false);
         return;
      }

      if (resultEvent.occurrencesCount == 0) {
         searchText.setBackground(searchText_noResultsBG);
         searchText.setForeground(searchText_noResultsFG);

         lblInfoMessage.setImage(SearchBarPlugin.getSharedImage(Constants.IMAGE_INFO));
         lblInfoMessage.setText(Messages.SearchBar_PhraseNotFound);
         lblInfoMessage.setVisible(true);
      } else if (/* noResultState && */resultEvent.occurrencesCount != 0) {
         searchText.setBackground(searchText_defaultBG);
         searchText.setForeground(searchText_defaultFG);

         lblInfoMessage.setImage(null);
         lblInfoMessage.setText(resultEvent.occurrencesCount + " matches");
         lblInfoMessage.setVisible(true);
      }

      setControlsEnabled(true);
   }

   private void setControlsEnabled(final boolean isEnabled) {
      btnPrevMatch.setEnabled(isEnabled);
      btnNextMatch.setEnabled(isEnabled);
   }

   @Override
   public boolean setFocus() {
      searchText.selectAll();
      return searchText.setFocus();
   }

   private void showMnemonics() {
      if (mnemonicsVisible)
         return;

      Keys.sendKeyDown(SWT.ALT);
      Threads.sleep(50);
      Keys.sendKeyUp(SWT.ALT);
      Threads.sleep(50);
      Keys.sendKeyDown(SWT.ESC);
      Threads.sleep(50);
      Keys.sendKeyUp(SWT.ESC);

      mnemonicsVisible = true;
   }

   @Override
   public void dispose() {
      searchEngine.removeListener(this::onSearchResultEvent);
      super.dispose();
   }
}
