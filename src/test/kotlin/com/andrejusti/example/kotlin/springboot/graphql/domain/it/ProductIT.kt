package com.andrejusti.example.kotlin.springboot.graphql.domain.it

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import com.andrejusti.example.kotlin.springboot.graphql.domain.AbstractIT
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Category
import com.andrejusti.example.kotlin.springboot.graphql.domain.entity.Product
import com.andrejusti.example.kotlin.springboot.graphql.domain.sdk.CategorySdk
import com.andrejusti.example.kotlin.springboot.graphql.domain.sdk.ProductSdk
import com.andrejusti.example.kotlin.springboot.graphql.domain.service.ProductService
import com.andrejusti.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

class ProductIT : AbstractIT() {

    @Autowired
    lateinit var productSdk: ProductSdk

    @Autowired
    lateinit var categorySdk: CategorySdk

    @Test
    fun `Search product by id without expecting result`() {
        val product = productSdk.product(Int.MAX_VALUE.toLong())
        Assert.assertNull("Should not return product", product)
    }

    @Test
    fun `Search products without expecting result`() {
        val products = productSdk.products(createPagination(), createRandomValue())
        Assert.assertNotNull("Should not return null", products)
    }

    @Test
    fun `Search products by name expecting results`() {
        val product = createAndSaveProduct()
        val productSearch = productSdk.products(createPagination(), product.name!!).firstOrNull { it.id == product.id }
        Assert.assertEquals("Product retrieved is different from saved", product, productSearch)
    }

    @Test
    fun `Delete nonexistent product`() {
        val hasDeleted = productSdk.deleteProduct(Int.MAX_VALUE.toLong())
        Assert.assertFalse("Should not remove product", hasDeleted)
    }

    @Test
    fun `Create product checking id`() {
        val product = createAndSaveProduct()
        Assert.assertNotNull("Should return id", product.id)
    }

    @Test
    fun `Delete product checking find by id`() {
        assertDeleteProduct(productSdk::product)
    }

    @Test
    fun `Delete product checking find all`() {
        assertDeleteProduct(::findAllProductId)
    }

    @Test
    fun `Create product checking find by id`() {
        assertCreateProduct(productSdk::product)
    }

    @Test
    fun `Create product checking find all`() {
        assertCreateProduct(::findAllProductId)
    }

    @Test
    fun `Edit product checking find by id`() {
        assertEditProduct(productSdk::product)
    }

    @Test
    fun `Edit product checking find all`() {
        assertEditProduct(::findAllProductId)
    }

