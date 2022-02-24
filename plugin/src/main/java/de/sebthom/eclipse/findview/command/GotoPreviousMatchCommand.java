/*
 * Copyright 2021-2022 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.sebthom.eclipse.findview.SearchReplaceEngine;

/**
 * @author Sebastian Thomschke
 */
public final class GotoPreviousMatchCommand extends AbstractHandler {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {
      SearchReplaceEngine.get().gotoPreviousMatch();
      return null;
   }
}
