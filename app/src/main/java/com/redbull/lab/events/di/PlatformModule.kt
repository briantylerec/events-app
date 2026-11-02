package com.redbull.lab.events.di

import com.redbull.lab.events.data.SdkProductCatalogRepository
import com.redbull.lab.events.domain.ProductCatalogRepository
import com.redbull.lab.sdk.RedBullSdk
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds @Singleton
    abstract fun bindProductCatalogRepository(implementation: SdkProductCatalogRepository): ProductCatalogRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {
    @Provides @Singleton
    fun provideSdk(): RedBullSdk = RedBullSdk.create()
}
