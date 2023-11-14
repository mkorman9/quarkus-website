create table todo_items (
    id         uuid          constraint todo_items_pkey primary key,
    content    varchar(255),
    done       boolean,
    created_at timestamp
);

create table todo_items_actions (
    id           uuid         constraint todo_items_actions_pkey primary key,
    item_id      uuid         constraint todo_items_actions__todo_items_fkey
        references todo_items(id) on delete cascade on update cascade,
    action_type  varchar(16),
    target_value boolean,
    created_at   timestamp
);
