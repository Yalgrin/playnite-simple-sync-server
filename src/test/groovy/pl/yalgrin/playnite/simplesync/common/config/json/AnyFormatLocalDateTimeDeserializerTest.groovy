package pl.yalgrin.playnite.simplesync.common.config.json

import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.core.ObjectReadContext
import tools.jackson.core.json.JsonFactory

import java.time.LocalDateTime

class AnyFormatLocalDateTimeDeserializerTest extends Specification {
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
        new AnyFormatLocalDateTimeDeserializer().deserialize(parser, null) == expected

        cleanup:
        parser.close()

        where:
        value                                     | expected
        '2017-08-01T12:30:00Z'                    | LocalDateTime.parse('2017-08-01T14:30:00')
        '2017-08-01T12:30:00.1234567Z'            | LocalDateTime.parse('2017-08-01T14:30:00.1234567')
        '2017-08-01T12:30:00+02:00'               | LocalDateTime.parse('2017-08-01T12:30:00')
        '2017-08-01T12:30:00+02:00[Europe/Paris]' | LocalDateTime.parse('2017-08-01T12:30:00')
        '2017-08-01T12:30:00'                     | LocalDateTime.parse('2017-08-01T12:30:00')
        '    '                                    | null
        ''                                        | null
        null                                      | null
    }
}