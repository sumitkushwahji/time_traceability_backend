# 🔄 Development Workflow

Comprehensive guide to Git workflow and development practices for the Time Traceability Backend project, including branching strategies, code review processes, and continuous integration.

## 📋 Table of Contents

- [Git Workflow Overview](#-git-workflow-overview)
- [Branching Strategy](#-branching-strategy)
- [Development Process](#-development-process)
- [Code Review Process](#-code-review-process)
- [Commit Guidelines](#-commit-guidelines)
- [Release Management](#-release-management)
- [Hotfix Workflow](#-hotfix-workflow)
- [Continuous Integration](#-continuous-integration)
- [Development Best Practices](#-development-best-practices)
- [Team Collaboration](#-team-collaboration)

## 🎯 Git Workflow Overview

The Time Traceability Backend project follows a **Git Flow** based workflow with modifications for continuous delivery:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Repository Structure                      │
└─────────────────────────────────────────────────────────────────┘

main branch (production)
├── develop branch (integration)
│   ├── feature/add-new-api-endpoint
│   ├── feature/improve-file-processing
│   ├── feature/enhance-statistics
│   └── bugfix/fix-mjd-calculation
├── release/v1.2.0 (release preparation)
└── hotfix/critical-security-patch (emergency fixes)

┌─────────────────────────────────────────────────────────────────┐
│                        Workflow Stages                          │
└─────────────────────────────────────────────────────────────────┘

Development → Testing → Code Review → Integration → Release → Production
     ↓            ↓           ↓             ↓           ↓         ↓
feature/*    unit tests   PR review     develop     release/*   main
```

### Core Principles

1. **Feature Isolation**: Each feature developed in separate branch
2. **Code Quality**: Mandatory code reviews and automated testing
3. **Continuous Integration**: Automated builds and tests on every commit
4. **Release Readiness**: Regular integration and release candidate testing
5. **Hotfix Support**: Emergency patches with minimal disruption

## 🌿 Branching Strategy

### 1. Main Branches

#### **main** - Production Branch
```bash
# Production-ready code only
# Direct commits not allowed
# Only merges from release/* or hotfix/* branches
# Automatically deployed to production

git checkout main
git log --oneline -5
# Example output:
# a1b2c3d (HEAD -> main, tag: v1.1.0) Release v1.1.0
# d4e5f6g Fix critical database connection issue
# g7h8i9j Release v1.0.0
```

#### **develop** - Integration Branch
```bash
# Latest development features
# Integration testing happens here
# Merges from feature/* and bugfix/* branches
# Base for release branches

git checkout develop
git log --oneline -5
# Example output:
# x1y2z3a (HEAD -> develop) Merge feature/enhance-statistics
# b4c5d6e Merge feature/add-new-api-endpoint
# e7f8g9h Merge bugfix/fix-mjd-calculation
```

### 2. Supporting Branches

#### **feature/** - Feature Development
```bash
# Create new feature branch
git checkout develop
git pull origin develop
git checkout -b feature/add-materialized-view-refresh

# Naming convention: feature/short-descriptive-name
# Examples:
# feature/add-new-api-endpoint
# feature/improve-file-processing
# feature/enhance-statistics-dashboard
# feature/implement-caching
```

#### **bugfix/** - Bug Fixes
```bash
# Create bugfix branch
git checkout develop
git pull origin develop
git checkout -b bugfix/fix-mjd-extraction-logic

# Naming convention: bugfix/short-description
# Examples:
# bugfix/fix-mjd-calculation
# bugfix/resolve-null-pointer-exception
# bugfix/correct-file-parsing-logic
```

#### **release/** - Release Preparation
```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# Naming convention: release/vX.Y.Z
# Examples:
# release/v1.1.0
# release/v1.2.0-beta
# release/v2.0.0-rc1
```

#### **hotfix/** - Emergency Fixes
```bash
# Create hotfix branch from main
git checkout main
git pull origin main
git checkout -b hotfix/critical-security-patch

# Naming convention: hotfix/critical-issue-description
# Examples:
# hotfix/security-vulnerability-fix
# hotfix/database-connection-issue
# hotfix/memory-leak-resolution
```

## 🔨 Development Process

### 1. Starting New Feature Development

```bash
# Step 1: Sync with latest develop
git checkout develop
git pull origin develop

# Step 2: Create feature branch
git checkout -b feature/add-file-validation-api

# Step 3: Set up development environment
./dev-scripts/dev-setup.sh

# Step 4: Create initial commit
git commit --allow-empty -m "feat: initialize file validation API feature"
git push -u origin feature/add-file-validation-api

# Step 5: Start development
# - Implement feature
# - Write tests
# - Update documentation
# - Test thoroughly
```

### 2. Feature Development Cycle

```bash
# Daily development workflow
git status                          # Check current state
git add src/main/java/...          # Stage specific files
git commit -m "feat: add file validation endpoint"

# Regular syncing with develop
git fetch origin
git rebase origin/develop          # Rebase to keep history clean

# Push progress regularly
git push origin feature/add-file-validation-api

# Before creating PR - final cleanup
git rebase -i HEAD~3               # Interactive rebase to squash commits
git push --force-with-lease origin feature/add-file-validation-api
```

### 3. Testing During Development

```bash
# Run unit tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=FileValidationServiceTest

# Run integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run with coverage
./mvnw test jacoco:report

# Check test results
open target/site/jacoco/index.html

# Run application for manual testing
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 4. Code Quality Checks

```bash
# Check code style
./mvnw fmt:check

# Format code
./mvnw fmt:format

# Static analysis (if configured)
./mvnw spotbugs:check

# Dependency vulnerability check
./mvnw dependency-check:check

# Full quality check before PR
./mvnw clean verify
```

## 👥 Code Review Process

### 1. Pull Request Creation

```markdown
## Pull Request Template

### Description
Brief description of the changes made.

### Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

### Related Issues
Fixes #123
Related to #456

### Changes Made
- Added file validation API endpoint
- Implemented validation rules for satellite data files
- Added comprehensive unit and integration tests
- Updated API documentation

### Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] Performance impact assessed

### Checklist
- [ ] Code follows the project's style guidelines
- [ ] Self-review of code completed
- [ ] Code is properly commented
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] No new warnings introduced

### Screenshots (if applicable)
[Add screenshots of new features or UI changes]

### Additional Notes
Any additional information that reviewers should know.
```

### 2. Code Review Guidelines

#### **For Authors:**
```bash
# Before requesting review
git checkout feature/add-file-validation-api
git rebase origin/develop                    # Ensure up-to-date
./mvnw clean verify                         # Ensure all tests pass
git push --force-with-lease origin feature/add-file-validation-api

# Create descriptive PR title and description
# Request appropriate reviewers
# Respond to feedback promptly
# Keep commits focused and logical
```

#### **For Reviewers:**
```bash
# Checkout PR branch for local testing
git fetch origin
git checkout feature/add-file-validation-api
./mvnw clean compile                        # Verify it builds
./mvnw test                                 # Run tests
./mvnw spring-boot:run -Dspring.profiles.active=dev  # Test functionality

# Review checklist:
# ✓ Code quality and readability
# ✓ Test coverage and quality
# ✓ Documentation updates
# ✓ Performance considerations
# ✓ Security implications
# ✓ API design consistency
# ✓ Error handling
# ✓ Logging appropriateness
```

### 3. Review Feedback Process

```markdown
## Feedback Categories

### 🔴 Must Fix (Blocking)
Issues that prevent merging:
- Broken functionality
- Security vulnerabilities
- Test failures
- Major design flaws

### 🟡 Should Fix (Recommended)
Issues that should be addressed:
- Code quality improvements
- Better error handling
- Performance optimizations
- Documentation gaps

### 🟢 Consider (Optional)
Suggestions for improvement:
- Alternative approaches
- Future enhancements
- Code style preferences
- Best practice recommendations
```

## 📝 Commit Guidelines

### 1. Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### **Types:**
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

#### **Examples:**
```bash
# Feature addition
git commit -m "feat(api): add file validation endpoint

Add REST endpoint for validating satellite data files.
Includes validation for file format, size, and content structure.

Closes #123"

# Bug fix
git commit -m "fix(parser): correct MJD extraction from filename

Fix issue where MJD was incorrectly extracted as '2' instead of '60866'
from filename 'GZLI2P60.866'. Now properly concatenates all digits
after position 6.

Fixes #145"

# Documentation
git commit -m "docs(api): update API documentation for statistics endpoint

Add detailed examples and response schemas for the file upload
statistics API endpoint."

# Refactoring
git commit -m "refactor(service): extract file processing logic

Move file processing logic from controller to dedicated service
for better separation of concerns and testability."
```

### 2. Commit Best Practices

```bash
# Make atomic commits
git add src/main/java/...FileValidationService.java
git commit -m "feat(validation): add file validation service"

git add src/test/java/...FileValidationServiceTest.java
git commit -m "test(validation): add tests for file validation service"

# Use imperative mood in subject line
✓ "Add file validation"
✗ "Added file validation"
✗ "Adding file validation"

# Limit subject line to 50 characters
✓ "feat(api): add file validation endpoint"
✗ "feat(api): add comprehensive file validation endpoint with multiple validation rules"

# Wrap body at 72 characters
git commit -m "feat(api): add file validation endpoint

This endpoint validates satellite data files before processing.
It checks file format, size constraints, and basic content
structure to ensure data quality."

# Reference issues and PRs
git commit -m "fix(parser): correct MJD extraction logic

Fixes #145
Related to PR #150"
```

## 🚀 Release Management

### 1. Release Planning

```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# Update version numbers
# Update pom.xml version
mvn versions:set -DnewVersion=1.2.0
mvn versions:commit

# Update documentation
# Update CHANGELOG.md
# Update README.md if needed

# Initial release commit
git add .
git commit -m "chore(release): prepare release v1.2.0"
git push -u origin release/v1.2.0
```

### 2. Release Testing

```bash
# Run comprehensive tests
./mvnw clean verify

# Run integration tests with test containers
./mvnw test -Dtest="*IntegrationTest"

# Performance testing
./mvnw test -Dtest="*PerformanceTest"

# Security scanning
./mvnw dependency-check:check

# Build release artifact
./mvnw clean package -Pprod

# Deploy to staging environment
./deploy-qa.sh
```

### 3. Release Finalization

```bash
# After testing is complete, merge to main
git checkout main
git pull origin main
git merge --no-ff release/v1.2.0
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin main
git push origin v1.2.0

# Merge back to develop
git checkout develop
git merge --no-ff release/v1.2.0
git push origin develop

# Clean up release branch
git branch -d release/v1.2.0
git push origin --delete release/v1.2.0

# Deploy to production
./deploy-prod.sh
```

### 4. Release Notes Generation

```markdown
# Release v1.2.0

## 🎉 New Features
- **File Validation API**: Added comprehensive file validation endpoint (#123)
- **Enhanced Statistics**: Improved statistics dashboard with real-time updates (#134)
- **Batch Processing**: Added parallel processing for large file batches (#145)

## 🐛 Bug Fixes
- **MJD Extraction**: Fixed incorrect MJD parsing from filenames (#145)
- **Null Pointer**: Resolved NullPointerException in statistics service (#156)
- **Memory Leak**: Fixed memory leak in file processing pipeline (#167)

## 🔧 Improvements
- **Performance**: 40% improvement in file processing speed
- **Monitoring**: Enhanced health checks and metrics
- **Documentation**: Updated API documentation with examples

## 📋 Technical Changes
- Updated Spring Boot to 2.7.2
- Added Testcontainers for integration testing
- Improved error handling throughout the application

## 🔒 Security
- Updated dependencies to address security vulnerabilities
- Added input validation for all API endpoints

## 📊 Metrics
- Files processed: 1.2M+ since last release
- Test coverage: 85%
- Performance improvement: 40%
- Bug fixes: 12

## 🚀 Deployment
- Database migration required: No
- Configuration changes: See [Configuration Guide](Configuration-Guide.md)
- Breaking changes: None
```

## 🚨 Hotfix Workflow

### 1. Emergency Hotfix Process

```bash
# Create hotfix branch from main
git checkout main
git pull origin main
git checkout -b hotfix/critical-security-patch

# Make minimal necessary changes
# Only fix the critical issue
# Do not add new features

# Test thoroughly
./mvnw clean verify
./mvnw test -Dtest="SecurityTest"

# Commit with clear message
git add .
git commit -m "fix(security): patch critical vulnerability in file upload

Sanitize file paths to prevent directory traversal attacks.
Add validation for file upload paths and reject suspicious patterns.

CVE-2023-12345"

# Push hotfix
git push -u origin hotfix/critical-security-patch
```

### 2. Hotfix Deployment

```bash
# Create emergency PR to main
# Get expedited review from senior developers
# Deploy to staging for quick verification

# After approval, merge to main
git checkout main
git merge --no-ff hotfix/critical-security-patch
git tag -a v1.1.1 -m "Hotfix version 1.1.1 - Security patch"
git push origin main
git push origin v1.1.1

# Merge back to develop
git checkout develop
git merge --no-ff hotfix/critical-security-patch
git push origin develop

# Deploy to production immediately
./deploy-prod.sh --hotfix

# Clean up hotfix branch
git branch -d hotfix/critical-security-patch
git push origin --delete hotfix/critical-security-patch
```

## 🔄 Continuous Integration

### 1. GitHub Actions Workflow

```yaml
# .github/workflows/ci.yml
name: Continuous Integration

on:
  push:
    branches: [ develop, main ]
  pull_request:
    branches: [ develop, main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: test_db
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests
      run: ./mvnw clean verify
      env:
        DB_URL: jdbc:postgresql://localhost:5432/test_db
        DB_USERNAME: postgres
        DB_PASSWORD: postgres
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: target/site/jacoco/jacoco.xml

  build:
    needs: test
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build application
      run: ./mvnw clean package -DskipTests
    
    - name: Build Docker image
      run: docker build -t time-traceability-backend:${{ github.sha }} .
    
    - name: Run security scan
      run: ./mvnw dependency-check:check

  deploy-staging:
    if: github.ref == 'refs/heads/develop'
    needs: [test, build]
    runs-on: ubuntu-latest
    
    steps:
    - name: Deploy to staging
      run: |
        echo "Deploying to staging environment"
        # Add actual deployment script
```

### 2. Pre-commit Hooks

```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "Running pre-commit checks..."

# Check if build passes
./mvnw clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "Build failed. Commit aborted."
    exit 1
fi

# Run tests
./mvnw test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Check code formatting
./mvnw fmt:check
if [ $? -ne 0 ]; then
    echo "Code formatting issues found. Run: ./mvnw fmt:format"
    exit 1
fi

# Check for large files
find . -size +10M -not -path "./target/*" -not -path "./.git/*" | head -1 | grep -q .
if [ $? -eq 0 ]; then
    echo "Large files detected. Commit aborted."
    exit 1
fi

echo "Pre-commit checks passed!"
exit 0
```

## 💡 Development Best Practices

### 1. Branch Management

```bash
# Keep branches up to date
git checkout feature/my-feature
git fetch origin
git rebase origin/develop              # Rebase regularly

# Clean up merged branches
git branch --merged develop | grep -v develop | xargs -n 1 git branch -d

# Delete remote tracking branches
git remote prune origin

# Check branch status
git branch -vv                         # Show tracking information
```

### 2. Conflict Resolution

```bash
# When rebase conflicts occur
git rebase origin/develop
# CONFLICT in file.java

# Resolve conflicts in IDE
# Stage resolved files
git add file.java

# Continue rebase
git rebase --continue

# If too many conflicts, abort and merge instead
git rebase --abort
git merge origin/develop
```

### 3. Code Quality Maintenance

```bash
# Regular dependency updates
./mvnw versions:display-dependency-updates
./mvnw versions:use-latest-versions

# Security vulnerability checks
./mvnw dependency-check:check

# Clean up dead code
# Use IDE tools to find unused code
# Remove commented code
# Clean up imports

# Refactor regularly
# Extract methods for complex logic
# Improve naming conventions
# Add documentation
```

## 🤝 Team Collaboration

### 1. Communication Practices

```markdown
## Daily Standup Template

**Yesterday:**
- Completed MJD extraction fix (#145)
- Started work on file validation API (#123)

**Today:**
- Finish file validation API implementation
- Write comprehensive tests
- Update API documentation

**Blockers:**
- Need clarification on validation rules for .nav files
- Waiting for code review on PR #150
```

### 2. Knowledge Sharing

```bash
# Document decisions
# Create Architecture Decision Records (ADRs)
# Update project wiki
# Share learnings in team meetings

# Code pairing
git checkout -b pair/improve-performance
# Work together on complex problems
# Share knowledge between team members

# Regular code reviews
# Review each other's PRs promptly
# Provide constructive feedback
# Learn from different approaches
```

### 3. Conflict Resolution

```markdown
## Merge Conflict Resolution

### Step 1: Understand the conflict
- Review both changes
- Understand the intent behind each change
- Identify the best approach

### Step 2: Resolve conflicts
- Keep both changes if they're compatible
- Choose the better implementation
- Combine approaches if possible
- Ask for clarification if needed

### Step 3: Test thoroughly
- Run all tests
- Test manually if needed
- Ensure no functionality is broken
```

---

## 📞 Development Workflow Support

For workflow questions and improvements:

1. **Git Issues**: Consult Git documentation and team leads
2. **Process Questions**: Review this guide and ask in team meetings
3. **Tool Problems**: Check development environment setup
4. **Collaboration Issues**: Discuss in retrospectives and team meetings

**Related Documentation**: 
- [Environment Setup](Environment-Setup.md) for development environment
- [Code Standards](Code-Standards.md) for coding conventions
- [Testing Strategy](Testing-Strategy.md) for testing practices

---

**Development Workflow Complete!** 🔄 Your team is set up for efficient, collaborative development with clear processes and quality gates.
