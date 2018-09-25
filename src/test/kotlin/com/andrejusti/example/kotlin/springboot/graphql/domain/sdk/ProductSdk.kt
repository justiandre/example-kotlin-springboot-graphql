package com.andrejusti.example.kotlin.springboot.graphql.domain.sdk

import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Category
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Product
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

@Component
class ProductSdk : AbstractSdk() {

    fun product(id: Long): Product? {
        val query = """
                {
                  "query":"query {
                    response: product(id: $id) {
                        id
                        name
                        value
                        description
                        categories { id }
                    }
                  }",
                  "variables":null
                }
            """
        return execGraphQl<Product>(query)
    }

    fun products(pagination: Pagination, name: String): List<Product> {
        val query = """
                {
                  "query":"query {
                    response: products(pagination: {page: ${pagination.page}, maxRecords: ${pagination.maxRecords}}, name: \"$name\") {
                        id
                        name
                        value
                        description
                        categories { id }
                    }
                  }",
                  "variables":null
                }
            """
        return execGraphQl<List<Product>>(query)
    }

    fun createProduct(product: Product): Product {
        val mutation = """
                {
                    "query":"mutation createProduct {
                        response: createProduct(product: {name: \"${product.name}\", description: \"${product.description}\", value: ${product.value}, categories: [${normalizeCategory(product)}]}) {
                            id
                            name
                            value
                            description
                            categories { id }
                        }
                    }",
                    "variables":null
                }
            """
        return execGraphQl<Product>(mutation)
    }

    fun editProduct(product: Product): Product {
        val mutation = """
                {
                    "query":"mutation editProduct {
                        response: editProduct(id: ${product.id}, product: {name: \"${product.name}\", description: \"${product.description}\", value: ${product.value}, categories: [${normalizeCategory(product)}]}) {
                            id
                            name
                            value
                            description
                            categories { id }
                        }
                    }",
                    "variables":null
                }
            """
        return execGraphQl<Product>(mutation)
    }

    fun deleteProduct(id: Long): Boolean {
        val mutation = """
                {
                    "query":"mutation deleteProduct {
                        response: deleteProduct(id: $id)
                    }",
                    "variables":null
                }
            """
        return execGraphQl<Boolean>(mutation)
    }

    private fun normalizeCategory(product: Product?) =
            product
                    ?.categories
                    ?.map { it.id }
                    ?.filterNotNull()
                    ?.joinToString(separator = ", ")
                    ?: StringUtils.EMPTY
}