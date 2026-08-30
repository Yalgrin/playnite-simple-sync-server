package pl.yalgrin.playnite.simplesync.migration.impl

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode

class GameV2MigratorTest extends SpockIntegrationTest {
    private static final PLAYNITE_ID = "123"
    private static final NAME = "Obiekt"
    private static final int SRC_MODEL_VERSION = 1
    private static final int TARGET_MODEL_VERSION = 2
    private static final String SAVED_DATA_JSON = """
    {
      "Id": "123",
      "Name": "Obiekt",
      "GameId": "1",
      "PluginId": "plugin",
      "Removed": false,
      "IncludeLibraryPluginAction": false,
      "HasIcon": true,
      "HasCoverImage": false,
      "HasBackgroundImage": true,
      "Description": "Opis",
      "Notes": "Notatki",
      "Genres": [
        {
          "Id": "1",
          "Name": "Akcja"
        },
        {
          "Id": "2",
          "Name": "RPG"
        }
      ],
      "Platforms": [
        {
          "Id": "3",
          "Name": "PC",
          "SpecificationId": "spec"
        }
      ],
      "Source": {
        "Id": "4",
        "Name": "Source"
      },
      "Links": [
        {
            "Name": "Steam store",
            "Url": "https://example.com"
        },
        {
            "Name": "Wikipedia",
            "Url": "https://example2.com"
        } 
      ],
      "CompletionStatus": {
        "Id": "5",
        "Name": "Completed"
      },
      "Hidden": true,
      "Favorite": false,
      "Playtime": 100,
      "UserScore": 80,
      "Manual": true
    }
"""

    @Autowired
    private ConnectionFactory connectionFactory
    @Autowired
    private GameRepository repository
    @Autowired
    private JsonMapper jsonMapper
    @Autowired
    private GameV2Migrator migrator

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()
    }

    def "should migrate JSON properly"() {
        given:
        Game entity = new Game()
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
                    assert node.get("GameId") == null
                    assert node.get("PluginId") == null
                    assert node.get("Removed") == null
                    assert node.get("IncludeLibraryPluginAction") == null
                    assert node.get("HasIcon") == null
                    assert node.get("HasCoverImage") == null
                    assert node.get("HasBackgroundImage") == null
                    assert node.get("Description") == null
                    assert node.get("Notes") == null
                    assert node.get("Genres") == null
                    assert node.get("Platforms") == null
                    assert node.get("Links") == null
                    assert node.get("Hidden") == null
                    assert node.get("Favorite") == null
                    assert node.get("Playtime") == null
                    assert node.get("UserScore") == null
                    assert node.get("Manual") == null
                    assert node.get("description")?.stringValue() == "Opis"
                    assert node.get("notes")?.stringValue() == "Notatki"
                    assert node.get("isHidden")?.booleanValue() == true
                    assert node.get("isFavorite")?.booleanValue() == false
                    assert node.get("playtime")?.intValue() == 100
                    assert node.get("userScore")?.intValue() == 80
                    assert node.get("manual")?.booleanValue() == true

                    def genres = node.get("genres")
                    assert genres instanceof ArrayNode
                    assert genres.size() == 2
                    def genre1 = genres.get(0)
                    assert genre1.get("Id") == null
                    assert genre1.get("Name") == null
                    assert genre1.get("id")?.stringValue() == "1"
                    assert genre1.get("name")?.stringValue() == "Akcja"
                    def genre2 = genres.get(1)
                    assert genre2.get("Id") == null
                    assert genre2.get("Name") == null
                    assert genre2.get("id")?.stringValue() == "2"
                    assert genre2.get("name")?.stringValue() == "RPG"

                    def platforms = node.get("platforms")
                    assert platforms instanceof ArrayNode
                    assert platforms.size() == 1
                    def platform = platforms.get(0)
                    assert platform.get("Id") == null
                    assert platform.get("Name") == null
                    assert platform.get("id")?.stringValue() == "3"
                    assert platform.get("name")?.stringValue() == "PC"
                    assert platform.get("specificationId")?.stringValue() == "spec"

                    def source = node.get("source")
                    assert source instanceof ObjectNode
                    assert source.get("Id") == null
                    assert source.get("Name") == null
                    assert source.get("id")?.stringValue() == "4"
                    assert source.get("name")?.stringValue() == "Source"

                    def links = node.get("links")
                    assert links instanceof ArrayNode
                    assert links.size() == 2
                    def link1 = links.get(0)
                    assert link1.get("Name") == null
                    assert link1.get("Url") == null
                    assert link1.get("name")?.stringValue() == "Steam store"
                    assert link1.get("url")?.stringValue() == "https://example.com"
                    def link2 = links.get(1)
                    assert link2.get("Name") == null
                    assert link2.get("Url") == null
                    assert link2.get("name")?.stringValue() == "Wikipedia"
                    assert link2.get("url")?.stringValue() == "https://example2.com"

                    def completionStatus = node.get("completionStatus")
                    assert completionStatus instanceof ObjectNode
                    assert completionStatus.get("Id") == null
                    assert completionStatus.get("Name") == null
                    assert completionStatus.get("id")?.stringValue() == "5"
                    assert completionStatus.get("name")?.stringValue() == "Completed"
                    true
                }
                .verifyComplete()
    }
}