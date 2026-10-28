package pt.matriz;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Enforces the onion layers (ADR-10): dependencies only point inwards. */
@AnalyzeClasses(packages = "pt.matriz", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule onion = onionArchitecture()
            .domainModels("pt.matriz.domain..")
            .domainServices("pt.matriz.domain..")
            .applicationServices("pt.matriz.application..")
            .adapter("api", "pt.matriz.infrastructure.api..")
            .adapter("persistence", "pt.matriz.infrastructure.persistence..")
            .adapter("keycloak", "pt.matriz.infrastructure.keycloak..");

    @ArchTest
    static final ArchRule domainIsPlainJava = noClasses().that().resideInAPackage("pt.matriz.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta..", "javax..", "io.quarkus..", "org.hibernate..", "com.fasterxml..",
                    "org.keycloak..", "org.eclipse.microprofile..")
            .because("the core must not depend on frameworks, persistence or HTTP");

    @ArchTest
    static final ArchRule applicationUsesOnlyPortsAndCdi = noClasses().that().resideInAPackage("pt.matriz.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..", "jakarta.ws.rs..", "io.quarkus..", "org.hibernate..",
                    "com.fasterxml..", "org.keycloak..", "org.eclipse.microprofile..")
            .because("services talk to the outside only through ports; CDI and @Transactional are allowed");
}
