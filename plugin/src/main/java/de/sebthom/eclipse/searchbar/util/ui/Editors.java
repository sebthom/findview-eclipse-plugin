/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.util.ui;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Sebastian Thomschke
 */
public final class Editors {

   public static IAnnotationModel getAnnotationModel(final ITextEditor editor) {
      if (editor == null)
         return null;
      final var docProvider = editor.getDocumentProvider();
      if (docProvider == null)
         return null;
      return docProvider.getAnnotationModel(editor.getEditorInput());
   }

   public static IDocument getDocument(final ITextEditor editor) {
      if (editor == null)
         return null;
      final var docProvider = editor.getDocumentProvider();
      if (docProvider == null)
         return null;
      return docProvider.getDocument(editor.getEditorInput());
   }

   private Editors() {
   }
}