    @Test
    fun `Create product checking validation - product duplicate`() {
        val product = createProduct()
        productSdk.createProduct(product)
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_DUPLICATE, { productSdk.createProduct(product) })
    }

    @Test
    fun `Edit product checking validation - product duplicate`() {
        val product1 = createAndSaveProduct()
        val product2 = createAndSaveProduct()
        product1.name = product2.name
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_DUPLICATE, { productSdk.editProduct(product1) })
    }

    @Test
    fun `Create product checking validation - product name not black`() {
        assertValidationExceptionProductNameNotBlack(createProduct(), { productSdk.createProduct(it) })
    }

    @Test
    fun `Edit product checking validation - product name not black`() {
        assertValidationExceptionProductNameNotBlack(createAndSaveProduct(), { productSdk.editProduct(it) })
    }

    @Test
    fun `Create product checking validation - product name max size`() {
        assertValidationExceptionProductNameMaxSize(createProduct(), { productSdk.createProduct(it) })
    }

    @Test
    fun `Edit product checking validation - product name max size`() {
        assertValidationExceptionProductNameMaxSize(createAndSaveProduct(), { productSdk.editProduct(it) })
    }

    @Test
    fun `Create product checking validation - product description max size`() {
        assertValidationExceptionProductDescriptionMaxSize(createProduct(), { productSdk.createProduct(it) })
    }

    @Test
    fun `Edit product checking validation - product description max size`() {
        assertValidationExceptionProductDescriptionMaxSize(createAndSaveProduct(), { productSdk.editProduct(it) })
    }

    @Test
    fun `Create product checking validation - product value not negative`() {
        assertValidationExceptionProductValueNotNegative(createProduct(), { productSdk.createProduct(it) })
    }

    @Test
    fun `Edit product checking validation - product value not negative`() {
        assertValidationExceptionProductValueNotNegative(createAndSaveProduct(), { productSdk.editProduct(it) })
    }

    @Test
    fun `Create product checking validation - product category required`() {
        assertValidationExceptionProductCategoryRequired(createProduct(), { productSdk.createProduct(it) })
    }

    @Test
    fun `Edit product checking validation - product category required`() {
        assertValidationExceptionProductCategoryRequired(createAndSaveProduct(), { productSdk.editProduct(it) })
    }

    @Test
    fun `Search find all without informing pagination`() {
        productSdk.products(Pagination(), StringUtils.EMPTY)
    }

    private fun findAllProductId(productId: Long) = productSdk.products(createPagination(), StringUtils.EMPTY).firstOrNull { it.id == productId }

    private fun createAndSaveCategory() = categorySdk.createCategory(createCategory())

    private fun createCategory() = Category(name = createRandomValue())

    private fun createAndSaveProduct() = productSdk.createProduct(createProduct())

    private fun createProduct() = Product(
            name = createRandomValue(),
            description = createRandomValue(),
            value = NumberUtils.DOUBLE_ONE,
            categories = listOf(createAndSaveCategory())
    )

    private fun assertDeleteProduct(searchProduct: (Long) -> Product?) {
        val product = assertCreateProduct(searchProduct)
        productSdk.deleteProduct(product.id!!)
        val productSearchAfterDelete = searchProduct(product.id!!)
        Assert.assertNull("Should not return to product after deleting", productSearchAfterDelete)
    }

    private fun assertEditProduct(searchProduct: (Long) -> Product?) {
        val product = assertCreateProduct(searchProduct)
        product.name = createRandomValue()
        productSdk.editProduct(product)
        val productSearch = searchProduct(product.id!!)
        Assert.assertEquals("Product retrieved is different from edited", product, productSearch)
    }

    private fun assertCreateProduct(searchProduct: (Long) -> Product?): Product {
        val product = createAndSaveProduct()
        val productId = product.id
        Assert.assertNotNull("Should return id", productId)
        val productSearch = searchProduct(productId!!)
        Assert.assertEquals("Product retrieved is different from saved", product, productSearch)
        return productSearch!!
    }

    private fun assertValidationExceptionProductNameNotBlack(product: Product, execValidationException: (Product) -> Unit) {
        product.name = StringUtils.EMPTY
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_NAME_NOT_BLACK, exec)
    }

    private fun assertValidationExceptionProductNameMaxSize(product: Product, execValidationException: (Product) -> Unit) {
        product.name = StringUtils.repeat("A", ProductService.PRODUCT_NAME_MAX_SIZE + NumberUtils.INTEGER_ONE)
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_NAME_MAX_SIZE, exec)
    }

    private fun assertValidationExceptionProductDescriptionMaxSize(product: Product, execValidationException: (Product) -> Unit) {
        product.description = StringUtils.repeat("A", ProductService.PRODUCT_DESCRIPTION_MAX_SIZE + NumberUtils.INTEGER_ONE)
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_DESCRIPTION_MAX_SIZE, exec)
    }

    private fun assertValidationExceptionProductValueNotNegative(product: Product, execValidationException: (Product) -> Unit) {
        product.value = NumberUtils.DOUBLE_MINUS_ONE
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_VALUE_NOT_NEGATIVE, exec)
    }

    private fun assertValidationExceptionProductCategoryRequired(product: Product, execValidationException: (Product) -> Unit) {
        product.categories = null
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_ERROR_PRODUCT_CATEGORY_REQUIRED, exec)
    }
}

