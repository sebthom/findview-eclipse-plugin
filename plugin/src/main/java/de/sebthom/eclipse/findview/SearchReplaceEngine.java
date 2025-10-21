/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview;

import static net.sf.jstuff.core.validation.NullAnalysisHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import de.sebthom.eclipse.commons.text.FindReplaceTarget;
import de.sebthom.eclipse.commons.text.Markers;
import de.sebthom.eclipse.commons.ui.Editors;
import de.sebthom.eclipse.commons.ui.UI;
import de.sebthom.eclipse.commons.ui.listener.PageListener;
import de.sebthom.eclipse.findview.prefs.PluginPreferences;
import net.sf.jstuff.core.Strings;
import net.sf.jstuff.core.collection.CollectionUtils;
import net.sf.jstuff.core.ref.MutableObservableRef;

/**
 * @author Sebastian Thomschke
 */
public class SearchReplaceEngine {

   private enum Direction {
      FORWARD,
      BACKWARD
   }

   public static final int NOT_FOUND = -1;

   private static final ConcurrentMap<IWorkbenchWindow, SearchReplaceEngine> INSTANCES_BY_WINDOW = new ConcurrentHashMap<>();

   public static SearchReplaceEngine get() {
      return INSTANCES_BY_WINDOW.computeIfAbsent(asNonNull(UI.getActiveWorkbenchWindow()), window -> {

         final var engine = new SearchReplaceEngine();

         window.addPageListener(new PageListener() {
            @Override
            public void pageClosed(final IWorkbenchPage page) {
               INSTANCES_BY_WINDOW.remove(window);
               engine.shutdown();
            }
         });

         return engine;
      });
   }

   private final Markers markers = new Markers("de.sebthom.eclipse.findview.defaultMarker");

   public final MutableObservableRef<String> searchString = MutableObservableRef.of("");
   public final MutableObservableRef<String> replaceWithString = MutableObservableRef.of("");
   public final MutableObservableRef<List<Position>> matches = MutableObservableRef.of(Collections.emptyList());

   public SearchReplaceEngine() {
      searchString.subscribe(this::search);
      PluginPreferences.addListener(this::onPreferencesChanged);
   }

   private void addMarkers() {
      final var editor = Editors.getActiveTextEditor();
      if (editor == null) {
         matches.set(Collections.emptyList());
         return;
      }
      final var annoModels = Editors.getAnnotationModel(editor);
      if (annoModels == null) {
         matches.set(Collections.emptyList());
         return;
      }
      markers.setMarkers(annoModels, matches.get(), new NullProgressMonitor());
   }

   public synchronized void gotoNextMatch() {
      selectNextMatch(FindReplaceTarget.get(), Direction.FORWARD);
   }

   public synchronized void gotoPreviousMatch() {
      selectNextMatch(FindReplaceTarget.get(), Direction.BACKWARD);
   }

   private synchronized void onPreferencesChanged(final PropertyChangeEvent ev) {
      switch (ev.getProperty()) {
         case PluginPreferences.PREF_HIGHLIGHT_ALL:
            if (PluginPreferences.isHighlightAll()) {
               addMarkers();
            } else {
               removeMarkers();
            }
            break;
         case PluginPreferences.PREF_MATCH_CASE:
         case PluginPreferences.PREF_MATCH_REGEX:
         case PluginPreferences.PREF_MATCH_WHOLEWORD:
            search();
            break;
         default:
            // nothing to do
      }
   }

   public synchronized void removeMarkers() {
      markers.removeMarkers();
   }

   public synchronized void replaceAll() {
      final var editor = Editors.getActiveTextEditor();
      if (editor == null)
         return;

      final var doc = Editors.getDocument(editor);
      if (doc == null)
         return;

      final var replaceWith = replaceWithString.get();
      for (final var match : CollectionUtils.reverse(matches.get())) {
         try {
            doc.replace(match.getOffset(), match.getLength(), replaceWith);
         } catch (final BadLocationException ex) {
            Plugin.log().error(ex);
         }
      }

      search();
   }

