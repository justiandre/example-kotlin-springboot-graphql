package com.andrejusti.example.kotlin.springboot.graphql.domain.controller.graphql.config

import com.fasterxml.jackson.annotation.JsonIgnore
import graphql.ErrorType
import graphql.ExceptionWhileDataFetching
import graphql.GraphQLError
import graphql.servlet.GraphQLErrorHandler
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.stereotype.Component
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.exception.ValidationException

@Component
class CustomGraphQlErrorHandler : GraphQLErrorHandler {

    companion object {
        const val KEY_ERRO_VALIDATION = "ItemValidationErrors"
    }

    override fun processErrors(errors: List<GraphQLError>): List<GraphQLError> {
        val erros = errors.filter { !isServerError(it) }
        val errosException = errors
                .filter { isServerError(it) }
                .map { parseGraphQLErrorAdapterException(it) }
        return erros + errosException
    }

    private fun isServerError(error: GraphQLError) = error is ExceptionWhileDataFetching

    private fun parseGraphQLErrorAdapterException(graphQLError: GraphQLError) =
            if (isValidationException(graphQLError))
                GraphQLErrorAdapterExceptionValidation(graphQLError)
            else
                GraphQLErrorAdapterException(graphQLError)

    private fun isValidationException(graphQLError: GraphQLError) =
            (graphQLError as ExceptionWhileDataFetching).exception is ValidationException

    private open class GraphQLErrorAdapterException(@JsonIgnore val graphQLError: GraphQLError) : GraphQLError by graphQLError {

        override fun getMessage() = getExceptionWhileDataFetching().exception.message

        override fun getExtensions(): Map<String, Any> =
                getExceptionWhileDataFetching().exception
                        .let { mapOf(it.javaClass.simpleName to ExceptionUtils.getRootCauseMessage(it)) }

        protected fun getExceptionWhileDataFetching() = graphQLError as ExceptionWhileDataFetching
    }

    private class GraphQLErrorAdapterExceptionValidation(@JsonIgnore val graphQLErrorValidation: GraphQLError) : GraphQLErrorAdapterException(graphQLErrorValidation) {

        override fun getExtensions() = mapOf(KEY_ERRO_VALIDATION to getValidationException().itemValidationErrors)

        override fun getErrorType() = ErrorType.ValidationError

        private fun getValidationException() = getExceptionWhileDataFetching().exception as ValidationException
    }
}


