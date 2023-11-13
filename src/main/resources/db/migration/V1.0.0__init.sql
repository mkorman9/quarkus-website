create table todo_items (
    id         uuid          constraint todo_items_pkey primary key,
    content    varchar(255),
    done       boolean,
    created_at timestamp
);

create table todo_items_mark_actions (
    id           uuid constraint todo_items_mark_actions_pkey primary key,
    item_id      uuid constraint todo_items_mark_actions__todo_items_fkey references todo_items(id) on delete cascade,
    target_value boolean,
    created_at   timestamp
);
