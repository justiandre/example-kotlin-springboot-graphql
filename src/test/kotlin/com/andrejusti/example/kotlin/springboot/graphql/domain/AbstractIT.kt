package com.andrejusti.example.kotlin.springboot.graphql.domain

import org.apache.commons.lang3.math.NumberUtils
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.Pagination
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.exception.ValidationException
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIT {

    protected fun createRandomValue() = UUID.randomUUID().toString()

    protected fun createPagination() = Pagination(page = NumberUtils.INTEGER_ZERO, maxRecords = Int.MAX_VALUE)

    protected fun assertValidationException(messageKey: String, exec: () -> Unit) {
        try {
            exec()
            Assert.fail("Not generated validation exception")
        } catch (validationException: ValidationException) {
            Assert.assertTrue("Invalid validation exception", validationException.itemValidationErrors.any { it.messageKey == messageKey })
        }
    }
}