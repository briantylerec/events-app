package com.redbull.lab.events.data

import com.redbull.lab.domain.Product
import com.redbull.lab.events.domain.ProductCatalogRepository
import com.redbull.lab.sdk.RedBullSdk
import javax.inject.Inject

/** Adapts the shared SDK to the Events app's domain contract. */
class SdkProductCatalogRepository @Inject constructor(
    private val sdk: RedBullSdk,
) : ProductCatalogRepository {
    override suspend fun loadProducts(): List<Product> = sdk.getProducts()
}
