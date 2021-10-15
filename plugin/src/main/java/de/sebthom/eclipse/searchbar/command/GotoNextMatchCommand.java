/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.sebthom.eclipse.searchbar.search.SearchEngine;

/**
 * @author Sebastian Thomschke
 */
public final class GotoNextMatchCommand extends AbstractHandler {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {
      SearchEngine.INSTANCE.gotoNextMatch();
      return null;
   }
}
