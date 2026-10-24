package pl.yalgrin.playnite.simplesync.migration.impl

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.enums.InstallSizeGroup
import pl.yalgrin.playnite.simplesync.library.enums.PlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.ScoreGroup
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode

class GameV3MigratorTest extends SpockIntegrationTest {
    private static final PLAYNITE_ID = "123"
    private static final PLUGIN_ID = "456"
    private static final NAME = "Anno 1602"
    private static final int SRC_MODEL_VERSION = 2
    private static final int TARGET_MODEL_VERSION = 3
    private static final String SAVED_DATA_JSON = """
    {
      "tags": [
        {
          "id": "c314f6eb-351e-4bfa-a557-da38625ba085",
          "name": "City builder",
          "isRemoved": false
        },
        {
          "id": "e4a9b159-6d00-43fd-85f0-f833a7b4ca27",
          "name": "Classic",
          "isRemoved": false
        },
        {
          "id": "39f3b92f-46d4-4e73-b0cb-5e5288e27477",
          "name": "Fantasy",
          "isRemoved": false
        },
        {
          "id": "e3050fc3-3f72-4ecd-a836-bbde40d0cd66",
          "name": "Good Old Game",
          "isRemoved": false
        },
        {
          "id": "84aa1b25-6075-4963-af28-0c99d3917fcf",
          "name": "Historical",
          "isRemoved": false
        },
        {
          "id": "26f3c708-c2ac-460b-ad78-cf49a278adc5",
          "name": "Management",
          "isRemoved": false
        },
        {
          "id": "79ce521f-fe5e-4014-99e4-03704b648cbf",
          "name": "Real-Time",
          "isRemoved": false
        },
        {
          "id": "095931fe-f54a-4f00-a04f-3e6f062452c2",
          "name": "Relaxing",
          "isRemoved": false
        },
        {
          "id": "188aff98-ee8c-4e28-8ca1-ad3872e697c9",
          "name": "Resource Management",
          "isRemoved": false
        },
        {
          "id": "ffb5fa3b-e592-4e40-9435-99b928a25478",
          "name": "Sandbox",
          "isRemoved": false
        },
        {
          "id": "45d922bd-9929-4c7a-9962-289eba4dc8d1",
          "name": "Trading",
          "isRemoved": false
        },
        {
          "id": "b7a0126e-8d94-40a4-a220-50b2a09142f7",
          "name": "Transportation",
          "isRemoved": false
        }
      ],
      "added": "2022-02-27T12:12:37.579Z",
      "links": [
        {
          "url": "https://en.wikipedia.org/wiki/Anno_1602",
          "name": "Wikipedia"
        },
        {
          "url": "https://www.gog.com/game/anno_1602_ad",
          "name": "GOG"
        },
        {
          "url": "https://www.twitch.tv/directory/game/Anno 1602: Creation of a New World",
          "name": "Twitch"
        }
      ],
      "genres": [
        {
          "id": "5187efbe-9831-42ed-82a5-b2069220735b",
          "name": "Real Time Strategy (RTS)",
          "isRemoved": false
        },
        {
          "id": "717c7dc0-5231-4de1-87e1-5aaf362ed1e5",
          "name": "Simulator",
          "isRemoved": false
        },
        {
          "id": "c777ca5c-393c-4dd9-9a67-284933649623",
          "name": "Strategy",
          "isRemoved": false
        }
      ],
      "series": [
        {
          "id": "dfaaf578-c422-4570-a680-a94794486a20",
          "name": "Anno",
          "isRemoved": false
        }
      ],
      "source": {
        "id": "74b9823f-e6c7-43bd-9593-e106e27f0cde",
        "name": "GOG",
        "isRemoved": false
      },
      "regions": [],
      "features": [
        {
          "id": "faf1f5bd-0f08-4181-a2ff-ee6c3b233a45",
          "name": "Single Player",
          "isRemoved": false
        }
      ],
      "isHidden": false,
      "modified": "2026-01-01T09:03:30.906Z",
      "playtime": 240,
      "platforms": [
        {
          "id": "6289efb5-deb2-43a0-adb6-ed3b2ca59451",
          "name": "PC (Windows)",
          "hasIcon": false,
          "isRemoved": false,
          "hasCoverImage": false,
          "specificationId": "pc_windows",
          "hasBackgroundImage": false
        }
      ],
      "playCount": 2,
      "ageRatings": [
        {
          "id": "deca0400-bac3-4485-98d8-168216c64541",
          "name": "PEGI 3",
          "isRemoved": false
        },
        {
          "id": "3c8a8458-217f-4526-a830-b697ca104521",
          "name": "USK 6",
          "isRemoved": false
        }
      ],
      "categories": [],
      "developers": [
        {
          "id": "992b89cb-18bb-4f57-ac85-1de5c6d2bf0c",
          "name": "Max Design GesMBH",
          "isRemoved": false
        }
      ],
      "isFavorite": false,
      "publishers": [
        {
          "id": "188bcaee-a101-420d-b299-8be97d2e247f",
          "name": "Ubisoft",
          "isRemoved": false
        }
      ],
      "description": "<b>A Whole New World!</b>",
      "installSize": 638464000,
      "releaseDate": "1998-09-23T00:00:00",
      "lastActivity": "2026-01-15T21:27:20.958Z",
      "criticScore": 68,
      "communityScore": 87,
      "userScore": 91,
      "completionStatus": {
        "id": "29a5377c-0e86-47d2-b67a-408b0294a521",
        "name": "Beaten",
        "isRemoved": false
      },
      "lastSizeScanDate": "2026-01-09T13:28:26.225Z"
    }
"""

