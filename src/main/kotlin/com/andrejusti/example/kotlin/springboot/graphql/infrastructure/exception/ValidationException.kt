package com.andrejusti.example.kotlin.springboot.graphql.infrastructure.exception

import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.ItemValidationError

data class ValidationException(val itemValidationErrors: List<ItemValidationError>) : RuntimeException()