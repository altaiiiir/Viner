package com.ael.viner.forge;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for running all Forge module tests together. This provides a convenient way to run all
 * tests and get consolidated reporting.
 */
@Suite
@SuiteDisplayName("Viner Forge Module Test Suite")
@SelectPackages("com.ael.viner.forge")
public class TestSuite {
  // This class serves as a test suite runner
  // All tests in the com.ael.viner.forge package will be executed
}
