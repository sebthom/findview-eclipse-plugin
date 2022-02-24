/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.ui;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import de.sebthom.eclipse.commons.ui.Buttons;
import de.sebthom.eclipse.commons.ui.Editors;
import de.sebthom.eclipse.commons.ui.Keys;
import de.sebthom.eclipse.commons.ui.Texts;
import de.sebthom.eclipse.commons.ui.UI;
import de.sebthom.eclipse.findview.Constants;
import de.sebthom.eclipse.findview.Plugin;
import de.sebthom.eclipse.findview.SearchReplaceEngine;
import de.sebthom.eclipse.findview.localization.Messages;
import de.sebthom.eclipse.findview.prefs.PluginPreferences;
import net.sf.jstuff.core.Strings;
import net.sf.jstuff.core.concurrent.Threads;

/**
 * @author Sebastian Thomschke
 */
public final class FindView extends Composite {

   final Text searchText;
   private final Color searchText_defaultBG;
   private final Color searchText_defaultFG;
   private final Color searchText_noResultsBG;
   private final Color searchText_noResultsFG;

   private final Button btnFindPrev;
   private final Button btnFindNext;
   private final Button btnMatchWholeWord;
   private final CLabel lblInfoMessage;

   private final Text replaceWithText;
   private final Button btnReplace;
   private final Button btnReplaceAll;

   private volatile boolean anyChildHasFocus = false;
   private volatile boolean mnemonicsVisible = true;

   private final SearchReplaceEngine searchReplaceEngine;

