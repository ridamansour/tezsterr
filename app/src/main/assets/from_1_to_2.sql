ALTER TABLE todo_list DROP COLUMN description;

ALTER TABLE todo_task ADD in_trash INTEGER NOT NULL DEFAULT 0;

ALTER TABLE todo_subtask ADD in_trash INTEGER NOT NULL DEFAULT 0;