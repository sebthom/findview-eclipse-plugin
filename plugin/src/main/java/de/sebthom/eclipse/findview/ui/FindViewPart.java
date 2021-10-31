/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.findview.ui;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.sebthom.eclipse.findview.SearchReplaceEngine;

/**
 * @author Sebastian Thomschke
 */
public final class FindViewPart extends ViewPart {

   public static final String ID = FindView.class.getName();

   private FindView content;

   @Override
   public void createPartControl(final Composite parent) {
      final var composite = new Composite(parent, SWT.NONE);
      composite.setLayout(GridLayoutFactory.fillDefaults().create());
      content = new FindView(composite, this);
   }

   @Override
   public void dispose() {
      SearchReplaceEngine.get().removeMarkers();
      super.dispose();
   }

   @Override
   public void setFocus() {
      content.setFocus();
   }
}
