# Project Review Profile

## Stack
- Language: Python 3.12 (interpreter `python3`); standard library only, no
  external/pinned dependencies.
- Test framework: `unittest` (pytest is NOT installed). Run tests from the
  repository root with `python3 -m unittest -v`.
- Workflow: TDD — write the failing test first (RED), then implement to GREEN.

## Conventions
- Executable scripts use a `def main() -> None:` entry point plus an
  `if __name__ == "__main__": main()` guard.
- Tests assert real behavior (e.g. captured subprocess stdout), not logs,
  mocks, or counters.
- Source files should end with a trailing newline.

## Repo Hygiene Gates
- Do NOT commit generated/build artifacts: no `__pycache__/`, no `*.pyc`.
  These must be excluded (`.gitignore`) rather than tracked.
- A change should not add files beyond what its own acceptance criteria list.
- `REVIEW.md` is the canonical review profile; keep it short and project-specific.

## Reviewing This Project
- For single-file scripts, the design lane is usually `NOT_RUN`.
- The cause lane applies only when a change claims to fix a bug or failure mode.
- Prefer running the documented commands from the repo root when verification
  is requested.
