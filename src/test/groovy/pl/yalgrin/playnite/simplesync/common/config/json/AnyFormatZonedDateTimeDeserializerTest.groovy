package pl.yalgrin.playnite.simplesync.common.config.json

import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.core.ObjectReadContext
import tools.jackson.core.json.JsonFactory

import java.time.ZonedDateTime

class AnyFormatZonedDateTimeDeserializerTest extends Specification {
    private TimeZone originalTimeZone

    def setup() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))
    }

    def cleanup() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Unroll
    def "deserializes #value"() {
        given:
        def parser = JsonFactory.builder().build()
                .createParser(ObjectReadContext.empty(), "${value ? "\"" + value + "\"" : ""}")
        parser.nextToken()

        expect:
        new AnyFormatZonedDateTimeDeserializer().deserialize(parser, null) == expected

        cleanup:
        parser.close()

        where:
        value                                     | expected
        '2017-08-01T12:30:00Z'                    | ZonedDateTime.parse('2017-08-01T12:30:00Z')
        '2017-08-01T12:30:00.1234567Z'            | ZonedDateTime.parse('2017-08-01T12:30:00.1234567Z')
        '2017-08-01T12:30:00+02:00'               | ZonedDateTime.parse('2017-08-01T12:30:00+02:00')
        '2017-08-01T12:30:00+02:00[Europe/Paris]' | ZonedDateTime.parse('2017-08-01T12:30:00+02:00[Europe/Paris]')
        '2017-08-01T12:30:00'                     | ZonedDateTime.parse('2017-08-01T12:30:00+02:00[Europe/Warsaw]')
        '    '                                    | null
        ''                                        | null
        null                                      | null
    }
}