    @Autowired
    private ConnectionFactory connectionFactory
    @Autowired
    private GameRepository repository
    @Autowired
    private JsonMapper jsonMapper
    @Autowired
    private GameV3Migrator migrator

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-db.sql"))
        populator.populate(connectionFactory).block()
    }

    def "should migrate JSON properly"() {
        given:
        Game entity = new Game()
        entity.setPlayniteId(PLAYNITE_ID)
        entity.setName(NAME)
        entity.setPluginId(PLUGIN_ID)
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
                    assert entity.pluginId == it.pluginId
                    assert entity.name == it.name
                    assert entity.modelVersion == TARGET_MODEL_VERSION
                    assert entity.savedData != null
                    def node = jsonMapper.readTree(entity.savedData.asArray())
                    assert node instanceof ObjectNode
                    assert node.get("releaseDate")?.stringValue() == "1998-09-23T00:00:00"
                    assert node.get("releaseYear")?.intValue() == 1998
                    assert node.get("pluginId")?.stringValue() == PLUGIN_ID
                    assert node.get("userScore")?.intValue() == 91
                    assert node.get("userScoreGroup")?.stringValue() == ScoreGroup.O9X.name()
                    assert node.get("criticScore")?.intValue() == 68
                    assert node.get("criticScoreGroup")?.stringValue() == ScoreGroup.O6X.name()
                    assert node.get("communityScore")?.intValue() == 87
                    assert node.get("communityScoreGroup")?.stringValue() == ScoreGroup.O8X.name()
                    assert node.get("added")?.stringValue() == "2022-02-27T12:12:37.579Z"
                    assert node.get("addedInMs")?.longValue() == 1645963957579L
                    assert node.get("modified")?.stringValue() == "2026-01-01T09:03:30.906Z"
                    assert node.get("modifiedInMs")?.longValue() == 1767258210906L
                    assert node.get("lastActivity")?.stringValue() == "2026-01-15T21:27:20.958Z"
                    assert node.get("lastActivityInMs")?.longValue() == 1768512440958L
                    assert node.get("recentActivity")?.stringValue() == "2026-01-15T21:27:20.958Z"
                    assert node.get("recentActivityInMs")?.longValue() == 1768512440958L
                    assert node.get("lastSizeScanDate")?.stringValue() == "2026-01-09T13:28:26.225Z"
                    assert node.get("lastSizeScanDateInMs")?.longValue() == 1767965306225L
                    assert node.get("playtime")?.longValue() == 240L
                    assert node.get("playtimeCategory")?.stringValue() == PlaytimeCategory.LESS_THAN_HOUR.name()
                    assert node.get("installSize")?.longValue() == 638464000L
                    assert node.get("installSizeGroup")?.stringValue() == InstallSizeGroup.S1_100MB_1GB.name()
                    true
                }
                .verifyComplete()
    }
}