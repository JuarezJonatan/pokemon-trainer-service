package com.betwarrior.pokestorage.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = { "com.betwarrior.pokestorage", "com.betwarrior.pokeapi" },
		importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule layersOnlyDependInwards = layeredArchitecture()
			.consideringOnlyDependenciesInLayers()
			.layer("Web").definedBy("com.betwarrior.pokestorage.web..")
			.layer("Application").definedBy("com.betwarrior.pokestorage.application..")
			.layer("Domain").definedBy("com.betwarrior.pokestorage.domain..")
			.layer("Infrastructure").definedBy("com.betwarrior.pokestorage.infrastructure..")
			.layer("PokeApiClient").definedBy("com.betwarrior.pokeapi..")
			.whereLayer("Web").mayNotBeAccessedByAnyLayer()
			.whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
			.whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Infrastructure")
			.whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Web", "Infrastructure")
			.whereLayer("PokeApiClient").mayOnlyBeAccessedByLayers("Infrastructure");

	@ArchTest
	static final ArchRule theDomainDoesNotDependOnFrameworksOrThePokeApiClient = noClasses()
			.that().resideInAPackage("com.betwarrior.pokestorage.domain..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("org.springframework..", "reactor..", "io.r2dbc..", "com.betwarrior.pokeapi..",
					"com.fasterxml.jackson..", "jakarta..")
			.as("the domain does not depend on frameworks or the PokeAPI client");

	@ArchTest
	static final ArchRule theApplicationDoesNotKnowHowDataIsStoredOrFetched = noClasses()
			.that().resideInAPackage("com.betwarrior.pokestorage.application..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("org.springframework.r2dbc..", "io.r2dbc..", "org.springframework.web..",
					"com.betwarrior.pokeapi..")
			.as("the application layer does not know about databases, HTTP or PokeAPI types");

	@ArchTest
	static final ArchRule theServiceDoesNotUseTheDeprecatedLibrary = noClasses()
			.that().resideInAnyPackage("com.betwarrior.pokestorage..", "com.betwarrior.pokeapi..")
			.should().dependOnClassesThat().resideInAPackage("skaro.pokeapi..")
			.as("neither the service nor the new client depend on the deprecated skaro.pokeapi library");

	@ArchTest
	static final ArchRule thePokeApiClientDoesNotKnowTheService = noClasses()
			.that().resideInAPackage("com.betwarrior.pokeapi..")
			.should().dependOnClassesThat().resideInAPackage("com.betwarrior.pokestorage..")
			.as("the PokeAPI client is reusable: it does not know the service");

}
