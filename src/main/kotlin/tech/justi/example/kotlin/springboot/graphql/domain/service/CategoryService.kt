package tech.justi.example.kotlin.springboot.graphql.domain.service

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.justi.example.kotlin.springboot.graphql.domain.entity.Category
import tech.justi.example.kotlin.springboot.graphql.domain.repository.api.CategoryRepository
import tech.justi.example.kotlin.springboot.graphql.domain.repository.api.ProductRepository
import tech.justi.example.kotlin.springboot.graphql.infrastructure.dto.Pagination
import tech.justi.example.kotlin.springboot.graphql.infrastructure.service.PaginationService
import tech.justi.example.kotlin.springboot.graphql.infrastructure.service.ValidateService

@Service
class CategoryService(
        @Autowired val paginationService: PaginationService,
        @Autowired val validationService: ValidateService,
        @Autowired val categoryRepository: CategoryRepository,
        @Autowired val productRepository: ProductRepository
) {

    companion object {
        const val CATEGORY_NAME_MAX_SIZE = 70

        const val ITEM_VALIDATION_LOCATION_CATEGORY_NAME = "category.name"
        const val ITEM_VALIDATION_LOCATION_CATEGORY_PRODUCT = "category.product"

        const val ITEM_VALIDATION_ERROR_CATEGORY_NAME_NOT_BLACK = "category.name.notBlank"
        const val ITEM_VALIDATION_ERROR_CATEGORY_NAME_MAX_SIZE = "category.name.maxSize"
        const val ITEM_VALIDATION_ERROR_CATEGORY_DUPLICATE = "category.duplicate"
        const val ITEM_VALIDATION_ERROR_CATEGORY_RELATIONSHIP = "category.product.relationship"
    }

    fun findById(id: Long) = categoryRepository.findById(id).orElse(null)

    fun findAll(pagination: Pagination) = categoryRepository.findAll(paginationService.parsePagination(pagination)).content

    @Transactional
    fun save(category: Category) = category.let {
        validateSave(it)
        categoryRepository.save(it)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        if (!categoryRepository.existsById(id)) {
            return false
        }
        validateDelete(id)
        categoryRepository.deleteById(id)
        return true
    }

    private fun validateDelete(idCategory: Long) = validationService.apply {
        addIfItemConditionIsTrue(productRepository.existsByCategoriesId(idCategory), ITEM_VALIDATION_LOCATION_CATEGORY_PRODUCT, ITEM_VALIDATION_ERROR_CATEGORY_RELATIONSHIP)
    }.validate()

    private fun validateSave(category: Category) = validationService.apply {
        addIfItemConditionIsTrue(StringUtils.isBlank(category.name), ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ITEM_VALIDATION_ERROR_CATEGORY_NAME_NOT_BLACK)
        addIfItemConditionIsTrueAndNotHasError({ StringUtils.length(category.name) > CATEGORY_NAME_MAX_SIZE }, ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ITEM_VALIDATION_ERROR_CATEGORY_NAME_MAX_SIZE)
        addIfItemConditionIsTrueAndNotHasError({ isDuplicateCategory(category) }, ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ITEM_VALIDATION_ERROR_CATEGORY_DUPLICATE)
    }.validate()

    private fun isDuplicateCategory(category: Category) =
            category.id?.let {
                categoryRepository.existsByIdNotAndNameIgnoreCase(it, category.name)
            } ?: categoryRepository.existsByNameIgnoreCase(category.name)
}
