package tech.justi.example.kotlin.springboot.graphql.infrastructure.service

import org.apache.commons.lang3.math.NumberUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import tech.justi.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

@Service
class PaginationService(
        @Value("\${app.search.pagination.numberRecords.default}") val paginationNumberRecordsDefault: Int,
        @Value("\${app.search.pagination.numberRecords.max}") val paginationNumberRecordsMax: Int
) {

    fun parsePagination(pagination: Pagination) = parsePagination(pagination.page, pagination.maxRecords)

    fun parsePagination(page: Int?, maxRecords: Int?): Pageable {
        val pageNormalized = page ?: NumberUtils.INTEGER_ZERO
        val maxRecordsNormalized: Int = (maxRecords ?: paginationNumberRecordsDefault)
                .takeIf { paginationNumberRecordsMax > it }
                ?: paginationNumberRecordsMax
        return PageRequest.of(pageNormalized, maxRecordsNormalized)
    }
}


