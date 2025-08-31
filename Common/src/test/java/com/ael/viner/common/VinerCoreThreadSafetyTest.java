package com.ael.viner.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Thread safety and concurrent access tests for VinerCore singleton. Tests behavior under
 * concurrent access scenarios.
 */
class VinerCoreThreadSafetyTest {

  private ExecutorService executor;

  @BeforeEach
  void setUp() {
    executor = Executors.newFixedThreadPool(10);
    VinerCore.setInstance(null);
  }

  @AfterEach
  void tearDown() {
    VinerCore.setInstance(null);
    if (executor != null) {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  @Test
  @DisplayName("Should handle concurrent instance setting safely")
  @Timeout(10)
  void handleConcurrentInstanceSetting() throws InterruptedException {
    // Arrange
    int threadCount = 20;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicReference<Exception> exception = new AtomicReference<>();

    // Create multiple mock instances
    IVinerMod[] mockMods = new IVinerMod[threadCount];
    for (int i = 0; i < threadCount; i++) {
      mockMods[i] = mock(IVinerMod.class, "MockMod" + i);
    }

    // Act - Multiple threads trying to set instance concurrently
    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              startLatch.await();
              VinerCore.setInstance(mockMods[index]);
              successCount.incrementAndGet();
            } catch (Exception e) {
              exception.compareAndSet(null, e);
            } finally {
              finishLatch.countDown();
            }
          });
    }

    // Start all threads simultaneously
    startLatch.countDown();
    assertTrue(
        finishLatch.await(5, TimeUnit.SECONDS), "All threads should complete within 5 seconds");

    // Assert
    assertNull(exception.get(), "No exceptions should occur during concurrent setting");
    assertEquals(threadCount, successCount.get(), "All threads should successfully set instance");
    assertNotNull(VinerCore.get(), "Final instance should not be null");
  }

  @Test
  @DisplayName("Should handle concurrent instance getting safely")
  @Timeout(10)
  void handleConcurrentInstanceGetting() throws InterruptedException {
    // Arrange
    IVinerMod mockMod = mock(IVinerMod.class);
    IPlayerRegistry mockRegistry = mock(IPlayerRegistry.class);
    when(mockMod.getPlayerRegistry()).thenReturn(mockRegistry);
    VinerCore.setInstance(mockMod);

    int threadCount = 50;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicReference<Exception> exception = new AtomicReference<>();

    // Act - Multiple threads getting instance concurrently
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              IVinerMod retrieved = VinerCore.get();
              if (retrieved == mockMod) {
                successCount.incrementAndGet();
              }
            } catch (Exception e) {
              exception.compareAndSet(null, e);
            } finally {
              finishLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    assertTrue(
        finishLatch.await(5, TimeUnit.SECONDS), "All threads should complete within 5 seconds");

    // Assert
    assertNull(exception.get(), "No exceptions should occur during concurrent getting");
    assertEquals(threadCount, successCount.get(), "All threads should retrieve the same instance");
  }

  @Test
  @DisplayName("Should handle concurrent player registry access safely")
  @Timeout(10)
  void handleConcurrentPlayerRegistryAccess() throws InterruptedException {
    // Arrange
    IVinerMod mockMod = mock(IVinerMod.class);
    IPlayerRegistry mockRegistry = mock(IPlayerRegistry.class);
    when(mockMod.getPlayerRegistry()).thenReturn(mockRegistry);
    VinerCore.setInstance(mockMod);

    int threadCount = 30;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicReference<Exception> exception = new AtomicReference<>();

    // Act - Multiple threads accessing player registry concurrently
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              IPlayerRegistry registry = VinerCore.getPlayerRegistry();
              if (registry == mockRegistry) {
                successCount.incrementAndGet();
              }
            } catch (Exception e) {
              exception.compareAndSet(null, e);
            } finally {
              finishLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    assertTrue(
        finishLatch.await(5, TimeUnit.SECONDS), "All threads should complete within 5 seconds");

    // Assert
    assertNull(exception.get(), "No exceptions should occur during concurrent registry access");
    assertEquals(threadCount, successCount.get(), "All threads should retrieve the same registry");

    // Verify the mock was called the expected number of times
    verify(mockMod, times(threadCount)).getPlayerRegistry();
  }

  @Test
  @DisplayName("Should handle race conditions between setting and getting")
  @Timeout(10)
  void handleRaceConditionsBetweenSetAndGet() throws InterruptedException {
    // Arrange
    int setterThreads = 5;
    int getterThreads = 15;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(setterThreads + getterThreads);
    AtomicReference<Exception> exception = new AtomicReference<>();

    IVinerMod[] mockMods = new IVinerMod[setterThreads];
    for (int i = 0; i < setterThreads; i++) {
      mockMods[i] = mock(IVinerMod.class, "SetterMock" + i);
    }

    // Setter threads
    for (int i = 0; i < setterThreads; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              startLatch.await();
              Thread.sleep((long) (Math.random() * 10)); // Random delay
              VinerCore.setInstance(mockMods[index]);
            } catch (Exception e) {
              exception.compareAndSet(null, e);
            } finally {
              finishLatch.countDown();
            }
          });
    }

    // Getter threads
    for (int i = 0; i < getterThreads; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              Thread.sleep((long) (Math.random() * 10)); // Random delay
              IVinerMod instance = VinerCore.get();
              // Instance can be null or any of the mock instances - both are valid
            } catch (Exception e) {
              exception.compareAndSet(null, e);
            } finally {
              finishLatch.countDown();
            }
          });
    }

    // Start all threads
    startLatch.countDown();
    assertTrue(
        finishLatch.await(10, TimeUnit.SECONDS), "All threads should complete within 10 seconds");

    // Assert
    assertNull(exception.get(), "No exceptions should occur during race conditions");
    // Final state should be one of the setter instances or null
    IVinerMod finalInstance = VinerCore.get();
    if (finalInstance != null) {
      // Should be one of the mock instances
      boolean foundMatch = false;
      for (IVinerMod mock : mockMods) {
        if (mock == finalInstance) {
          foundMatch = true;
          break;
        }
      }
      assertTrue(foundMatch, "Final instance should be one of the set instances");
    }
  }

  @Test
  @DisplayName("Should handle concurrent null checks safely")
  @Timeout(10)
  void handleConcurrentNullChecksSafely() throws InterruptedException {
    // Arrange - Start with null instance
    int threadCount = 25;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(threadCount);
    AtomicInteger nullCount = new AtomicInteger(0);
    AtomicInteger exceptionCount = new AtomicInteger(0);

    // Act - Multiple threads trying to get player registry when instance is null
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              try {
                IPlayerRegistry registry = VinerCore.getPlayerRegistry();
                if (registry == null) {
                  nullCount.incrementAndGet();
                }
              } catch (NullPointerException e) {
                // Expected when instance is null
                exceptionCount.incrementAndGet();
              }
            } catch (Exception e) {
              // Unexpected exception - should not happen
              fail("Unexpected exception: " + e.getMessage());
            } finally {
              finishLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    assertTrue(
        finishLatch.await(5, TimeUnit.SECONDS), "All threads should complete within 5 seconds");

    // Assert
    assertEquals(threadCount, exceptionCount.get(), "All threads should get NullPointerException");
    assertEquals(0, nullCount.get(), "No threads should get null registry");
  }
}
