package com.andrejusti.example.kotlin.springboot.graphql.domain.sdk

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ErrorType
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.stereotype.Component
import com.andrejusti.example.kotlin.springboot.graphql.domain.controller.graphql.config.CustomGraphQlErrorHandler
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.ItemValidationError
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.exception.ValidationException

@Component
abstract class AbstractSdk {

    @Autowired
    protected lateinit var testRestTemplate: TestRestTemplate

    @Value("\${graphql.servlet.mapping:/graphql}")
    protected lateinit var graphQLPath: String

    @Autowired
    protected lateinit var objectMapperJson: ObjectMapper

    final inline fun <reified T> execGraphQl(body: String?): T {
        val response = execGraphQlRequest(body)
        val responseJson = objectMapperJson.reader().readTree(response)
        val erros = responseJson.get("errors")
        if (StringUtils.isNotBlank(erros?.toString())) {
            throwErroResponseGraphQL(erros)
        }
        val responseSucess = responseJson?.get("data")?.get("response").toString()
        return objectMapperJson.reader().forType(object : TypeReference<T>() {}).readValue(responseSucess)
    }

    fun throwErroResponseGraphQL(jsonNode: JsonNode) {
        val erros = jsonNode.toList()
        val validationError = erros
                .filter { StringUtils.equalsIgnoreCase(it.get("errorType")?.asText(), ErrorType.ValidationError.name) }
                .first()
        validationError
                ?.let { throwValidationErrorItensResponseGraphQL(validationError) }
                ?: throw RuntimeException("Unexpected error in response errors block: [bodyResponseErros: '${jsonNode.toString()}']")
    }

    fun throwValidationErrorItensResponseGraphQL(validationError: JsonNode) {
        val validationErrorItens = validationError.get("extensions")?.get(CustomGraphQlErrorHandler.KEY_ERRO_VALIDATION)?.toList()
        val validationErrorItensNormalized = objectMapperJson.reader().forType(object : TypeReference<List<ItemValidationError>>() {}).readValue<List<ItemValidationError>>(validationErrorItens?.toString())
        throw ValidationException(validationErrorItensNormalized)
    }

    fun execGraphQlRequest(body: String?): String? {
        val bodyRequestNormalized = body
                ?.let { StringUtils.replaceAll(it, "\n|\r|\t", StringUtils.SPACE) }
                ?.let { StringUtils.replaceAll(it, "(\\s{1,})", StringUtils.SPACE) }
        val response = testRestTemplate.postForEntity(graphQLPath, bodyRequestNormalized, String::class.java)
        val responseBody = response?.body
        if (response.statusCode.is2xxSuccessful) {
            return responseBody
        }
        throw RuntimeException("Unexpected error while executing request graphql: [statusHttp: '${response.statusCode}', bodyResponse: '$responseBody']")
    }
}