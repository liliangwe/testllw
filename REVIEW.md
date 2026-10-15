# Project Review Profile — Todo CLI

- **Language:** Python 3.9+
- **Dependencies:** standard library only
- **Testing:** `unittest` (stdlib), run with `python3 -m unittest discover -s tests -v`
- **Entry point:** `todo` command → `src/todo_cli/main.py:main()`
- **Data path:** `~/.todo/tasks.json`

## Review gates (project-specific)

1. Zero third-party imports in runtime and test code.
2. Atomic writes via `NamedTemporaryFile` + `os.replace` (no partial writes).
3. Exit code 1 for all error paths (missing args, invalid name, unknown command).
4. `validate_name` rejects empty and whitespace-containing names.
5. Test coverage: separate test classes for parser, main(), and each data-layer function.
6. `pyproject.toml` registers `todo = "todo_cli.main:main"`.