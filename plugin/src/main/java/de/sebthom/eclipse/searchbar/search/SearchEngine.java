/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.search;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IWorkbenchPart;

import de.sebthom.eclipse.searchbar.prefs.SearchBarPreferences;
import de.sebthom.eclipse.searchbar.util.ui.Editors;
import de.sebthom.eclipse.searchbar.util.ui.Markers;
import de.sebthom.eclipse.searchbar.util.ui.UI;
import de.sebthom.eclipse.searchbar.view.SearchBarView;
import net.sf.jstuff.core.Strings;
import net.sf.jstuff.core.validation.Args;

/**
 * @author Sebastian Thomschke
 */
public final class SearchEngine {

   private static final class SearchState {
      private IAnnotationModel annotationModel;
      private ArrayList<Position> matches;
      private String searchPattern;
      private String previousSearchPattern = "";
      private boolean isNoResults;
   }

   public static final SearchEngine INSTANCE = new SearchEngine();

   private static final Markers MARKERS = new Markers();

   private static final ArrayList<Position> EMPTY_RESULT = new ArrayList<>(0);

   private final SearchState searchState = new SearchState();
   private SearchBarView searchBarView;
   private final Set<Consumer<SearchResultEvent>> listeners = ConcurrentHashMap.newKeySet();

   private IDocument currentDoc;

   public void addListener(final Consumer<SearchResultEvent> listener) {
      listeners.add(listener);
   }

   private void beepIfRequired(final SearchState searchState) {
      if (searchState.previousSearchPattern.length() > searchState.searchPattern.length())
         return;

      if (searchState.isNoResults) {
         searchBarView.getSite().getShell().getDisplay().beep();
      }
   }

   private boolean checkState(final boolean isTyping) {

      if (searchState.searchPattern.length() == 0) {
         fireResultEvent(SearchResultEvent.NO_SEARCH_PATTERN);
         searchState.matches = EMPTY_RESULT;
         searchState.isNoResults = false;
         if (SearchBarPreferences.isHighlightAll()) {
            MARKERS.removeMarkers();
         }
         return false;
      }

      if (isTyping && searchState.isNoResults && searchState.searchPattern.contains(searchState.previousSearchPattern)) {
         if (SearchBarPreferences.isHighlightAll()) {
            MARKERS.removeMarkers();
         }
         fireResultEvent(SearchResultEvent.ZERO_FOUND);
         return false;
      }

      return true;
   }

   private Position find(final String searchIn, final String searchFor, int startAt, int endAt, final boolean ignoreCase) {
      Args.notNull("searchIn", searchIn);
      Args.notNull("searchFor", searchFor);

      final var searchInLen = searchIn.length();

      if (startAt < 0) {
         startAt = 0;
      }
      if (endAt > searchInLen) {
         endAt = searchInLen;
      }

      if (endAt < 0 || startAt >= endAt)
         return null;

      final var searchForLen = searchFor.length();
      if (searchForLen == 0)
         return new Position(startAt, 0);

      final var foundAt = ignoreCase //
         ? Strings.indexOfIgnoreCase(searchIn, searchFor, startAt) //
         : searchIn.indexOf(searchFor, startAt);

      final var max = endAt - searchForLen;
      if (foundAt == -1 || foundAt > max)
         return null;
      return new Position(foundAt, searchForLen);
   }

   private ArrayList<Position> findAll(final String searchIn, final String searchFor, int startAt, final boolean ignoreCase) {
      Position position;
      final var result = new ArrayList<Position>();
      while ((position = find(searchIn, searchFor, startAt, searchIn.length(), ignoreCase)) != null) {
         result.add(position);
         startAt = position.getOffset() + position.getLength();
      }
      return result;
   }

   private int findAndSelect(final IFindReplaceTarget target, final boolean goForward, final boolean stepRequired) {
      if (target == null)
         return -1;

      final var pos = target.getSelection();
      if (pos == null)
         return -1;

      var findReplacePosition = pos.x;
      if (goForward) {
         if (stepRequired) {
            findReplacePosition += pos.y;
         }
      } else {
         findReplacePosition -= pos.y;
      }

      var index = target.findAndSelect( //
         findReplacePosition, //
         searchState.searchPattern, //
         goForward, //
         SearchBarPreferences.isMatchCase(), //
         false //
      );

      if (index == -1) {
         index = target.findAndSelect(-1, searchState.searchPattern, goForward, SearchBarPreferences.isMatchCase(), false);
      }
      return index;
   }

   private void fireResultEvent(final SearchResultEvent searchEvent) {
      for (final var l : listeners) {
         l.accept(searchEvent);
      }
   }

   private IFindReplaceTarget getFindReplaceTarget() {
      final IWorkbenchPart editor = UI.getActiveTextEditor();
      return editor == null //
         ? null //
         : (IFindReplaceTarget) editor.getAdapter(IFindReplaceTarget.class);
   }

   private void gotoMatch(final boolean goForward, final boolean stepRequired) {
      final var index = findAndSelect(getFindReplaceTarget(), goForward, stepRequired);
      searchState.isNoResults = index == -1;
      beepIfRequired(searchState);

      final var editor = UI.getActiveTextEditor();
      final var activeDoc = Editors.getDocument(editor);

      fireResultEvent(activeDoc == null || searchState.isNoResults //
         ? SearchResultEvent.ZERO_FOUND
         : new SearchResultEvent(findAll(activeDoc.get(), searchState.searchPattern, 0, !SearchBarPreferences.isMatchCase()).size()) //
      );

      if (activeDoc != currentDoc && SearchBarPreferences.isHighlightAll()) {
         searchState.searchPattern = searchBarView.getSearchString();
         markAll();
         currentDoc = activeDoc;
      }
   }

   public void gotoNextMatch() {
      searchState.searchPattern = searchBarView.getSearchString();
      if (!checkState(false))
         return;
      gotoMatch(true, true);
   }

   public void gotoPreviousMatch() {
      searchState.searchPattern = searchBarView.getSearchString();
      if (!checkState(false))
         return;
      gotoMatch(false, true);
   }

   public void markAll() {
      final var editor = UI.getActiveTextEditor();
      if (editor == null) {
         fireResultEvent(SearchResultEvent.ZERO_FOUND);
         return;
      }
      searchState.annotationModel = Editors.getAnnotationModel(editor);
      MARKERS.removeMarkers();

      final var doc = Editors.getDocument(editor);
      final var searchResult = findAll(doc.get(), searchState.searchPattern, 0, !SearchBarPreferences.isMatchCase());
      searchState.matches = searchResult;
      searchState.isNoResults = searchState.matches.isEmpty();

      if (searchState.isNoResults) {
         fireResultEvent(SearchResultEvent.ZERO_FOUND);
         return;
      }

      MARKERS.setMarkers(searchState.annotationModel, searchState.matches, new NullProgressMonitor());
      fireResultEvent(new SearchResultEvent(searchState.matches.size()));
   }

   public void removeListener(final Consumer<SearchResultEvent> listener) {
      listeners.remove(listener);
   }

   public void runSearch() {
      searchState.searchPattern = searchBarView.getSearchString();

      if (!checkState(true)) {
         searchState.previousSearchPattern = searchState.searchPattern;
         beepIfRequired(searchState);
         return;
      }

      if (SearchBarPreferences.isHighlightAll()) {
         markAll();
      }

      gotoMatch(true, false);

      searchState.previousSearchPattern = searchState.searchPattern;
   }

   public void setView(final SearchBarView searchBarView) {
      this.searchBarView = searchBarView;
   }

   public void unmarkResults() {
      MARKERS.removeMarkers();
   }
}
