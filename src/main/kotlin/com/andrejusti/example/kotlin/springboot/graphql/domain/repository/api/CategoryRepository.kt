package com.andrejusti.example.kotlin.springboot.graphql.domain.repository.api

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Category

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    fun existsByNameIgnoreCase(name: String?): Boolean

    fun existsByIdNotAndNameIgnoreCase(id: Long, name: String?): Boolean
}


