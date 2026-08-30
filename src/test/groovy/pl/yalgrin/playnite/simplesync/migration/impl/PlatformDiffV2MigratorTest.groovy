package pl.yalgrin.playnite.simplesync.migration.impl

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.repository.PlatformDiffRepository
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode

class PlatformDiffV2MigratorTest extends SpockIntegrationTest {
    private static final PLAYNITE_ID = "123"
    private static final NAME = "Obiekt"
    private static final int SRC_MODEL_VERSION = 1
    private static final int TARGET_MODEL_VERSION = 2
    private static final String DIFF_JSON = """
    {
      "Id": "123",
      "Name": "Obiekt",
      "BaseObjectId": 1,
      "ChangedFields": [
        "Id",
        "Name",
        "SpecificationId"
      ],
      "SpecificationId": "SpecificationId",
      "Removed": false
    }
"""

    @Autowired
    private ConnectionFactory connectionFactory
    @Autowired
    private PlatformDiffRepository repository
    @Autowired
    private JsonMapper jsonMapper
    @Autowired
    private PlatformDiffV2Migrator migrator

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()
    }

    def "should migrate JSON properly"() {
        given:
        PlatformDiff entity = new PlatformDiff()
        entity.setPlayniteId(PLAYNITE_ID)
        entity.setName(NAME)
        entity.setModelVersion(SRC_MODEL_VERSION)
        entity.setDiffData(Json.of(DIFF_JSON))
        entity = repository.save(entity).block()

        when:
        migrator.migrate(entity).block()

        then:
        StepVerifier.create(repository.findById(entity.id))
                .expectNextMatches {
                    assert entity.id == it.id
                    assert entity.playniteId == it.playniteId
                    assert entity.name == it.name
                    assert entity.modelVersion == TARGET_MODEL_VERSION
                    assert entity.diffData != null
                    def node = jsonMapper.readTree(entity.diffData.asArray())
                    assert node instanceof ObjectNode
                    assert node.get("Id") == null
                    assert node.get("Name") == null
                    assert node.get("Removed") == null
                    assert node.get("BaseObjectId") == null
                    assert node.get("ChangedFields") == null
                    assert node.get("SpecificationId") == null
                    assert node.get("baseObjectId")?.intValue() == 1
                    assert node.get("specificationId")?.stringValue() == "SpecificationId"
                    def changedFields = node.get("changedFields")
                    assert changedFields != null
                    assert changedFields instanceof ArrayNode
                    assert changedFields.size() == 3
                    assert changedFields.get(0)?.stringValue() == "Id"
                    assert changedFields.get(1)?.stringValue() == "Name"
                    assert changedFields.get(2)?.stringValue() == "SpecificationId"
                    true
                }
                .verifyComplete()
    }
}
