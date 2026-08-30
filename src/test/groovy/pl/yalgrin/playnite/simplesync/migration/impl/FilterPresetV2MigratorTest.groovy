package pl.yalgrin.playnite.simplesync.migration.impl

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.library.repository.FilterPresetRepository
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode

class FilterPresetV2MigratorTest extends SpockIntegrationTest {
    private static final PLAYNITE_ID = "123"
    private static final NAME = "Obiekt"
    private static final int SRC_MODEL_VERSION = 1
    private static final int TARGET_MODEL_VERSION = 2
    private static final String SAVED_DATA_JSON = """
    {
      "Id": "123",
      "Name": "Obiekt",
      "Removed": false,
      "Settings": {
        "UseAndFilteringStyle": true,
        "IsInstalled": true,
        "Favorite": false,
        "Name": "Nazwa",
        "Version": "1.0",
        "ReleaseYear": {
          "Values": ["2000", "2001"]
        },
        "Genre": {
          "Ids": ["123", "456"]
        },
        "Category": {
          "Text": "Kategoria"
        }
      },
      "SortingOrder": "Name",
      "SortingOrderDirection": "Ascending",
      "GroupingOrder": "Category",
      "ShowInFullscreenQuickSelection": false
    }
"""

    @Autowired
    private ConnectionFactory connectionFactory
    @Autowired
    private FilterPresetRepository repository
    @Autowired
    private JsonMapper jsonMapper
    @Autowired
    private FilterPresetV2Migrator migrator

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()
    }

    def "should migrate JSON properly"() {
        given:
        FilterPreset entity = new FilterPreset()
        entity.setPlayniteId(PLAYNITE_ID)
        entity.setName(NAME)
        entity.setModelVersion(SRC_MODEL_VERSION)
        entity.setSavedData(Json.of(SAVED_DATA_JSON))
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
                    assert entity.savedData != null
                    def node = jsonMapper.readTree(entity.savedData.asArray())
                    assert node instanceof ObjectNode
                    assert node.get("Id") == null
                    assert node.get("Name") == null
                    assert node.get("Removed") == null
                    assert node.get("Settings") == null
                    assert node.get("SortingOrder") == null
                    assert node.get("SortingOrderDirection") == null
                    assert node.get("GroupingOrder") == null
                    assert node.get("ShowInFullscreenQuickSelection") == null
                    assert node.get("settings") != null
                    assert node.get("sortingOrder")?.stringValue() == "Name"
                    assert node.get("sortingOrderDirection")?.stringValue() == "Ascending"
                    assert node.get("groupingOrder")?.stringValue() == "Category"
                    assert node.get("showInFullscreenQuickSelection")?.booleanValue() == false

                    def settings = node.get("settings")
                    assert settings instanceof ObjectNode
                    assert settings.get("UseAndFilteringStyle") == null
                    assert settings.get("IsInstalled") == null
                    assert settings.get("Favorite") == null
                    assert settings.get("Name") == null
                    assert settings.get("Version") == null
                    assert settings.get("ReleaseYear") == null
                    assert settings.get("Genre") == null
                    assert settings.get("Category") == null
                    assert settings.get("useAndFilteringStyle")?.booleanValue() == true
                    assert settings.get("isInstalled")?.booleanValue() == true
                    assert settings.get("isFavorite")?.booleanValue() == false
                    assert settings.get("name")?.stringValue() == "Nazwa"
                    assert settings.get("version")?.stringValue() == "1.0"

                    def releaseYear = settings.get("releaseYear")
                    assert releaseYear instanceof ObjectNode
                    assert releaseYear.get("Values") == null
                    assert releaseYear.get("values") instanceof ArrayNode
                    assert releaseYear.get("values").size() == 2
                    assert releaseYear.get("values").get(0)?.stringValue() == "2000"
                    assert releaseYear.get("values").get(1)?.stringValue() == "2001"

                    def genre = settings.get("genre")
                    assert genre instanceof ObjectNode
                    assert genre.get("Ids") == null
                    assert genre.get("ids") instanceof ArrayNode
                    assert genre.get("ids").size() == 2
                    assert genre.get("ids").get(0)?.stringValue() == "123"
                    assert genre.get("ids").get(1)?.stringValue() == "456"

                    def category = settings.get("category")
                    assert category instanceof ObjectNode
                    assert category.get("Text") == null
                    assert category.get("text")?.stringValue() == "Kategoria"
                    true
                }
                .verifyComplete()
    }
}
