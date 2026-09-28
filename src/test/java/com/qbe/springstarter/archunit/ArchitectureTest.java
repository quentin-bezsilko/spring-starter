package com.qbe.springstarter.archunit;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.qbe.springstarter.constants.ArchUnitConstants;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.mapstruct.Mapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = ArchUnitConstants.ROOT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // Annotations

    @ArchTest
    void clientsShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CLIENT)
                .should()
                .beAnnotatedWith(Component.class)
                .check(classes);
    }

    @ArchTest
    void configurationsShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONFIG)
                .should()
                .beAnnotatedWith(Configuration.class)
                .check(classes);
    }

    @ArchTest
    void controllersShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .and()
                .areNotInterfaces()
                .should()
                .beAnnotatedWith(RestController.class)
                .check(classes);
    }

    @ArchTest
    void entitiesShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_ENTITY)
                .should()
                .beAnnotatedWith(Entity.class)
                .check(classes);
    }

    @ArchTest
    void mappersShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_MAPPER)
                .and()
                .areInterfaces()
                .should()
                .beAnnotatedWith(Mapper.class)
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .and()
                .areNotInterfaces()
                .should()
                .beAnnotatedWith(Repository.class)
                .check(classes);
    }

    @ArchTest
    void servicesShouldBeAnnotated(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_SERVICE)
                .and()
                .areNotInterfaces()
                .should()
                .beAnnotatedWith(Service.class)
                .check(classes);
    }

    // Dependencies

    @ArchTest
    void controllersShouldNotDependOnRepositories(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .check(classes);
    }

    @ArchTest
    void controllerShouldNotDependOnEntities(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_ENTITY)
                .check(classes);
    }

    @ArchTest
    void controllerShouldNotDependOnMappers(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_MAPPER)
                .check(classes);
    }

    @ArchTest
    void controllerShouldNotDependOnConfig(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONFIG)
                .check(classes);
    }

    @ArchTest
    void servicesShouldNotDependOnController(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_SERVICE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .check(classes);
    }

    @ArchTest
    void servicesShouldNotDependOnProperties(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_SERVICE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_PROPERTIES)
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldNotDependOnClient(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CLIENT)
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldNotAccessControllers(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldNotAccessServices(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_SERVICE)
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldNotAccessProperties(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_PROPERTIES)
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldNotAccessClient(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CLIENT)
                .check(classes);
    }

    // Should end with

    @ArchTest
    void controllersShouldEndWithController(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONTROLLER)
                .should()
                .haveSimpleNameEndingWith("Controller")
                .check(classes);
    }

    @ArchTest
    void servicesShouldEndWithService(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_SERVICE)
                .should()
                .haveSimpleNameEndingWith("Service")
                .check(classes);
    }

    @ArchTest
    void repositoriesShouldEndWithRepository(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_REPOSITORY)
                .should()
                .haveSimpleNameEndingWith("Repository")
                .check(classes);
    }

    @ArchTest
    void clientsShouldEndWithClient(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CLIENT)
                .should()
                .haveSimpleNameEndingWith("Client")
                .check(classes);
    }

    @ArchTest
    void configurationsShouldEndWithConfiguration(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONFIG)
                .should()
                .haveSimpleNameEndingWith("Config")
                .check(classes);
    }

    @ArchTest
    void constantsShouldEndWithConstants(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_CONSTANTS)
                .should()
                .haveSimpleNameEndingWith("Constants")
                .check(classes);
    }

    @ArchTest
    void dtosShouldEndWithDto(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_DTO)
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("Dto")
                .check(classes);
    }

    @ArchTest
    void entitiesShouldEndWithEntity(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_ENTITY)
                .should()
                .haveSimpleNameEndingWith("Entity")
                .check(classes);
    }

    @ArchTest
    void propertiesShouldEndWithProperties(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_PROPERTIES)
                .should()
                .haveSimpleNameEndingWith("Properties")
                .check(classes);
    }

    // Should be

    @ArchTest
    void dtoShouldBeRecords(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage(ArchUnitConstants.PACKAGE_DTO)
                .and()
                .areTopLevelClasses()
                .should(new ArchCondition<>("be records") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (!item.reflect().isRecord()) {
                            events.add(SimpleConditionEvent.violated(item, item.getName() + " is not a record"));
                        }
                    }
                })
                .check(classes);
    }

    // No cycle

    @ArchTest
    void noCycles(JavaClasses classes) {
        slices().matching("com.qbe.springstarter.(*)..")
                .should()
                .beFreeOfCycles()
                .ignoreDependency(resideInAnyPackage(ArchUnitConstants.PACKAGE_VALIDATOR), alwaysTrue());
    }
}
