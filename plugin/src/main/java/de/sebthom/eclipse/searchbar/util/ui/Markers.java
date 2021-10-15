/*
 * Copyright 2021 by Sebastian Thomschke and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package de.sebthom.eclipse.searchbar.util.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

import de.sebthom.eclipse.searchbar.SearchBarPlugin;

/**
 * @author Sebastian Thomschke
 */
public final class Markers {

   public static final String DEFAULT_MARKER_ID = SearchBarPlugin.class.getPackageName() + ".defaultMarker";

   private final String markerId;

   private Annotation[] activeMarkers = new Annotation[0];
   private IAnnotationModel activeMarkersAnnoModel;

   public Markers() {
      this(DEFAULT_MARKER_ID);
   }

   public Markers(final String markerId) {
      this.markerId = markerId;
   }

   public void removeMarkers() {
      if (activeMarkersAnnoModel != null && activeMarkers.length > 0) {
         if (activeMarkersAnnoModel instanceof IAnnotationModelExtension) {
            final var annoModelEx = (IAnnotationModelExtension) activeMarkersAnnoModel;
            annoModelEx.replaceAnnotations(activeMarkers, new HashMap<>());
         } else {
            for (final Annotation annotation : activeMarkers) {
               activeMarkersAnnoModel.removeAnnotation(annotation);
            }
         }
      }
   }

   public void setMarkers(final IAnnotationModel annoModel, final List<Position> matches, final IProgressMonitor monitor) {
      if (activeMarkersAnnoModel != null) {
         removeMarkers();
      }
      activeMarkersAnnoModel = annoModel;

      if (matches == null)
         return;

      final var job = new Job("Setting markers") {
         @Override
         public IStatus run(final IProgressMonitor monitor) {
            final Map<Annotation, Position> newMarkers = new HashMap<>(matches.size());
            for (final var matchPos : matches) {
               newMarkers.put(new Annotation(markerId, false, null), matchPos);
            }

            if (annoModel instanceof IAnnotationModelExtension) {
               ((IAnnotationModelExtension) annoModel).replaceAnnotations(activeMarkers, newMarkers);
            } else {
               removeMarkers();
               for (final Map.Entry<Annotation, Position> entry : newMarkers.entrySet()) {
                  annoModel.addAnnotation(entry.getKey(), entry.getValue());
               }
            }
            activeMarkers = newMarkers.keySet().toArray(new Annotation[newMarkers.size()]);
            return Status.OK_STATUS;
         }
      };
      job.setPriority(Job.INTERACTIVE);
      job.run(monitor);
   }
}
