package com.ael.viner.common;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for all Common module tests. Runs all tests in the Common module with
 * detailed reporting.
 */
@Suite
@SuiteDisplayName("🧪 Viner Common Module - Complete Test Suite")
@SelectPackages("com.ael.viner.common")
public class AllTestsSuite {
  // This class serves as a comprehensive test suite runner
  // All tests in the com.ael.viner.common package and subpackages will be executed

  // Test Coverage:
  // ✅ VinerCore - Singleton management and mod instance handling
  // ✅ Position - Record functionality and coordinate handling
  // ✅ VineMiningLogic - Core vine mining algorithm
  // ✅ BlockTagUtils - Block and tag parsing utilities
}