   public synchronized void replaceCurrentSelection() {
      final var editor = Editors.getActiveTextEditor();
      if (editor == null)
         return;

      final var doc = Editors.getDocument(editor);
      if (doc == null)
         return;

      final var selProvider = editor.getSelectionProvider();
      final var selection = selProvider.getSelection();
      if (!(selection instanceof ITextSelection sel))
         return;
      if (sel.isEmpty() || sel.getLength() == 0)
         return;

      try {
         markers.removeMarkerAt(sel);
         final var replacement = replaceWithString.get();
         doc.replace(sel.getOffset(), sel.getLength(), replacement);
         selProvider.setSelection(new TextSelection(sel.getOffset() + replacement.length(), 0));
      } catch (final BadLocationException ex) {
         Plugin.log().error(ex);
      }
   }

   public synchronized boolean replaceNextMatch() {
      return replaceNextMatch(FindReplaceTarget.get(), Direction.FORWARD);
   }

   private boolean replaceNextMatch(final @Nullable FindReplaceTarget target, final Direction direction) {
      if (target == null || selectNextMatch(target, direction) == NOT_FOUND)
         return false;

      target.replaceSelection(replaceWithString.get());
      return true;
   }

   public synchronized boolean replacePreviousMatch() {
      return replaceNextMatch(FindReplaceTarget.get(), Direction.BACKWARD);
   }

   private synchronized void search() {
      final var searchString = this.searchString.get();
      if (Strings.isEmpty(searchString)) {
         matches.set(Collections.emptyList());
         return;
      }

      final var doc = Editors.getActiveDocument();
      if (doc == null) {
         matches.set(Collections.emptyList());
         return;
      }

      final var content = doc.get();
      if (Strings.isEmpty(content)) {
         matches.set(Collections.emptyList());
         return;
      }

      final List<Position> newMatches = new ArrayList<>();

      final String regEx;
      if (PluginPreferences.isMatchRegEx()) {
         regEx = searchString;
      } else {
         if (PluginPreferences.isMatchWholeWord()) {
            regEx = "\\b" + Pattern.quote(searchString) + "\\b";
         } else {
            regEx = Pattern.quote(searchString);
         }
      }

      // Pattern is fast: https://www.baeldung.com/java-case-insensitive-string-matching
      final var pattern = Pattern.compile(regEx, PluginPreferences.isMatchCase() ? 0 : Pattern.CASE_INSENSITIVE);
      final var matcher = pattern.matcher(content);
      while (matcher.find()) {
         newMatches.add(new Position(matcher.start(), matcher.end() - matcher.start()));
      }

      matches.set(newMatches);

      if (PluginPreferences.isHighlightAll()) {
         addMarkers();
      }
   }

   private int selectNextMatch(final @Nullable FindReplaceTarget target, final Direction direction) {
      if (target == null)
         return NOT_FOUND;

      final var selPos = target.getSelection();

      final var selOffset = selPos.x;
      final var selLength = selPos.y;

      var findReplacePosition = selOffset;

      switch (direction) {
         case FORWARD:
            findReplacePosition += selLength;
            break;
         case BACKWARD:
            findReplacePosition -= selLength;
            break;
      }

      var index = target.findAndSelect( //
         findReplacePosition, //
         searchString.get(), //
         direction == Direction.FORWARD, //
         PluginPreferences.isMatchCase(), //
         PluginPreferences.isMatchWholeWord(), //
         PluginPreferences.isMatchRegEx() //
      );

      // if not found starting from current selection then start at document begin/end again
      if (index == NOT_FOUND) {
         index = target.findAndSelect( //
            -1, //
            searchString.get(), //
            direction == Direction.FORWARD, //
            PluginPreferences.isMatchCase(), //
            PluginPreferences.isMatchWholeWord(), //
            PluginPreferences.isMatchRegEx() //
         );
      }

      return index;
   }

   private void shutdown() {
      searchString.unsubscribe(this::search);
      PluginPreferences.removeListener(this::onPreferencesChanged);
      removeMarkers();
   }
}
