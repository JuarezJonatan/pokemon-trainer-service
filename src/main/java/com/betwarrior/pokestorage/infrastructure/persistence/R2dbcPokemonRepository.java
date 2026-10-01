package com.betwarrior.pokestorage.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.betwarrior.pokestorage.application.exception.SlotAlreadyTakenException;
import com.betwarrior.pokestorage.application.port.PokemonRepository;
import com.betwarrior.pokestorage.domain.pokemon.CaptureOrigin;
import com.betwarrior.pokestorage.domain.pokemon.Gender;
import com.betwarrior.pokestorage.domain.pokemon.Level;
import com.betwarrior.pokestorage.domain.pokemon.MoveSet;
import com.betwarrior.pokestorage.domain.pokemon.PokemonId;
import com.betwarrior.pokestorage.domain.pokemon.PokemonSpecimen;
import com.betwarrior.pokestorage.domain.species.SpeciesRef;
import com.betwarrior.pokestorage.domain.stats.EffortValues;
import com.betwarrior.pokestorage.domain.stats.IndividualValues;
import com.betwarrior.pokestorage.domain.stats.Nature;
import com.betwarrior.pokestorage.domain.stats.StatValues;
import com.betwarrior.pokestorage.domain.storage.StorageArea;
import com.betwarrior.pokestorage.domain.storage.StorageSlot;
import com.betwarrior.pokestorage.domain.trainer.TrainerId;

