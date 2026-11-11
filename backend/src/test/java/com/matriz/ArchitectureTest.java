package com.matriz;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Enforces the hexagonal/onion layers (ADR-10): dependencies only point inwards. */
@AnalyzeClasses(packages = "com.matriz", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule onion = onionArchitecture()
            .domainModels("com.matriz.domain..")
            .domainServices("com.matriz.domain..")
            .applicationServices("com.matriz.application..")
            .adapter("rest", "com.matriz.adapter.rest..")
            .adapter("persistence", "com.matriz.adapter.persistence..")
            .adapter("keycloak", "com.matriz.adapter.keycloak..")
            .adapter("scheduler", "com.matriz.adapter.scheduler..");

    @ArchTest
    static final ArchRule domainIsPlainJava = noClasses().that().resideInAPackage("com.matriz.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta..", "javax..", "io.quarkus..", "org.hibernate..", "com.fasterxml..",
                    "org.keycloak..", "org.eclipse.microprofile..")
            .because("the core must not depend on frameworks, persistence or HTTP");

    @ArchTest
    static final ArchRule applicationUsesOnlyPortsAndCdi = noClasses().that().resideInAPackage("com.matriz.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..", "jakarta.ws.rs..", "io.quarkus..", "org.hibernate..",
                    "com.fasterxml..", "org.keycloak..", "org.eclipse.microprofile..")
            .because("services talk to the outside only through ports; CDI and @Transactional are allowed");

    @ArchTest
    static final ArchRule portsAreInterfaces = classes().that().resideInAPackage("com.matriz.application.port..")
            .should().beInterfaces()
            .because("ports only declare what the application offers or needs");

    @ArchTest
    static final ArchRule adaptersDoNotUseServices = noClasses().that().resideInAPackage("com.matriz.adapter..")
            .should().dependOnClassesThat().resideInAPackage("com.matriz.application.service..")
            .because("adapters depend on ports, not on their implementations");
}
