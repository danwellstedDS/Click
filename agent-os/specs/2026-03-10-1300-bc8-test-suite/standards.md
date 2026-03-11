# BC8 Test Suite — Standards

## Framework
- JUnit 5 (`@Test`, `@BeforeEach`, `@ExtendWith`)
- Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`)
- AssertJ (`assertThat`, `assertThatThrownBy`)

## Rules
- No mocks in domain tests — test aggregates and value objects directly
- Mock only ports and repositories in application tests
- Instantiate pure services (QualityValidator, Normalizer, BatchAssembler) directly
- Use `ArgumentCaptor<EventEnvelope<?>>` for event verification
- Follow `given/when/then` structure implicit in method naming

## Naming Conventions
- Test methods: `shouldDescribeBehaviourInCamelCase()`
- Test classes: `{SubjectClass}Test`
