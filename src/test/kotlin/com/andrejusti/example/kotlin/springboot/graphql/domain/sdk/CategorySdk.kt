package com.andrejusti.example.kotlin.springboot.graphql.domain.sdk

import org.springframework.stereotype.Component
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Category
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

@Component
class CategorySdk : AbstractSdk() {

    fun category(id: Long): Category? {
        val query = """
                {
                  "query":"query {
                    response: category(id: $id) {
                      id
                      name
                    }
                  }",
                  "variables":null
                }
            """
        return execGraphQl<Category>(query)
    }

    fun categories(pagination: Pagination): List<Category> {
        val query = """
                {
                  "query":"query {
                    response: categories(pagination: {page: ${pagination.page}, maxRecords: ${pagination.maxRecords}}) {
                      id
                      name
                    }
                  }",
                  "variables":null
                }
            """
        return execGraphQl<List<Category>>(query)
    }

    fun createCategory(category: Category): Category {
        val mutation = """
                {
                    "query":"mutation createCategory {
                        response: createCategory(category: {name: \"${category.name}\" }) {
                            id
                            name
                        }
                    }",
                    "variables":null
                }
            """
        return execGraphQl<Category>(mutation)
    }

    fun editCategory(category: Category): Category {
        val mutation = """
                {
                    "query":"mutation editCategory {
                        response: editCategory(id: ${category.id}, category: {name: \"${category.name}\"}) {
                            id
                            name
                        }
                    }",
                    "variables":null
                }
            """
        return execGraphQl<Category>(mutation)
    }

    fun deleteCategory(id: Long): Boolean {
        val mutation = """
                {
                    "query":"mutation deleteCategory {
                        response: deleteCategory(id: $id)
                    }",
                    "variables":null
                }
            """
        return execGraphQl<Boolean>(mutation)
    }
}