   public FindView(final Composite parent, final FindViewPart findViewPart) {
      super(parent, SWT.NONE);

      setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      searchReplaceEngine = SearchReplaceEngine.get();

      final var findViewLayout = new GridLayout();
      findViewLayout.numColumns = 8;
      findViewLayout.marginHeight = 3;
      findViewLayout.marginWidth = 3;
      findViewLayout.horizontalSpacing = 5;
      findViewLayout.verticalSpacing = 0;
      setLayout(findViewLayout);

      /* ***************************************************************
       * "Find" Row
       * ***************************************************************/
      final var closeButtonTB = new ToolBar(this, SWT.FLAT);
      final var closeButton = new ToolItem(closeButtonTB, SWT.PUSH);
      closeButton.setImage(Plugin.get().getSharedImage(Constants.IMAGE_CLOSE_VIEW));
      closeButtonTB.pack();
      Buttons.onSelected(closeButton, () -> UI.getActiveWorkbenchPage().hideView(findViewPart));

      final var lblSearchText = new Label(this, SWT.NONE);
      lblSearchText.setText(Messages.FindView_FindLabel);
      lblSearchText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

      searchText = new Text(this, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.FLAT);
      final var gc = new GC(searchText);
      final var fm = gc.getFontMetrics();
      final var gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
      gridData.widthHint = 30 /* number of chars */ * (int) fm.getAverageCharacterWidth();
      searchText.setLayoutData(gridData);
      gc.dispose();
      Texts.bind(searchText, searchReplaceEngine.searchString);
      searchText_defaultBG = searchText.getBackground();
      searchText_defaultFG = searchText.getForeground();
      searchText_noResultsBG = UI.getDisplay().getSystemColor(SWT.COLOR_RED);
      searchText_noResultsFG = UI.getDisplay().getSystemColor(SWT.COLOR_WHITE);

      Texts.onKeyPressed(searchText, ev -> {
         switch (ev.keyCode) {
            case SWT.ESC:
               if (PluginPreferences.isCloseWithEsc()) {
                  UI.getActiveWorkbenchPage().hideView(findViewPart);
                  break;
               }

               final var editor = Editors.getActiveEditor();
               if (editor != null) {
                  editor.setFocus();
               }
               break;
            case SWT.CR:
               searchReplaceEngine.gotoNextMatch();
               break;
         }
      });
      Texts.onFocused(searchText, () -> {
         showMnemonics();

         // Running in an asyncExec because the selectAll() does not appear to work when using mouse to give focus to text.
         searchText.getDisplay().asyncExec(searchText::selectAll);
      });

      btnFindNext = new Button(this, SWT.PUSH | SWT.NO_FOCUS);
      btnFindNext.setImage(Plugin.get().getSharedImage(Constants.IMAGE_ARROW_DOWN));
      btnFindNext.setText(Messages.FindView_GotoNextButton);
      btnFindNext.setEnabled(false);
      Buttons.onSelected(btnFindNext, searchReplaceEngine::gotoNextMatch);

      btnFindPrev = new Button(this, SWT.PUSH | SWT.NO_FOCUS);
      btnFindPrev.setImage(Plugin.get().getSharedImage(Constants.IMAGE_ARROW_UP));
      btnFindPrev.setText(Messages.FindView_GotoPrevButton);
      btnFindPrev.setEnabled(false);
      Buttons.onSelected(btnFindPrev, searchReplaceEngine::gotoPreviousMatch);

      final var btnHighlightAll = new Button(this, SWT.CHECK | SWT.NO_FOCUS);
      btnHighlightAll.setText(Messages.FindView_HighlightAll);
      btnHighlightAll.setSelection(PluginPreferences.isHighlightAll());
      Buttons.onSelected(btnHighlightAll, () -> {
         PluginPreferences.setHighlightAll(btnHighlightAll.getSelection());
         PluginPreferences.save();
      });

      final var btnMatchCase = new Button(this, SWT.CHECK | SWT.NO_FOCUS);
      btnMatchCase.setText(Messages.FindView_MatchCase);
      btnMatchCase.setSelection(PluginPreferences.isMatchCase());
      Buttons.onSelected(btnMatchCase, () -> {
         PluginPreferences.setMatchCase(btnMatchCase.getSelection());
         PluginPreferences.save();
      });

      /* ***************************************************************
       * Separator Row
       * ***************************************************************/
      new Label(this, SWT.NONE) //
         .setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));

      final var switchButton = new Button(this, SWT.PUSH);
      switchButton.setImage(Plugin.get().getSharedImage(Constants.IMAGE_ARROWS_UP_AND_DOWN));
      switchButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

      // using CLabel which supports displaying image and text at the same time
      lblInfoMessage = new CLabel(this, SWT.HORIZONTAL);
      lblInfoMessage.setVisible(false);
      lblInfoMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

      btnMatchWholeWord = new Button(this, SWT.CHECK | SWT.NO_FOCUS);
      btnMatchWholeWord.setEnabled(!PluginPreferences.isMatchRegEx());
      btnMatchWholeWord.setText(Messages.FindView_MatchWholeWord);
      btnMatchWholeWord.setSelection(PluginPreferences.isMatchWholeWord());
      Buttons.onSelected(btnMatchWholeWord, () -> {
         PluginPreferences.setMatchWholeWord(btnMatchWholeWord.getSelection());
         PluginPreferences.save();
      });

      final var btnMatchRegEx = new Button(this, SWT.CHECK | SWT.NO_FOCUS);
      btnMatchRegEx.setText(Messages.FindView_MatchRegEx);
      btnMatchRegEx.setSelection(PluginPreferences.isMatchRegEx());
      Buttons.onSelected(btnMatchRegEx, () -> {
         PluginPreferences.setMatchRegEx(btnMatchRegEx.getSelection());
         PluginPreferences.save();
      });

      /* ***************************************************************
       * "Replace with" Row
       * ***************************************************************/
      final var lblReplaceWith = new Label(this, SWT.NONE);
      lblReplaceWith.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      lblReplaceWith.setText("Replace with:");

      replaceWithText = new Text(this, SWT.SEARCH | SWT.ICON_CANCEL);
      replaceWithText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
      Texts.onModified(replaceWithText, searchReplaceEngine.replaceWithString::set);
      Texts.onKeyPressed(replaceWithText, ev -> {
         switch (ev.keyCode) {
            case SWT.ESC:
               if (PluginPreferences.isCloseWithEsc()) {
                  UI.getActiveWorkbenchPage().hideView(findViewPart);
                  break;
               }

               final var editor = Editors.getActiveEditor();
               if (editor != null) {
                  editor.setFocus();
               }
               break;
         }
      });

      btnReplace = new Button(this, SWT.PUSH | SWT.NO_FOCUS);
      btnReplace.setText(Messages.FindView_ReplaceButton);
      btnReplace.setEnabled(false);
      btnReplace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      Buttons.onSelected(btnReplace, searchReplaceEngine::replaceCurrentSelection);

      btnReplaceAll = new Button(this, SWT.PUSH | SWT.NO_FOCUS);
      btnReplaceAll.setText(Messages.FindView_ReplaceAllButton);
      btnReplaceAll.setEnabled(false);
      btnReplaceAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      Buttons.onSelected(btnReplaceAll, searchReplaceEngine::replaceAll);

      PluginPreferences.addListener(this::onPreferencesChanged);
      searchReplaceEngine.matches.subscribe(this::updateControlStates);

      Buttons.onSelected(switchButton, () -> {
         final var s = searchText.getText();
         final var r = replaceWithText.getText();
         searchText.setText(r);
         replaceWithText.setText(s);
      });

      getDisplay().addFilter(SWT.FocusIn, this::onAnyControlFocused);
      getDisplay().addFilter(SWT.FocusOut, this::onAnyControlLostFocused);
   }

   @Override
   public void dispose() {
      getDisplay().removeFilter(SWT.FocusIn, this::onAnyControlFocused);
      getDisplay().removeFilter(SWT.FocusOut, this::onAnyControlLostFocused);

      PluginPreferences.removeListener(this::onPreferencesChanged);
      searchReplaceEngine.matches.unsubscribe(this::updateControlStates);
      super.dispose();
   }

   private void onAnyControlFocused(final Event event) {
      if (anyChildHasFocus)
         return;

      if (!(event.widget instanceof Control))
         return;

      for (var c = (Control) event.widget; c != null; c = c.getParent()) {
         if (c == FindView.this) {
            anyChildHasFocus = true;
            updateControlStates();
            return;
         }
      }
   }

   private void onAnyControlLostFocused(final Event event) {
      if (!anyChildHasFocus)
         return;

      if (!(event.widget instanceof Control))
         return;

      for (var c = (Control) event.widget; c != null; c = c.getParent()) {
         if (c == FindView.this) {
            anyChildHasFocus = false;
            return;
         }
      }
   }

   private void onPreferencesChanged(final PropertyChangeEvent ev) {
      switch (ev.getProperty()) {
         case PluginPreferences.PREF_MATCH_REGEX:
            btnMatchWholeWord.setEnabled(!PluginPreferences.isMatchRegEx());
            break;
      }
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

   void updateControlStates() {
      if (isDisposed())
         return;

      final var editor = Editors.getActiveTextEditor();
      final var matches = searchReplaceEngine.matches.get().size();

      if (editor == null || matches == 0) {
         btnFindNext.setEnabled(false);
         btnFindPrev.setEnabled(false);
         btnReplace.setEnabled(false);
         btnReplaceAll.setEnabled(false);

         if (editor == null || Strings.isEmpty(searchReplaceEngine.searchString.get())) {
            // no editor open or no search string typed
            searchText.setBackground(searchText_defaultBG);
            searchText.setForeground(searchText_defaultFG);
            lblInfoMessage.setVisible(false);
         } else {
            searchText.setBackground(searchText_noResultsBG);
            searchText.setForeground(searchText_noResultsFG);
            lblInfoMessage.setImage(Plugin.get().getSharedImage(Constants.IMAGE_STATUS_WARN));
            lblInfoMessage.setText(Messages.FindView_NoMatchNotFound);
            lblInfoMessage.setVisible(true);
         }
      } else {
         btnFindNext.setEnabled(true);
         btnFindPrev.setEnabled(true);
         btnReplace.setEnabled(true);
         btnReplaceAll.setEnabled(true);

         searchText.setBackground(searchText_defaultBG);
         searchText.setForeground(searchText_defaultFG);

         lblInfoMessage.setImage(null);
         lblInfoMessage.setImage(Plugin.get().getSharedImage(Constants.IMAGE_STATUS_INFO));
         lblInfoMessage.setText(matches + " matches");
         lblInfoMessage.setVisible(true);
      }
   }
}
