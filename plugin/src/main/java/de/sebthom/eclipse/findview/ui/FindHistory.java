/*
 * SPDX-FileCopyrightText: © Sebastian Thomschke and contributors.
 * SPDX-FileContributor: Sebastian Thomschke
 * SPDX-License-Identifier: EPL-2.0
 * SPDX-ArtifactOfProjectHomePage: https://github.com/sebthom/findview-eclipse-plugin
 */
package de.sebthom.eclipse.findview.ui;

import static net.sf.jstuff.core.validation.NullAnalysisHelper.lateNonNull;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.sebthom.eclipse.commons.ui.Buttons;
import de.sebthom.eclipse.findview.prefs.PluginPreferences;
import net.sf.jstuff.core.Strings;

/**
 * @author Sebastian Thomschke
 */
public final class FindHistory extends Composite {

   public static final class HistoryEntry {
      static HistoryEntry deserialize(final String line) {
         final var parts = line.split("\\|", -1);
         final UnaryOperator<String> dec = (final String s) -> URLDecoder.decode(s, StandardCharsets.UTF_8);
         final var find = parts.length > 0 ? dec.apply(parts[0]) : "";
         final var replace = parts.length > 1 ? dec.apply(parts[1]) : "";
         final var mc = parts.length > 2 && "1".equals(parts[2]);
         final var mw = parts.length > 3 && "1".equals(parts[3]);
         final var re = parts.length > 4 && "1".equals(parts[4]);
         final var ha = parts.length > 5 && "1".equals(parts[5]);
         final var pi = parts.length > 6 && "1".equals(parts[6]);
         return new HistoryEntry(find, replace, mc, mw, re, ha, pi);
      }

      public final String find;
      public final String replace;
      public final boolean matchCase;
      public final boolean matchWholeWord;
      public final boolean matchRegEx;
      public final boolean highlightAll;
      public boolean pinned;

      public HistoryEntry(final String find, final String replace, final boolean matchCase, final boolean matchWholeWord,
            final boolean matchRegEx, final boolean highlightAll) {
         this(find, replace, matchCase, matchWholeWord, matchRegEx, highlightAll, false);
      }

      public HistoryEntry(final String find, final String replace, final boolean matchCase, final boolean matchWholeWord,
            final boolean matchRegEx, final boolean highlightAll, final boolean pinned) {
         this.find = find;
         this.replace = replace;
         this.matchCase = matchCase;
         this.matchWholeWord = matchWholeWord;
         this.matchRegEx = matchRegEx;
         this.highlightAll = highlightAll;
         this.pinned = pinned;
      }

      @Override
      public boolean equals(final @Nullable Object obj) {
         if (this == obj)
            return true;
         if (!(obj instanceof final HistoryEntry other))
            return false;
         return matchCase == other.matchCase //
               && matchWholeWord == other.matchWholeWord //
               && matchRegEx == other.matchRegEx //
               && highlightAll == other.highlightAll //
               && find.equals(other.find) //
               && replace.equals(other.replace);
      }

      @Override
      public int hashCode() {
         int result = find.hashCode();
         result = 31 * result + replace.hashCode();
         result = 31 * result + (matchCase ? 1 : 0);
         result = 31 * result + (matchWholeWord ? 1 : 0);
         result = 31 * result + (matchRegEx ? 1 : 0);
         result = 31 * result + (highlightAll ? 1 : 0);
         return result;
      }

      String optionsDisplay() {
         final var opts = new ArrayList<String>(4);
         if (highlightAll) {
            opts.add("All");
         }
         if (matchCase) {
            opts.add("Case");
         }
         if (matchWholeWord) {
            opts.add("Word");
         }
         if (matchRegEx) {
            opts.add("RegEx");
         }
         return String.join(", ", opts);
      }

      String serialize() {
         return URLEncoder.encode(find, StandardCharsets.UTF_8) + '|' //
               + URLEncoder.encode(replace, StandardCharsets.UTF_8) + '|' //
               + (matchCase ? '1' : '0') + '|' //
               + (matchWholeWord ? '1' : '0') + '|' //
               + (matchRegEx ? '1' : '0') + '|' //
               + (highlightAll ? '1' : '0') + '|' //
               + (pinned ? '1' : '0');
      }
   }

   private static final int HISTORY_LIMIT = 50;

   private Table table = lateNonNull();
   private Button btnAdd = lateNonNull();
   private Button chkAutoAdd = lateNonNull();
   private Button btnClear = lateNonNull();
   private final List<HistoryEntry> history = new ArrayList<>();
   private final List<TableEditor> editors = new ArrayList<>();

