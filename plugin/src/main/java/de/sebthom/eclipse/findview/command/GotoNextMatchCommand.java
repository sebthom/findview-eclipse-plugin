/*
 * SPDX-FileCopyrightText: © Sebastian Thomschke and contributors.
 * SPDX-FileContributor: Sebastian Thomschke
 * SPDX-License-Identifier: EPL-2.0
 * SPDX-ArtifactOfProjectHomePage: https://github.com/sebthom/findview-eclipse-plugin
 */
package de.sebthom.eclipse.findview.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;

import de.sebthom.eclipse.findview.SearchReplaceEngine;

/**
 * @author Sebastian Thomschke
 */
public final class GotoNextMatchCommand extends AbstractHandler {

   @Override
   public @Nullable Object execute(final ExecutionEvent event) throws ExecutionException {
      SearchReplaceEngine.get().gotoNextMatch();
      return null;
   }
}
