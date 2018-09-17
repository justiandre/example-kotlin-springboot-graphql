package tech.justi.example.kotlin.springboot.graphql.infrastructure.exception

import tech.justi.example.kotlin.springboot.graphql.infrastructure.dto.ItemValidationError

data class ValidationException(val itemValidationErrors: List<ItemValidationError>) : RuntimeException()