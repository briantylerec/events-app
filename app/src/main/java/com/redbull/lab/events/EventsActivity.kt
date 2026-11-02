package com.redbull.lab.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.redbull.lab.events.presentation.EventsViewModel
import com.redbull.lab.events.presentation.resources.Dimensions
import com.redbull.lab.ui.PlatformEmptyState
import com.redbull.lab.ui.PlatformErrorState
import com.redbull.lab.ui.PlatformLoadingIndicator
import com.redbull.lab.ui.ProductCard
import com.redbull.lab.ui.RedBullTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsActivity : ComponentActivity() {
    private val viewModel: EventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedBullTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                Column(Modifier.fillMaxSize().padding(Dimensions.Dim16dp)) {
                    Text(stringResource(R.string.events_title), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.events_subtitle), style = MaterialTheme.typography.bodyMedium)
                    when {
                        state.isLoading -> PlatformLoadingIndicator()
                        state.hasError -> PlatformErrorState(state.errorMessage.orEmpty().ifBlank { stringResource(R.string.load_error) }, viewModel::refresh)
                        state.highlights.isEmpty() -> PlatformEmptyState(stringResource(R.string.empty_highlights))
                        else -> LazyColumn {
                            items(state.highlights, key = { it.id }) { product ->
                                ProductCard(product.title, stringResource(R.string.product_details, product.price.toString(), product.description))
                            }
                        }
                    }
                }
            }
        }
    }
}
