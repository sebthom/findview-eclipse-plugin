/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.util.ui;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * @author Sebastian Thomschke
 */
public final class Keys {

   public static void onKeyPressed(final Control control, final Consumer<KeyEvent> handler) {
      control.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(final KeyEvent ev) {
            handler.accept(ev);
         }
      });
   }

   public static void sendKeyDown(final int keyCode) {
      final var e = new Event();
      e.keyCode = keyCode;
      e.type = SWT.KeyDown;
      UI.getDisplay().post(e);
   }

   public static void sendKeyUp(final int keyCode) {
      final var e = new Event();
      e.keyCode = keyCode;
      e.type = SWT.KeyUp;
      UI.getDisplay().post(e);
   }

   private Keys() {
   }
}
