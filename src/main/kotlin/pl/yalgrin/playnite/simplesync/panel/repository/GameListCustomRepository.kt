package pl.yalgrin.playnite.simplesync.panel.repository

import io.vavr.Tuple
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.common.util.escapeLike
import pl.yalgrin.playnite.simplesync.common.util.isNotNullAndNotBlank
import pl.yalgrin.playnite.simplesync.library.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.enums.*
import pl.yalgrin.playnite.simplesync.panel.dto.GameListFilterDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Repository
class GameListCustomRepository(
    private val databaseClient: DatabaseClient,
    private val jsonMapper: JsonMapper
) {

    //TODO isInstalled, isUninstalled

    fun findIdsByFilter(filter: GameListFilterDTO): Flux<Long> {
        return Mono.fromSupplier {
            val queryBuilder: StringBuilder = StringBuilder("SELECT g.id FROM playnite_game g WHERE 1=1")
            val params: MutableMap<String, Any> = mutableMapOf()

            val useAndFilteringStyle = filter.useAndFilteringStyle ?: false

            queryBuilder.append(" AND g.removed = :isRemoved")
            params["isRemoved"] = false

            filter.isHidden?.let {
                queryBuilder.append(" AND g.contents->>'isHidden' = :isHidden")
                params["isHidden"] = it.toString()
            }

            filter.isFavorite?.let {
                queryBuilder.append(" AND g.contents->>'isFavorite' = :isFavorite")
                params["isFavorite"] = it.toString()
            }

            val name = filter.name
            if (!name.isNullOrBlank()) {
                queryBuilder.append(" AND lower(g.name) LIKE :name ESCAPE '\\'")
                params["name"] = "%${name.escapeLike()}%"
            }

            val version = filter.version
            if (!version.isNullOrBlank()) {
                queryBuilder.append(" AND lower(g.contents->>'version') LIKE :version ESCAPE '\\'")
                params["version"] = "%${version.escapeLike()}%"
            }

            val releaseYears = filter.releaseYear?.values
            if (!releaseYears.isNullOrEmpty()) {
                queryBuilder.append(" AND g.contents->>'releaseYear' IN (:releaseYears)")
                params["releaseYears"] = releaseYears
            }

            addIdItemPropertiesFilterMultiple(filter.genre, queryBuilder, params, useAndFilteringStyle, "genres")
            addIdItemPropertiesFilterMultiple(filter.platform, queryBuilder, params, useAndFilteringStyle, "platforms")
            addIdItemPropertiesFilterMultiple(
                filter.publisher,
                queryBuilder,
                params,
                useAndFilteringStyle,
                "publishers"
            )
            addIdItemPropertiesFilterMultiple(
                filter.developer,
                queryBuilder,
                params,
                useAndFilteringStyle,
                "developers"
            )
            addIdItemPropertiesFilterMultiple(filter.category, queryBuilder, params, useAndFilteringStyle, "categories")
            addIdItemPropertiesFilterMultiple(filter.tag, queryBuilder, params, useAndFilteringStyle, "tags")
            addIdItemPropertiesFilterMultiple(filter.series, queryBuilder, params, useAndFilteringStyle, "series")
            addIdItemPropertiesFilterMultiple(filter.region, queryBuilder, params, useAndFilteringStyle, "regions")
            addIdItemPropertiesFilter(filter.source, queryBuilder, params, "source")
            addIdItemPropertiesFilterMultiple(
                filter.ageRating,
                queryBuilder,
                params,
                useAndFilteringStyle,
                "ageRatings"
            )
            addCustomIdItemPropertiesFilter(filter.library, queryBuilder, params, "libraries", "pluginId", "pluginName")
            addIdItemPropertiesFilter(filter.completionStatuses, queryBuilder, params, "completionStatus")
            addIdItemPropertiesFilterMultiple(filter.feature, queryBuilder, params, useAndFilteringStyle, "features")

            addScorePropertiesFilter(filter.userScore, queryBuilder, params, "userScoreGroup")
            addScorePropertiesFilter(filter.criticScore, queryBuilder, params, "criticScoreGroup")
            addScorePropertiesFilter(filter.communityScore, queryBuilder, params, "communityScoreGroup")

            addPastTimePropertiesFilter(filter.lastActivity, queryBuilder, params, "lastActivityInMs")
            addPastTimePropertiesFilter(filter.recentActivity, queryBuilder, params, "recentActivityInMs")
            addPastTimePropertiesFilter(filter.added, queryBuilder, params, "addedInMs")
            addPastTimePropertiesFilter(filter.modified, queryBuilder, params, "modifiedInMs")

            addPlaytimeCategoryFilter(filter.playTime, queryBuilder, params, "playtimeCategory")
            addInstallSizeFilter(filter.installSize, queryBuilder, params, "installSizeGroup")

            addOrdering(filter, queryBuilder)

            databaseClient.sql(queryBuilder.toString())
                .bindValues(params)
        }.flatMapMany { builder ->
            builder.map { row ->
                Tuple.of(row.get("id", Long::class.java))
            }.all().map { it._1!! }
        }
    }

    private fun addIdItemPropertiesFilterMultiple(
        filter: IdItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        useAndFilteringStyle: Boolean, fieldName: String
    ) {
        if (filter == null) {
            return
        }

        val ids = filter.ids
        if (ids.isNotEmpty()) {
            val paramName = "${fieldName}Ids"

            if (useAndFilteringStyle) {
                val idsMap = mutableMapOf<String, Any>()
                val idsList = mutableListOf<Map<String, String>>()
                ids.forEach {
                    idsList.add(mapOf("id" to it))
                }
                idsMap[fieldName] = idsList
                val queryJson = jsonMapper.writeValueAsString(idsMap)

                queryBuilder.append(" AND CAST(g.contents AS jsonb) @> CAST(:$paramName AS jsonb)")
                params[paramName] = queryJson
            } else {
                queryBuilder.append(
                    """ AND EXISTS ( SELECT 1 FROM json_array_elements(g.contents->'$fieldName') AS o WHERE o->>'id' IN (:$paramName) )"""
                )
                params[paramName] = ids
            }
        }

        val text = filter.text
        if (text.isNotNullAndNotBlank()) {
            val paramName = "${fieldName}Text"

            queryBuilder.append(
                """ AND EXISTS ( SELECT 1 FROM json_array_elements(g.contents->'$fieldName') AS o WHERE lower(o->>'name') LIKE :$paramName ESCAPE '\' )"""
            )
            params[paramName] = "%${text.escapeLike()}%"
        }
    }

    private fun addIdItemPropertiesFilter(
        filter: IdItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        fieldName: String
    ) {
        if (filter == null) {
            return
        }

        val ids = filter.ids
        if (ids.isNotEmpty()) {
            val paramName = "${fieldName}Ids"

            queryBuilder.append(" AND CAST(g.contents->>'$fieldName' AS jsonb)->>'id' IN (:$paramName)")
            params[paramName] = ids
        }

        val text = filter.text
        if (text.isNotNullAndNotBlank()) {
            val paramName = "${fieldName}Text"

            queryBuilder.append(" AND lower(CAST(g.contents->>'$fieldName' AS jsonb)->>'name') LIKE :$paramName ESCAPE '\\'")
            params[paramName] = "%${text.escapeLike()}%"
        }
    }

    private fun addCustomIdItemPropertiesFilter(
        filter: IdItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        fieldName: String, idFieldName: String, nameFieldName: String
    ) {
        if (filter == null) {
            return
        }

        val ids = filter.ids
        if (ids.isNotEmpty()) {
            val paramName = "${fieldName}Ids"

            queryBuilder.append(" AND g.contents->>'$idFieldName' IN (:$paramName)")
            params[paramName] = ids
        }

        val text = filter.text
        if (text.isNotNullAndNotBlank()) {
            val paramName = "${fieldName}Text"

            queryBuilder.append(" AND lower(g.contents->>'$nameFieldName') LIKE :$paramName ESCAPE '\\'")
            params[paramName] = "%${text.escapeLike()}%"
        }
    }

    private fun addScorePropertiesFilter(
        filter: IntItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        fieldName: String
    ) {
        if (filter == null) {
            return
        }

        val values = filter.values.mapNotNull { it.toScoreGroupById() }.map { it.name }
        if (values.isNotEmpty()) {
            val paramName = "${fieldName}Values"

            queryBuilder.append(" AND g.contents->>'$fieldName' IN (:$paramName)")
            params[paramName] = values
        }
    }

    private fun addPastTimePropertiesFilter(
        filter: IntItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        fieldName: String
    ) {
        if (filter == null) {
            return
        }

        val values = filter.values.mapNotNull { it.toPastTimeSegmentById() }
        if (values.isNotEmpty()) {
            val now = ZonedDateTime.now()
            val startOfToday = now.truncatedTo(ChronoUnit.DAYS)
            val startOfTomorrow = startOfToday.plusDays(1)
            val startOfYesterday = startOfToday.minusDays(1)
            val pastWeek = now.minusDays(7)
            val pastMonth = now.minusDays(31)
            val pastYear = now.minusDays(365)

            queryBuilder.append(" AND (0=1")
            if (values.contains(PastTimeSegment.NEVER)) {
                queryBuilder.append(" OR g.contents->>'$fieldName' IS NULL")
            }
            if (values.contains(PastTimeSegment.TODAY)) {
                addPeriodBetween(fieldName, "Today", queryBuilder, params, startOfToday, startOfTomorrow)
            }
            if (values.contains(PastTimeSegment.YESTERDAY)) {
                addPeriodBetween(fieldName, "Yesterday", queryBuilder, params, startOfYesterday, startOfToday)
            }
            if (values.contains(PastTimeSegment.PAST_WEEK)) {
                addPeriodBetween(fieldName, "PastWeek", queryBuilder, params, pastWeek, startOfYesterday)
            }
            if (values.contains(PastTimeSegment.PAST_MONTH)) {
                addPeriodBetween(fieldName, "PastMonth", queryBuilder, params, pastMonth, pastWeek)
            }
            if (values.contains(PastTimeSegment.PAST_YEAR)) {
                addPeriodBetween(fieldName, "PastYear", queryBuilder, params, pastYear, pastMonth)
            }
            if (values.contains(PastTimeSegment.MORE_THAN_YEAR)) {
                val paramName = "${fieldName}MoreThanYear"
                queryBuilder.append(" OR CAST(g.contents->>'$fieldName' AS BIGINT) < :$paramName")
                params[paramName] = pastYear.toInstant().toEpochMilli()
            }
            queryBuilder.append(")")
        }
    }

    private fun addPeriodBetween(
        fieldName: String,
        paramPrefix: String,
        queryBuilder: StringBuilder,
        params: MutableMap<String, Any>,
        startPeriod: ZonedDateTime,
        endPeriod: ZonedDateTime
    ) {
        val startParamName = "${fieldName}${paramPrefix}Start"
        val endParamName = "${fieldName}${paramPrefix}End"
        queryBuilder.append(" OR (CAST(g.contents->>'$fieldName' AS BIGINT) >= :$startParamName AND CAST(g.contents->>'$fieldName' AS BIGINT) < :$endParamName)")
        params[startParamName] = startPeriod.toInstant().toEpochMilli()
        params[endParamName] = endPeriod.toInstant().toEpochMilli()
    }

    private fun addPlaytimeCategoryFilter(
        filter: IntItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        fieldName: String
    ) {
        if (filter == null) {
            return
        }

        val values = filter.values.mapNotNull { it.toPlaytimeCategoryById() }.map { it.name }
        if (values.isNotEmpty()) {
            val paramName = "${fieldName}Values"

            queryBuilder.append(" AND g.contents->>'$fieldName' IN (:$paramName)")
            params[paramName] = values
        }
    }

    private fun addInstallSizeFilter(
        filter: IntItemPropertiesDTO?, queryBuilder: StringBuilder, params: MutableMap<String, Any>,
        fieldName: String
    ) {
        if (filter == null) {
            return
        }

        val values = filter.values.mapNotNull { it.toInstallSizeGroupById() }.map { it.name }
        if (values.isNotEmpty()) {
            val paramName = "${fieldName}Values"

            queryBuilder.append(" AND g.contents->>'$fieldName' IN (:$paramName)")
            params[paramName] = values
        }
    }

    private fun addOrdering(
        filter: GameListFilterDTO,
        queryBuilder: StringBuilder
    ) {
        val direction = if (filter.sortingOrderDirection.equals(
                SortOrderDirection.DESCENDING.code,
                ignoreCase = true
            )
        ) "DESC" else "ASC"
        val sortingOrder = SortOrder.entries.find { it.code == filter.sortingOrder }
        if (sortingOrder != null) {
            when (sortingOrder) {
                SortOrder.NAME -> queryBuilder.append(" ORDER BY COALESCE(g.contents->>'sortingName', name) $direction")
                SortOrder.PLATFORMS -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'platforms') AS item) $direction")
                SortOrder.LIBRARY -> queryBuilder.append(" ORDER BY g.contents->>'pluginName' $direction")
                SortOrder.CATEGORIES -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'categories') AS item) $direction")
                SortOrder.LAST_ACTIVITY -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'lastActivityInMs' AS BIGINT), 0) $direction")
                SortOrder.GENRES -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'genres') AS item) $direction")
                SortOrder.RELEASE_DATE -> queryBuilder.append(" ORDER BY TO_DATE(g.contents->>'releaseDate', 'YYYY-MM-DD') $direction")
                SortOrder.DEVELOPERS -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'developers') AS item) $direction")
                SortOrder.PUBLISHERS -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'publishers') AS item) $direction")
                SortOrder.TAGS -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'tags') AS item) $direction")
                SortOrder.SERIES -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'series') AS item) $direction")
                SortOrder.AGE_RATINGS -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'ageRatings') AS item) $direction")
                SortOrder.VERSION -> queryBuilder.append(" ORDER BY lower(version) $direction")
                SortOrder.REGIONS -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'regions') AS item) $direction")
                SortOrder.SOURCE -> queryBuilder.append(" ORDER BY (CAST(g.contents->>'source' AS jsonb)->>'name') $direction")
                SortOrder.PLAY_COUNT -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'playCount' AS BIGINT), 0) $direction")
                SortOrder.PLAYTIME -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'playtime' AS BIGINT), 0) $direction")
                SortOrder.COMPLETION_STATUS -> queryBuilder.append(" ORDER BY (CAST(g.contents->>'completionStatus' AS jsonb)->>'name') $direction")
                SortOrder.USER_SCORE -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'userScore' AS BIGINT), 0) $direction")
                SortOrder.CRITIC_SCORE -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'criticScore' AS BIGINT), 0) $direction")
                SortOrder.COMMUNITY_SCORE -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'communityScore' AS BIGINT), 0) $direction")
                SortOrder.ADDED -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'addedInMs' AS BIGINT), 0) $direction")
                SortOrder.MODIFIED -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'modifiedInMs' AS BIGINT), 0) $direction")
//                SortOrder.IS_INSTALLED -> TODO()
                SortOrder.HIDDEN -> queryBuilder.append(" ORDER BY COALESCE(g.contents->>'isHidden', 'false') $direction")
                SortOrder.FAVORITE -> queryBuilder.append(" ORDER BY COALESCE(g.contents->>'isFavorite', 'false') $direction")
//                SortOrder.INSTALL_DIRECTORY -> TODO()
                SortOrder.FEATURES -> queryBuilder.append(" ORDER BY (SELECT min(item->>'name') FROM json_array_elements(g.contents->'features') AS item) $direction")
                SortOrder.INSTALL_SIZE -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'installSize' AS BIGINT), 0) $direction")
                SortOrder.RECENT_ACTIVITY -> queryBuilder.append(" ORDER BY COALESCE(CAST(g.contents->>'recentActivityInMs' AS BIGINT), 0) $direction")
                else -> queryBuilder.append(" ORDER BY COALESCE(g.contents->>'sortingName', name) $direction")
            }
            queryBuilder.append(", COALESCE(g.contents->>'sortingName', name) $direction, id $direction")
        } else {
            queryBuilder.append(" ORDER BY COALESCE(g.contents->>'sortingName', name) $direction, id $direction")
        }
    }
}