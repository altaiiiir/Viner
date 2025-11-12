# Viner Testing Instructions

This document explains how to run tests in the Viner project and interpret the results.

## Running Tests

### Run All Tests
```bash
./gradlew testAll
```
This runs tests for all modules (Common and Forge) and provides a comprehensive summary.

### Run Tests for Specific Modules
```bash
# Run only Common module tests
./gradlew testCommon

# Run only Forge module tests  
./gradlew testForge

# Run tests for individual modules directly
./gradlew :Common:test
./gradlew :Forge:test
```

### Run Tests with Specific Options
```bash
# Run tests with more verbose output
./gradlew testAll --info

# Run tests and continue on failure
./gradlew testAll --continue

# Force all tests to run (ignore up-to-date optimization)
./gradlew testAll --rerun-tasks

# Clean build cache and run all tests
./gradlew clean testAll
```

### Important: Gradle Incremental Builds
**If you see fewer tests running than expected**, it's because Gradle skips "up-to-date" tests that haven't changed. Use `--rerun-tasks` to force all tests to run:
```bash
./gradlew testAll --rerun-tasks
```

## Understanding Test Output

### Test Result Format
Each test execution will show:
- **PASSED**: Test completed successfully
- **FAILED**: Test failed with error details
- **SKIPPED**: Test was skipped (usually due to conditions)

### Summary Format
At the end of each test run, you'll see:
```
--------------------------------
|  Results: SUCCESS (X tests, Y passed, 0 failed, Z skipped)  |
--------------------------------
```

### What Each Module Tests

#### Common Module Tests
- **VineMiningLogicTest**: Tests the core vine mining algorithm
  - Single block collection
  - Connected block detection
  - Limit enforcement
  - 3D block traversal
  
- **PositionTest**: Tests the Position record class
  - Coordinate handling
  - Equality and hashing
  - String representation

- **VinerCoreTest**: Tests the mod singleton management
  - Instance setting/getting
  - Player registry access
  - Error handling

#### Forge Module Tests  
- **VinerForgeTest**: Tests Forge-specific initialization
  - Mod ID constants
  - Interface implementation
  - Instance management

## Test Structure

Tests follow JUnit 5 conventions:
- Test classes end with `Test`
- Test methods are annotated with `@Test`
- Tests use descriptive `@DisplayName` annotations
- Assertions use JUnit 5 assertions (assertEquals, assertTrue, etc.)
- Mocking uses Mockito for isolation

## Continuous Integration

The test setup supports:
- Detailed logging of test execution
- Proper exit codes for CI/CD pipelines
- Test result summaries for quick overview
- Individual module testing for focused development

## Writing New Tests

1. Create test classes in `src/test/java` mirroring your source structure
2. Use descriptive test names and display names
3. Follow the Arrange-Act-Assert pattern
4. Mock external dependencies using Mockito
5. Test both positive and negative cases
6. Keep tests independent and idempotent

## Troubleshooting

### Common Issues
- **Test not found**: Ensure test class is in correct package and ends with `Test`
- **Compilation errors**: Check test dependencies are properly configured
- **Mock issues**: Verify Mockito usage and mock setup

### Getting Help
- Check test output for specific error messages
- Use `--info` or `--debug` flags for detailed logging
- Review test class imports and dependencies
