# Implementation Plan: Hello World

## Overview

Create a minimal, runnable Python program that prints "Hello, World!" with a
test that verifies the output. This is a fresh repository with only a README
file; the program will be the first source file added.

## Global Constraints

- Language: Python 3 (interpreter is python3 3.12.x)
- No external dependencies — use the standard library only
- The program must be directly runnable: `python3 hello.py` prints exactly
  `Hello, World!` followed by a newline
- Tests use the standard-library `unittest` framework (pytest is not
  installed); tests are runnable via `python3 -m unittest -v`
- Follow TDD: write the failing test first, confirm RED, then implement to
  GREEN

## Task 1

### Goal

Create `hello.py` that prints "Hello, World!" and a `test_hello.py` that
verifies the exact output using `unittest`.

### Requirements

1. Create `test_hello.py` first with a single `unittest.TestCase` test:
   - Runs `hello.py` as a subprocess and captures stdout
   - Asserts the captured stdout equals `Hello, World!\n`
2. Run the test to confirm RED (fails because `hello.py` does not exist yet)
3. Create `hello.py`:
   - A `main()` function that prints `Hello, World!`
   - A `if __name__ == "__main__": main()` guard
   - No unused imports, no dead code
4. Run the test to confirm GREEN
5. Run `python3 hello.py` directly to confirm it prints `Hello, World!`
6. Commit the work

### Acceptance Criteria

- `python3 hello.py` outputs exactly `Hello, World!` (with trailing newline)
- `python3 -m unittest -v` passes with 1 test
- No files beyond `hello.py` and `test_hello.py` are created
