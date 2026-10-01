-- Optimistic lock: every update must match the version it read, and increments it.
alter table pokemon add column version bigint not null default 0;
