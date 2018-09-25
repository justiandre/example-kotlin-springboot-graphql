package com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto

data class ItemValidationError(
        var errorLocation: String,
        var messageKey: String,
        var context: Map<String, String>? = null
)


