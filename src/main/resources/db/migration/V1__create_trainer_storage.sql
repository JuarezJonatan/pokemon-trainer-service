create table trainer (
    id         uuid         primary key,
    name       varchar(100) not null,
    created_at timestamptz  not null default now()
);

create table pokemon (
    id                  uuid          primary key,
    trainer_id          uuid          not null references trainer (id),
    species_id          integer       not null,
    species_name        varchar(100)  not null,
    nickname            varchar(40),
    level               smallint      not null check (level between 1 and 100),

    iv_hp               smallint      not null check (iv_hp between 0 and 31),
    iv_attack           smallint      not null check (iv_attack between 0 and 31),
    iv_defense          smallint      not null check (iv_defense between 0 and 31),
    iv_special_attack   smallint      not null check (iv_special_attack between 0 and 31),
    iv_special_defense  smallint      not null check (iv_special_defense between 0 and 31),
    iv_speed            smallint      not null check (iv_speed between 0 and 31),

    ev_hp               smallint      not null check (ev_hp between 0 and 252),
    ev_attack           smallint      not null check (ev_attack between 0 and 252),
    ev_defense          smallint      not null check (ev_defense between 0 and 252),
    ev_special_attack   smallint      not null check (ev_special_attack between 0 and 252),
    ev_special_defense  smallint      not null check (ev_special_defense between 0 and 252),
    ev_speed            smallint      not null check (ev_speed between 0 and 252),

    nature              varchar(20)   not null,
    ability             varchar(100)  not null,
    gender              varchar(10)   not null check (gender in ('MALE', 'FEMALE', 'GENDERLESS')),
    shiny               boolean       not null,

    original_trainer_id varchar(100)  not null,
    pokeball            varchar(100)  not null,
    caught_at           timestamptz   not null,
    met_level           smallint      not null check (met_level between 1 and 100),
    met_location        varchar(100)  not null,

    moves               varchar(100)[] not null check (cardinality(moves) between 1 and 4),
    held_item           varchar(100),

    storage_area        varchar(4)    not null check (storage_area in ('TEAM', 'BOX')),
    storage_slot        smallint      not null check (storage_slot >= 1),

    created_at          timestamptz   not null default now(),
    updated_at          timestamptz   not null default now(),

    constraint ck_pokemon_ev_total check (
        ev_hp + ev_attack + ev_defense + ev_special_attack + ev_special_defense + ev_speed <= 510),
    constraint uq_pokemon_storage_slot unique (trainer_id, storage_area, storage_slot)
);
