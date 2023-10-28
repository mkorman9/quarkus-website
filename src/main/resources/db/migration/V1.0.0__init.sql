create table todo_items (
    id         uuid          primary key,
    content    varchar(255),
    done       boolean,
    created_at timestamp
);