import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class R2dbcPokemonRepository implements PokemonRepository {

	private static final String SLOT_CONSTRAINT = "uq_pokemon_storage_slot";

	private static final String COLUMNS = """
			id, trainer_id, species_id, species_name, nickname, level,
			iv_hp, iv_attack, iv_defense, iv_special_attack, iv_special_defense, iv_speed,
			ev_hp, ev_attack, ev_defense, ev_special_attack, ev_special_defense, ev_speed,
			nature, ability, gender, shiny,
			original_trainer_id, pokeball, caught_at, met_level, met_location,
			moves, held_item, storage_area, storage_slot""";

	private static final String INSERT = "insert into pokemon (" + COLUMNS + """
			) values (
			:id, :trainer_id, :species_id, :species_name, :nickname, :level,
			:iv_hp, :iv_attack, :iv_defense, :iv_special_attack, :iv_special_defense, :iv_speed,
			:ev_hp, :ev_attack, :ev_defense, :ev_special_attack, :ev_special_defense, :ev_speed,
			:nature, :ability, :gender, :shiny,
			:original_trainer_id, :pokeball, :caught_at, :met_level, :met_location,
			:moves, :held_item, :storage_area, :storage_slot)""";

	private static final String UPDATE = """
			update pokemon set
			species_id = :species_id, species_name = :species_name, nickname = :nickname, level = :level,
			iv_hp = :iv_hp, iv_attack = :iv_attack, iv_defense = :iv_defense,
			iv_special_attack = :iv_special_attack, iv_special_defense = :iv_special_defense, iv_speed = :iv_speed,
			ev_hp = :ev_hp, ev_attack = :ev_attack, ev_defense = :ev_defense,
			ev_special_attack = :ev_special_attack, ev_special_defense = :ev_special_defense, ev_speed = :ev_speed,
			nature = :nature, ability = :ability, gender = :gender, shiny = :shiny,
			original_trainer_id = :original_trainer_id, pokeball = :pokeball, caught_at = :caught_at,
			met_level = :met_level, met_location = :met_location, moves = :moves, held_item = :held_item,
			storage_area = :storage_area, storage_slot = :storage_slot, updated_at = now()
			where id = :id and trainer_id = :trainer_id""";

	private final DatabaseClient database;

	@Override
	public Mono<PokemonSpecimen> insert(PokemonSpecimen pokemon) {
		return execute(INSERT, pokemon);
	}

	@Override
	public Mono<PokemonSpecimen> update(PokemonSpecimen pokemon) {
		return execute(UPDATE, pokemon);
	}

	@Override
	public Mono<PokemonSpecimen> findByOwner(TrainerId owner, PokemonId id) {
		return database.sql("select " + COLUMNS + " from pokemon where id = :id and trainer_id = :trainer_id")
				.bind("id", id.value())
				.bind("trainer_id", owner.value())
				.map(R2dbcPokemonRepository::toPokemon)
				.one();
	}

	@Override
	public Flux<PokemonSpecimen> findInArea(TrainerId owner, StorageArea area, int offset, int limit) {
		return database.sql("select " + COLUMNS
				+ " from pokemon where trainer_id = :trainer_id and storage_area = :area"
				+ " order by storage_slot offset :offset limit :limit")
				.bind("trainer_id", owner.value())
				.bind("area", area.name())
				.bind("offset", offset)
				.bind("limit", limit)
				.map(R2dbcPokemonRepository::toPokemon)
				.all();
	}

	@Override
	public Mono<Long> countInArea(TrainerId owner, StorageArea area) {
		return database.sql("select count(*) as total from pokemon where trainer_id = :trainer_id and storage_area = :area")
				.bind("trainer_id", owner.value())
				.bind("area", area.name())
				.map(row -> row.get("total", Long.class))
				.one();
	}

	@Override
	public Mono<Map<PokemonId, StorageSlot>> occupiedSlots(TrainerId owner) {
		return database.sql("select id, storage_area, storage_slot from pokemon where trainer_id = :trainer_id")
				.bind("trainer_id", owner.value())
				.map(row -> Map.entry(new PokemonId(row.get("id", UUID.class)), slotOf(row)))
				.all()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public Flux<PokemonSpecimen> findAll(int offset, int limit) {
		return database.sql("select " + COLUMNS + " from pokemon order by created_at, id offset :offset limit :limit")
				.bind("offset", offset)
				.bind("limit", limit)
				.map(R2dbcPokemonRepository::toPokemon)
				.all();
	}

	@Override
	public Mono<Long> count() {
		return database.sql("select count(*) as total from pokemon")
				.map(row -> row.get("total", Long.class))
				.one();
	}

	private Mono<PokemonSpecimen> execute(String sql, PokemonSpecimen pokemon) {
		return bind(database.sql(sql), pokemon)
				.fetch()
				.rowsUpdated()
				.thenReturn(pokemon)
				.onErrorMap(R2dbcPokemonRepository::isSlotConflict, SlotAlreadyTakenException::new);
	}

	private static boolean isSlotConflict(Throwable error) {
		return error instanceof DataIntegrityViolationException
				&& String.valueOf(error.getMessage()).contains(SLOT_CONSTRAINT);
	}

	private static GenericExecuteSpec bind(GenericExecuteSpec spec, PokemonSpecimen pokemon) {
		StatValues ivs = pokemon.individualValues().values();
		StatValues evs = pokemon.effortValues().values();
		CaptureOrigin origin = pokemon.origin();
		return bindNullable(bindNullable(spec, "nickname", pokemon.nickname()), "held_item", pokemon.heldItem())
				.bind("id", pokemon.id().value())
				.bind("trainer_id", pokemon.owner().value())
				.bind("species_id", pokemon.species().id())
				.bind("species_name", pokemon.species().name())
				.bind("level", (short) pokemon.level().value())
				.bind("iv_hp", (short) ivs.hp())
				.bind("iv_attack", (short) ivs.attack())
				.bind("iv_defense", (short) ivs.defense())
				.bind("iv_special_attack", (short) ivs.specialAttack())
				.bind("iv_special_defense", (short) ivs.specialDefense())
				.bind("iv_speed", (short) ivs.speed())
				.bind("ev_hp", (short) evs.hp())
				.bind("ev_attack", (short) evs.attack())
				.bind("ev_defense", (short) evs.defense())
				.bind("ev_special_attack", (short) evs.specialAttack())
				.bind("ev_special_defense", (short) evs.specialDefense())
				.bind("ev_speed", (short) evs.speed())
				.bind("nature", pokemon.nature().name())
				.bind("ability", pokemon.ability())
				.bind("gender", pokemon.gender().name())
				.bind("shiny", pokemon.shiny())
				.bind("original_trainer_id", origin.originalTrainerId())
				.bind("pokeball", origin.pokeball())
				.bind("caught_at", origin.caughtAt().atOffset(ZoneOffset.UTC))
				.bind("met_level", (short) origin.metLevel().value())
				.bind("met_location", origin.metLocation())
				.bind("moves", pokemon.moves().moves().toArray(String[]::new))
				.bind("storage_area", pokemon.slot().area().name())
				.bind("storage_slot", (short) pokemon.slot().position());
	}

	private static GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, Optional<String> value) {
		return value.map(present -> spec.bind(name, present)).orElseGet(() -> spec.bindNull(name, String.class));
	}

	private static PokemonSpecimen toPokemon(Readable row) {
		return new PokemonSpecimen(
				new PokemonId(row.get("id", UUID.class)),
				new TrainerId(row.get("trainer_id", UUID.class)),
				new SpeciesRef(row.get("species_id", Integer.class), row.get("species_name", String.class)),
				Optional.ofNullable(row.get("nickname", String.class)),
				new Level(smallint(row, "level")),
				new IndividualValues(stats(row, "iv_")),
				new EffortValues(stats(row, "ev_")),
				Nature.valueOf(row.get("nature", String.class)),
				row.get("ability", String.class),
				Gender.valueOf(row.get("gender", String.class)),
				row.get("shiny", Boolean.class),
				new CaptureOrigin(
						row.get("original_trainer_id", String.class),
						row.get("pokeball", String.class),
						row.get("caught_at", OffsetDateTime.class).toInstant(),
						new Level(smallint(row, "met_level")),
						row.get("met_location", String.class)),
				new MoveSet(List.of(row.get("moves", String[].class))),
				Optional.ofNullable(row.get("held_item", String.class)),
				slotOf(row));
	}

	private static StorageSlot slotOf(Readable row) {
		return new StorageSlot(StorageArea.valueOf(row.get("storage_area", String.class)), smallint(row, "storage_slot"));
	}

	private static StatValues stats(Readable row, String prefix) {
		return new StatValues(
				smallint(row, prefix + "hp"),
				smallint(row, prefix + "attack"),
				smallint(row, prefix + "defense"),
				smallint(row, prefix + "special_attack"),
				smallint(row, prefix + "special_defense"),
				smallint(row, prefix + "speed"));
	}

	private static int smallint(Readable row, String column) {
		return row.get(column, Short.class);
	}

}
