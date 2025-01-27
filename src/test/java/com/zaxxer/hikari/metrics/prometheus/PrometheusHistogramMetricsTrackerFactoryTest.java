package com.zaxxer.hikari.metrics.prometheus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.PoolStats;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

public class PrometheusHistogramMetricsTrackerFactoryTest {

   @Test
   public void registersToProvidedCollectorRegistry() {
      PrometheusRegistry collectorRegistry = new PrometheusRegistry();
      PrometheusHistogramMetricsTrackerFactory factory =
         new PrometheusHistogramMetricsTrackerFactory(collectorRegistry);
      IMetricsTracker iMetricsTracker = factory.create("testpool-1", poolStats());
      assertHikariMetricsAreNotPresent(PrometheusRegistry.defaultRegistry);
      assertHikariMetricsArePresent(collectorRegistry);
      iMetricsTracker.close();
   }

   @Test
   public void registersToDefaultCollectorRegistry() {
      PrometheusHistogramMetricsTrackerFactory factory = new PrometheusHistogramMetricsTrackerFactory();
      IMetricsTracker iMetricsTracker = factory.create("testpool-2", poolStats());
      assertHikariMetricsArePresent(PrometheusRegistry.defaultRegistry);
      iMetricsTracker.close();
   }

   private void assertHikariMetricsArePresent(PrometheusRegistry collectorRegistry) {
      List<String> registeredMetrics = toMetricNames(collectorRegistry.scrape());
      assertTrue(registeredMetrics.contains("hikaricp_active_connections"));
      assertTrue(registeredMetrics.contains("hikaricp_idle_connections"));
      assertTrue(registeredMetrics.contains("hikaricp_pending_threads"));
      assertTrue(registeredMetrics.contains("hikaricp_connections"));
      assertTrue(registeredMetrics.contains("hikaricp_max_connections"));
      assertTrue(registeredMetrics.contains("hikaricp_min_connections"));
   }

   private void assertHikariMetricsAreNotPresent(PrometheusRegistry collectorRegistry) {
      List<String> registeredMetrics = toMetricNames(collectorRegistry.scrape());
      assertFalse(registeredMetrics.contains("hikaricp_active_connections"));
      assertFalse(registeredMetrics.contains("hikaricp_idle_connections"));
      assertFalse(registeredMetrics.contains("hikaricp_pending_threads"));
      assertFalse(registeredMetrics.contains("hikaricp_connections"));
      assertFalse(registeredMetrics.contains("hikaricp_max_connections"));
      assertFalse(registeredMetrics.contains("hikaricp_min_connections"));
   }

   private List<String> toMetricNames(MetricSnapshots metricSnapshots) {
      return metricSnapshots.stream().map(metricSnapshot -> metricSnapshot.getMetadata().getName()).collect(Collectors.toList());
   }

   private PoolStats poolStats() {
      return new PoolStats(0) {
         @Override
         protected void update() {
            // do nothing
         }
      };
   }

}
