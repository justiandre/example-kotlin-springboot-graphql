package tech.justi.example.kotlin.springboot.graphql.infrastructure.dto

data class Pagination(
        var page: Int? = null,
        var maxRecords: Int? = null
)