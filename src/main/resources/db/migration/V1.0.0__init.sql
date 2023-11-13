create table todo_items (
    id         uuid          constraint todo_items_pkey primary key,
    content    varchar(255),
    done       boolean,
    created_at timestamp
);
