package com.andrejusti.example.kotlin.springboot.graphql.domain.controller.graphql.resolver

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Category
import com.andrejusti.example.kotlin.springboot.graphql.domain.service.CategoryService
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

@Component
class CategoryGraphQlResolver(
        @Autowired val categoryService: CategoryService
) : GraphQLQueryResolver, GraphQLMutationResolver {

    fun category(id: Long) = categoryService.findById(id)

    fun categories(pagination: Pagination) = categoryService.findAll(pagination)

    fun createCategory(category: Category) = categoryService.save(category)

    fun editCategory(id: Long, category: Category) = categoryService.save(category.apply { this.id = id })

    fun deleteCategory(id: Long) = categoryService.delete(id)
}