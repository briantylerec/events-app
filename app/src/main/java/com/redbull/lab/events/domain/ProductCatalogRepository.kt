package com.redbull.lab.events.domain

import com.redbull.lab.domain.Product
import javax.inject.Inject

/** App-facing contract for the catalog data used by the event highlights screen. */
interface ProductCatalogRepository {
    suspend fun loadProducts(): List<Product>
}

/** Applies the event screen's presentation-independent curation rule. */
class GetEventHighlightsUseCase @Inject constructor(private val repository: ProductCatalogRepository) {
    suspend operator fun invoke(): List<Product> = repository.loadProducts()
        .filter { it.price >= 0.0 }
        .sortedByDescending { it.price }
        .take(HIGHLIGHT_LIMIT)

    private companion object {
        const val HIGHLIGHT_LIMIT = 6
    }
}
