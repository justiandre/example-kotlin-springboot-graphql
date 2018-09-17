package tech.justi.example.kotlin.springboot.graphql.domain.controller.graphql.resolver

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tech.justi.example.kotlin.springboot.graphql.domain.entity.Category
import tech.justi.example.kotlin.springboot.graphql.domain.entity.Product
import tech.justi.example.kotlin.springboot.graphql.domain.service.ProductService
import tech.justi.example.kotlin.springboot.graphql.infrastructure.dto.Pagination

@Component
class ProductGraphQlResolver(
        @Autowired val productService: ProductService
) : GraphQLQueryResolver, GraphQLMutationResolver {

    fun product(id: Long) = productService.findById(id)

    fun products(pagination: Pagination, name: String?) = productService.findAllByName(pagination, name)

    fun createProduct(product: ProductInput) = productService.save(parseProduct(product))

    fun editProduct(id: Long, product: ProductInput) = productService.save(parseProduct(product).apply { this.id = id })

    fun deleteProduct(id: Long) = productService.delete(id)

    private fun parseProduct(productInput: ProductInput) =
            Product(
                    name = productInput.name,
                    description = productInput.description,
                    value = productInput.value,
                    categories = productInput.categories?.map { Category(id = it) }
            )

    object ProductInput {
        var name: String? = null
        var description: String? = null
        var value: Double? = null
        var categories: List<Long>? = null
    }
}
