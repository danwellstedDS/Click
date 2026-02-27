package com.derbysoft.click.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * Enforces bounded context boundary rules:
 *
 * <ul>
 *   <li>BC1 (identity-access) must only access BC3 (organisation-structure) via its
 *       {@code api/} contracts and ports — never via domain, infrastructure, or interfaces.
 *   <li>BC3 domain layer must not depend on BC1 domain or infrastructure.
 *   <li>BC3 domain layer must not depend on BC2 (tenant-governance) internals.
 * </ul>
 *
 * <p><strong>Note:</strong> BC3's application and interfaces layers intentionally reference
 * BC1's {@code infrastructure.security.UserPrincipal} for Spring Security integration
 * ({@code @AuthenticationPrincipal}). This cross-layer usage is a pragmatic trade-off and
 * is not enforced here. Moving {@code UserPrincipal} to BC1's {@code api/contracts} would
 * eliminate this coupling if stricter isolation is desired in the future.
 */
class BoundaryRulesTest {

  private static final JavaClasses classes = new ClassFileImporter()
      .importPackages("com.derbysoft.click.modules");

  @Test
  void identityAccessShouldNotDirectlyImportOrganisationStructureDomainOrInfrastructure() {
    noClasses()
        .that().resideInAPackage("com.derbysoft.click.modules.identityaccess..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "com.derbysoft.click.modules.organisationstructure.domain..",
            "com.derbysoft.click.modules.organisationstructure.infrastructure..",
            "com.derbysoft.click.modules.organisationstructure.interfaces..",
            "com.derbysoft.click.modules.organisationstructure.application.."
        )
        .check(classes);
  }

  @Test
  void identityAccessShouldNotDirectlyImportTenantGovernanceInternals() {
    noClasses()
        .that().resideInAPackage("com.derbysoft.click.modules.identityaccess..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "com.derbysoft.click.modules.tenantgovernance.domain..",
            "com.derbysoft.click.modules.tenantgovernance.infrastructure..",
            "com.derbysoft.click.modules.tenantgovernance.interfaces..",
            "com.derbysoft.click.modules.tenantgovernance.application.."
        )
        .check(classes);
  }

  @Test
  void organisationStructureDomainShouldBeIndependentOfOtherBCDomains() {
    noClasses()
        .that().resideInAPackage("com.derbysoft.click.modules.organisationstructure.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "com.derbysoft.click.modules.identityaccess..",
            "com.derbysoft.click.modules.tenantgovernance.."
        )
        .check(classes);
  }

  @Test
  void tenantGovernanceDomainShouldBeIndependentOfOtherBCDomains() {
    noClasses()
        .that().resideInAPackage("com.derbysoft.click.modules.tenantgovernance.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "com.derbysoft.click.modules.identityaccess..",
            "com.derbysoft.click.modules.organisationstructure.."
        )
        .check(classes);
  }
}
