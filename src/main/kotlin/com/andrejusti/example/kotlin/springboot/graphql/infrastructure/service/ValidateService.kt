package com.andrejusti.example.kotlin.springboot.graphql.infrastructure.service

import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.ItemValidationError
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.exception.ValidationException

@RequestScope
@Service
class ValidateService {
    
    private var itemValidationErrors: ArrayList<ItemValidationError> = arrayListOf()

    fun hasValidationError() = itemValidationErrors.isNotEmpty()

    fun addItemValidation(itemValidationError: ItemValidationError) {
        itemValidationError.apply { itemValidationErrors.add(this) }
    }

    fun addIfItemConditionIsTrue(condition: () -> Boolean, itemValidationError: ItemValidationError) {
        if (condition()) {
            addItemValidation(itemValidationError)
        }
    }

    fun addIfItemConditionIsTrue(condition: () -> Boolean, errorLocation: String, messageKey: String, context: Map<String, String>? = null) {
        addIfItemConditionIsTrue(condition, ItemValidationError(errorLocation, messageKey, context))
    }

    fun addIfItemConditionIsTrue(condition: Boolean, itemValidationError: ItemValidationError) {
        addIfItemConditionIsTrue({ condition }, itemValidationError)
    }

    fun addIfItemConditionIsTrue(condition: Boolean, errorLocation: String, messageKey: String, context: Map<String, String>? = null) {
        addIfItemConditionIsTrue(condition, ItemValidationError(errorLocation, messageKey, context))
    }


    fun addIfItemConditionIsTrueAndNotHasError(condition: () -> Boolean, errorLocation: String, messageKey: String, context: Map<String, String>? = null) {
        if (!hasValidationError()) {
            addIfItemConditionIsTrue(condition, errorLocation, messageKey, context)
        }
    }

    fun validate() {
        if (hasValidationError()) {
            throw ValidationException(itemValidationErrors)
        }
    }
}