   public FindHistory(final Composite parent, final int gridSpan, final Consumer<HistoryEntry> onLoad, final Runnable onAddRequested) {
      super(parent, SWT.NONE);

      final var gl = new GridLayout(1, false);
      gl.marginTop = 5;
      gl.marginWidth = 0;
      gl.marginHeight = 0;
      gl.verticalSpacing = 3;
      setLayout(gl);
      setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, gridSpan, 1));

      table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
      table.setHeaderVisible(true);
      table.setLinesVisible(true);
      final var tableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
      tableGD.heightHint = 120;
      table.setLayoutData(tableGD);

      final var colFind = new TableColumn(table, SWT.LEFT);
      colFind.setText("Find");
      colFind.setWidth(250);

      final var colReplace = new TableColumn(table, SWT.LEFT);
      colReplace.setText("Replace");
      colReplace.setWidth(250);

      final var colOptions = new TableColumn(table, SWT.LEFT);
      colOptions.setText("Options");
      colOptions.setWidth(180);

      final var colActions = new TableColumn(table, SWT.LEFT);
      colActions.setText("Actions");
      colActions.setWidth(140);

      table.addListener(SWT.MouseDoubleClick, ev -> {
         final int idx = table.getSelectionIndex();
         if (idx < 0 || idx >= history.size())
            return;
         onLoad.accept(history.get(idx));
      });

      final var actions = new Composite(this, SWT.NONE);
      actions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      final var glActions = new GridLayout(3, false);
      glActions.marginWidth = 0;
      glActions.marginHeight = 0;
      glActions.horizontalSpacing = 5;
      actions.setLayout(glActions);

      chkAutoAdd = new Button(actions, SWT.CHECK | SWT.NO_FOCUS);
      chkAutoAdd.setText("Auto-add to history");
      chkAutoAdd.setSelection(PluginPreferences.isHistoryAutoAdd());
      Buttons.onSelected(chkAutoAdd, () -> {
         PluginPreferences.setHistoryAutoAdd(chkAutoAdd.getSelection());
         PluginPreferences.save();
      });

      btnAdd = new Button(actions, SWT.PUSH | SWT.NO_FOCUS);
      btnAdd.setText("Add to history");
      Buttons.onSelected(btnAdd, onAddRequested);

      btnClear = new Button(actions, SWT.PUSH | SWT.NO_FOCUS);
      btnClear.setText("Clear");
      btnClear.setEnabled(false);
      Buttons.onSelected(btnClear, this::clear);

      load();
      refresh();
   }

   public void addEntry(final HistoryEntry he) {
      if (Strings.isEmpty(he.find))
         return;
      for (int i = 0; i < history.size(); i++) {
         final var existing = history.get(i);
         if (he.equals(existing)) {
            he.pinned = he.pinned || existing.pinned;
            history.remove(i);
            break;
         }
      }
      history.add(0, he);

      // enforce limit by removing oldest UNPINNED entries; never remove pinned
      int toRemove = history.size() - HISTORY_LIMIT;
      while (toRemove > 0) {
         int idx = -1;
         for (int i = history.size() - 1; i >= 0; i--) {
            if (!history.get(i).pinned) {
               idx = i;
               break;
            }
         }
         if (idx == -1) {
            break; // only pinned remain; do not remove
         }
         history.remove(idx);
         toRemove--;
      }
      persist();
      refresh();
   }

   private void clear() {
      if (history.isEmpty())
         return;
      history.removeIf(e -> !e.pinned);
      persist();
      refresh();
   }

   private void load() {
      history.clear();
      final var stored = PluginPreferences.getHistory();
      if (Strings.isEmpty(stored))
         return;
      final var lines = stored.split("\n");
      for (final var line : lines) {
         if (Strings.isEmpty(line)) {
            continue;
         }
         try {
            history.add(HistoryEntry.deserialize(line));
         } catch (final Exception ex) {
            // ignore malformed entries
         }
      }
   }

   private void persist() {
      final var b = new StringBuilder();
      for (int i = 0; i < history.size(); i++) {
         if (i > 0) {
            b.append('\n');
         }
         b.append(history.get(i).serialize());
      }
      PluginPreferences.setHistory(b.toString());
      PluginPreferences.save();
   }

   private void refresh() {
      // dispose previous editors/buttons
      for (final var ed : editors) {
         final var editorControl = ed.getEditor();
         if (editorControl != null && !editorControl.isDisposed()) {
            editorControl.dispose();
         }
         ed.dispose();
      }
      editors.clear();

      table.setRedraw(false);
      table.removeAll();

      // first show pinned, then others
      for (final var he : history) {
         if (!he.pinned) {
            continue;
         }
         createRow(he);
      }
      for (final var he : history) {
         if (he.pinned) {
            continue;
         }
         createRow(he);
      }
      table.setRedraw(true);
      boolean hasNonPinned = false;
      for (final var he : history) {
         if (!he.pinned) {
            hasNonPinned = true;
            break;
         }
      }
      btnClear.setEnabled(hasNonPinned);
   }

   private void createRow(final HistoryEntry he) {
      final var item = new TableItem(table, SWT.NONE);
      item.setText(0, he.find);
      item.setText(1, he.replace);
      item.setText(2, he.optionsDisplay());

      final var actions = new Composite(table, SWT.NONE);
      final var gl = new GridLayout(2, true);
      gl.marginWidth = 0;
      gl.marginHeight = 0;
      gl.verticalSpacing = 0;
      gl.horizontalSpacing = 2;
      actions.setLayout(gl);

      final var pinBtn = new Button(actions, SWT.PUSH | SWT.NO_FOCUS);
      pinBtn.setText(he.pinned ? "Unpin" : "Pin");
      final var pinGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
      pinGD.heightHint = Math.max(16, table.getItemHeight() - 2);
      pinBtn.setLayoutData(pinGD);
      Buttons.onSelected(pinBtn, () -> {
         he.pinned = !he.pinned;
         persist();
         refresh();
      });

      final var delBtn = new Button(actions, SWT.PUSH | SWT.NO_FOCUS);
      delBtn.setText("Delete");
      final var delGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
      delGD.heightHint = Math.max(16, table.getItemHeight() - 2);
      delBtn.setLayoutData(delGD);
      Buttons.onSelected(delBtn, () -> {
         for (int i = 0; i < history.size(); i++) {
            if (history.get(i).equals(he)) {
               history.remove(i);
               break;
            }
         }
         persist();
         refresh();
      });

      final var ed = new TableEditor(table);
      ed.grabHorizontal = true;
      ed.grabVertical = true;
      ed.horizontalAlignment = SWT.LEFT;
      ed.verticalAlignment = SWT.CENTER;
      ed.setEditor(actions, item, 3);
      editors.add(ed);
   }
}
