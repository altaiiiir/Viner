package com.ael.viner.common;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for running all Common module tests together.
 * This provides a convenient way to run all tests and get consolidated reporting.
 */
@Suite
@SuiteDisplayName("Viner Common Module Test Suite")
@SelectPackages("com.ael.viner.common")
public class TestSuite {
  // This class serves as a test suite runner
  // All tests in the com.ael.viner.common package will be executed
}
