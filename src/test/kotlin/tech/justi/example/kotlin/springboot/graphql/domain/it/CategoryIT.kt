package tech.justi.example.kotlin.springboot.graphql.domain.it

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import tech.justi.example.kotlin.springboot.graphql.domain.AbstractIT
import tech.justi.example.kotlin.springboot.graphql.domain.entity.Category
import tech.justi.example.kotlin.springboot.graphql.domain.entity.Product
import tech.justi.example.kotlin.springboot.graphql.domain.sdk.CategorySdk
import tech.justi.example.kotlin.springboot.graphql.domain.sdk.ProductSdk
import tech.justi.example.kotlin.springboot.graphql.domain.service.CategoryService
import tech.justi.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

class CategoryIT : AbstractIT() {

    @Autowired
    lateinit var productSdk: ProductSdk

    @Autowired
    lateinit var categorySdk: CategorySdk

    @Test
    fun `Search category by id without expecting result`() {
        val category = categorySdk.category(Int.MAX_VALUE.toLong())
        Assert.assertNull("Should not return category", category)
    }

    @Test
    fun `Search categories without expecting result`() {
        val categories = categorySdk.categories(createPagination())
        Assert.assertNotNull("Should not return null", categories)
    }

    @Test
    fun `Delete nonexistent category`() {
        val hasDeleted = categorySdk.deleteCategory(Int.MAX_VALUE.toLong())
        Assert.assertFalse("Should not remove category", hasDeleted)
    }

    @Test
    fun `Create category checking id`() {
        val category = createAndSaveCategory()
        Assert.assertNotNull("Should return id", category.id)
    }

    @Test
    fun `Delete category checking find by id`() {
        assertDeleteCategory(categorySdk::category)
    }

    @Test
    fun `Delete category checking find all`() {
        assertDeleteCategory(::findAllCategoryId)
    }

    @Test
    fun `Delete category checking validation - category product relationship`() {
        val category = createAndSaveCategory()
        productSdk.createProduct(Product(name = createRandomValue(), categories = listOf(category)))
        val execValidationException: () -> Unit = { categorySdk.deleteCategory(category.id!!) }
        assertValidationException(CategoryService.ITEM_VALIDATION_ERROR_CATEGORY_RELATIONSHIP, execValidationException)
    }

    @Test
    fun `Create category checking find by id`() {
        assertCreateCategory(categorySdk::category)
    }

    @Test
    fun `Create category checking find all`() {
        assertCreateCategory(::findAllCategoryId)
    }

    @Test
    fun `Edit category checking find by id`() {
        assertEditCategory(categorySdk::category)
    }

    @Test
    fun `Edit category checking find all`() {
        assertEditCategory(::findAllCategoryId)
    }

    @Test
    fun `Create category checking validation - category duplicate`() {
        val category = createCategory()
        categorySdk.createCategory(category)
        assertValidationException(CategoryService.ITEM_VALIDATION_ERROR_CATEGORY_DUPLICATE, { categorySdk.createCategory(category) })
    }

    @Test
    fun `Edit category checking validation - category duplicate`() {
        val category1 = createAndSaveCategory()
        val category2 = createAndSaveCategory()
        category1.name = category2.name
        assertValidationException(CategoryService.ITEM_VALIDATION_ERROR_CATEGORY_DUPLICATE, { categorySdk.editCategory(category1) })
    }

    @Test
    fun `Create category checking validation - category name not black`() {
        assertValidationExceptionCategoryNameNotBlack(createCategory(), { categorySdk.createCategory(it) })
    }

    @Test
    fun `Edit category checking validation - category name not black`() {
        assertValidationExceptionCategoryNameNotBlack(createAndSaveCategory(), { categorySdk.editCategory(it) })
    }

    @Test
    fun `Create category checking validation - category name max size`() {
        assertValidationExceptionCategoryNameMaxSize(createCategory(), { categorySdk.createCategory(it) })
    }

    @Test
    fun `Edit category checking validation - category name max size`() {
        assertValidationExceptionCategoryNameMaxSize(createAndSaveCategory(), { categorySdk.editCategory(it) })
    }

    @Test
    fun `Search find all without informing pagination`() {
        categorySdk.categories(Pagination())
    }

    private fun findAllCategoryId(categoryId: Long) = categorySdk.categories(createPagination()).firstOrNull { it.id == categoryId }

    private fun assertDeleteCategory(searchCategory: (Long) -> Category?) {
        val category = assertCreateCategory(searchCategory)
        categorySdk.deleteCategory(category.id!!)
        val categorySearchAfterDelete = searchCategory(category.id!!)
        Assert.assertNull("Should not return to category after deleting", categorySearchAfterDelete)
    }

    private fun assertEditCategory(searchCategory: (Long) -> Category?) {
        val category = assertCreateCategory(searchCategory)
        category.name = createRandomValue()
        categorySdk.editCategory(category)
        val categorySearch = searchCategory(category.id!!)
        Assert.assertEquals("Category retrieved is different from edited", category, categorySearch)
    }

    private fun assertCreateCategory(searchCategory: (Long) -> Category?): Category {
        val category = createAndSaveCategory()
        val categoryId = category.id
        Assert.assertNotNull("Should return id", categoryId)
        val categorySearch = searchCategory(categoryId!!)
        Assert.assertEquals("Category retrieved is different from saved", category, categorySearch)
        return categorySearch!!
    }

    private fun assertValidationExceptionCategoryNameNotBlack(category: Category, execValidationException: (Category) -> Unit) {
        category.name = StringUtils.EMPTY
        val exec = { execValidationException(category) }
        assertValidationException(CategoryService.ITEM_VALIDATION_ERROR_CATEGORY_NAME_NOT_BLACK, exec)
    }

    private fun assertValidationExceptionCategoryNameMaxSize(category: Category, execValidationException: (Category) -> Unit) {
        category.name = StringUtils.repeat("A", CategoryService.CATEGORY_NAME_MAX_SIZE + NumberUtils.INTEGER_ONE)
        val exec = { execValidationException(category) }
        assertValidationException(CategoryService.ITEM_VALIDATION_ERROR_CATEGORY_NAME_MAX_SIZE, exec)
    }

    private fun createAndSaveCategory() = categorySdk.createCategory(createCategory())

    private fun createCategory() = Category(name = createRandomValue())
}

