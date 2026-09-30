package com.betwarrior.pokeapi.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

import com.betwarrior.pokeapi.Fixtures;
import com.betwarrior.pokeapi.model.evolution.EvolutionChain;
import com.betwarrior.pokeapi.model.items.Item;
import com.betwarrior.pokeapi.model.locations.LocationArea;
import com.betwarrior.pokeapi.model.pokemon.Pokemon;
import com.betwarrior.pokeapi.model.species.PokemonSpecies;
import com.betwarrior.pokeapi.model.utility.Language;

class PokemonModelTest {

	private final Pokemon pikachu = Fixtures.read("pokemon", Pokemon.class);
	private final PokemonSpecies pikachuSpecies = Fixtures.read("pokemon-species", PokemonSpecies.class);

	@Test
	void givenASpeciesWithSeveralVarieties_whenAskingForItsDefault_thenTheOneMarkedAsDefaultIsReturned() {
		assertThat(pikachuSpecies.defaultVariety().name()).isEqualTo("pikachu");
		assertThat(pikachuSpecies.defaultVariety().id()).isEqualTo(25);
	}

	@Test
	void givenAnEvolvedSpecies_whenAskingWhatItEvolvesFrom_thenThePreviousStageIsReturned() {
		assertThat(pikachuSpecies.evolvesFrom()).hasValueSatisfying(pichu -> assertThat(pichu.name()).isEqualTo("pichu"));
	}

	@Test
	void givenAPokemon_whenAskingForItsBaseStats_thenTheyAreKeyedByStatName() {
		assertThat(pikachu.baseStats()).containsExactly(
				entry("hp", 35),
				entry("attack", 55),
				entry("defense", 40));
	}

	@Test
	void givenAPokemon_whenAskingWhetherItLearnsAMove_thenItsMoveListIsChecked() {
		assertThat(pikachu.learns("thunder-punch")).isTrue();
		assertThat(pikachu.learns("surf")).isFalse();
		assertThat(pikachu.moveNames()).contains("mega-punch", "pay-day", "thunder-punch");
	}

	@Test
	void givenAPokemon_whenAskingForItsTypes_thenTheyFollowSlotOrder() {
		assertThat(pikachu.typeNames()).containsExactly("electric");
		assertThat(pikachu.frontSprite()).isPresent();
	}

	@Test
	void givenAPokemonWithBooleanFlags_whenMapped_thenTheFlagsAreRead() {
		assertThat(pikachu.isDefault()).isTrue();
		assertThat(pikachu.abilities()).anySatisfy(ability -> assertThat(ability.isHidden()).isTrue());
	}

	@Test
	void givenALocalizedResource_whenAskingForItsNameInALanguage_thenTheTranslationIsReturned() {
		assertThat(pikachuSpecies.nameIn("KO")).contains("피카츄");
		assertThat(pikachuSpecies.nameIn("tlh")).isEmpty();
	}

	@Test
	void givenAnItem_whenMapped_thenItsCategoryIsASingleLink() {
		Item masterBall = Fixtures.read("item", Item.class);

		assertThat(masterBall.category().name()).isEqualTo("standard-balls");
		assertThat(masterBall.prices()).isNotEmpty();
	}

	@Test
	void givenAnEvolutionChain_whenMapped_thenItsFirstStageIsRead() {
		EvolutionChain chain = Fixtures.read("evolution-chain", EvolutionChain.class);

		assertThat(chain.chain().species().name()).isEqualTo("bulbasaur");
		assertThat(chain.chain().evolvesTo()).extracting(link -> link.species().name()).containsExactly("ivysaur");
	}

	@Test
	void givenALocationArea_whenMapped_thenItsIdAndEncounterRatesAreRead() {
		LocationArea area = Fixtures.read("location-area", LocationArea.class);

		assertThat(area.id()).isEqualTo(1);
		assertThat(area.encounterMethodRates()).isNotEmpty();
	}

	@Test
	void givenAResourceWithoutSomeLists_whenMapped_thenTheListsAreEmptyAndImmutable() {
		Language language = new Language(1, "ja", true, null, "ja", "jp");

		assertThat(language.names()).isEmpty();
		assertThatThrownBy(() -> pikachu.moves().clear())
				.isInstanceOf(UnsupportedOperationException.class);
	}

}
