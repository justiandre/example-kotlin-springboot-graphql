package tech.justi.example.kotlin.springboot.graphql.domain.service

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import tech.justi.example.kotlin.springboot.graphql.domain.entity.Product
import tech.justi.example.kotlin.springboot.graphql.domain.repository.api.ProductRepository
import tech.justi.example.kotlin.springboot.graphql.infrastructure.dto.Pagination
import tech.justi.example.kotlin.springboot.graphql.infrastructure.service.PaginationService
import tech.justi.example.kotlin.springboot.graphql.infrastructure.service.ValidateService

@Service
class ProductService(
        @Autowired val paginationService: PaginationService,
        @Autowired val validationService: ValidateService,
        @Autowired val productRepository: ProductRepository
) {

    companion object {
        const val PRODUCT_NAME_MAX_SIZE = 70
        const val PRODUCT_DESCRIPTION_MAX_SIZE = 4000

        const val ITEM_VALIDATION_LOCATION_PRODUCT_NAME = "product.name"
        const val ITEM_VALIDATION_LOCATION_PRODUCT_DESCRIPTION = "product.description"
        const val ITEM_VALIDATION_LOCATION_PRODUCT_VALUE = "product.value"
        const val ITEM_VALIDATION_LOCATION_PRODUCT_CATEGORY = "product.category"

        const val ITEM_VALIDATION_ERROR_PRODUCT_NAME_NOT_BLACK = "product.name.notBlank"
        const val ITEM_VALIDATION_ERROR_PRODUCT_NAME_MAX_SIZE = "product.name.maxSize"
        const val ITEM_VALIDATION_ERROR_PRODUCT_DESCRIPTION_MAX_SIZE = "product.description.maxSize"
        const val ITEM_VALIDATION_ERROR_PRODUCT_VALUE_NOT_NEGATIVE = "product.value.notNegative"
        const val ITEM_VALIDATION_ERROR_PRODUCT_CATEGORY_REQUIRED = "product.category.required"
        const val ITEM_VALIDATION_ERROR_PRODUCT_DUPLICATE = "product.duplicate"
    }

    fun findById(id: Long) = productRepository.findById(id).orElse(null)

    fun findAllByName(pagination: Pagination, name: String?): List<Product> {
        val filter = Example.of(Product(name = StringUtils.trimToNull(name)))
        val paginationNormalized = paginationService.parsePagination(pagination)
        return productRepository.findAll(filter, paginationNormalized).content
    }

    @Transactional
    fun save(product: Product) = product.let {
        validateSave(it)
        productRepository.save(it)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        if (!productRepository.existsById(id)) {
            return false
        }
        productRepository.deleteById(id)
        return true
    }

    private fun validateSave(product: Product) = validationService.apply {
        addIfItemConditionIsTrue(StringUtils.isBlank(product.name), ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ITEM_VALIDATION_ERROR_PRODUCT_NAME_NOT_BLACK)
        addIfItemConditionIsTrueAndNotHasError({ StringUtils.length(product.name) > PRODUCT_NAME_MAX_SIZE }, ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ITEM_VALIDATION_ERROR_PRODUCT_NAME_MAX_SIZE)
        addIfItemConditionIsTrueAndNotHasError({ isDuplicateProduct(product) }, ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ITEM_VALIDATION_ERROR_PRODUCT_DUPLICATE)
        addIfItemConditionIsTrue(StringUtils.length(product.description) > PRODUCT_DESCRIPTION_MAX_SIZE, ITEM_VALIDATION_LOCATION_PRODUCT_DESCRIPTION, ITEM_VALIDATION_ERROR_PRODUCT_DESCRIPTION_MAX_SIZE)
        addIfItemConditionIsTrue(product.value != null && product.value!! <= NumberUtils.DOUBLE_ZERO, ITEM_VALIDATION_LOCATION_PRODUCT_VALUE, ITEM_VALIDATION_ERROR_PRODUCT_VALUE_NOT_NEGATIVE)
        addIfItemConditionIsTrue(CollectionUtils.isEmpty(product.categories), ITEM_VALIDATION_LOCATION_PRODUCT_CATEGORY, ITEM_VALIDATION_ERROR_PRODUCT_CATEGORY_REQUIRED)
    }.validate()

    fun isDuplicateProduct(product: Product) =
            product.id?.let {
                productRepository.existsByIdNotAndNameIgnoreCase(it, product.name)
            } ?: productRepository.existsByNameIgnoreCase(product.name)
}

