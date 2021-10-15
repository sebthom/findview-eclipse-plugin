/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.search;

/**
 * @author Sebastian Thomschke
 */
public final class SearchResultEvent {

   public static final SearchResultEvent NO_SEARCH_PATTERN = new SearchResultEvent(-1);
   public static final SearchResultEvent ZERO_FOUND = new SearchResultEvent(0);

   /**
    * -1 means no search pattern specified
    */
   public final int occurrencesCount;

   SearchResultEvent(final int occurrencesCount) {
      this.occurrencesCount = occurrencesCount;
   }
}
