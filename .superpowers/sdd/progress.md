Task 1: 创建待办事项 (Todo create) — complete (new files Todo.java/TodoRepository.java/TodoService.java/TodoApp.java, review clean)
Minor (final review triage): (1) Todo ctor lacks null-check on name; (2) mutable id used in equals/hashCode; (3) Repository list write-only (dead storage) in create-only scope.
