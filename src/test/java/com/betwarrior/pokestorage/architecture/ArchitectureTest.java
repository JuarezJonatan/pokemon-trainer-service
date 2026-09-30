package com.betwarrior.pokestorage.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.betwarrior.pokestorage", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule theDomainDoesNotDependOnFrameworksOrThePokeApiLibrary = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("org.springframework..", "reactor..", "io.r2dbc..", "skaro.pokeapi..",
					"com.fasterxml.jackson..", "jakarta..")
			.as("the domain does not depend on frameworks or the PokeAPI library");

}
