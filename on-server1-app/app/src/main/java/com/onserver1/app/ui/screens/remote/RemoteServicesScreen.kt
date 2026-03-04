package com.onserver1.app.ui.screens.remote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.onserver1.app.R
import com.onserver1.app.ui.components.ProductCard
import com.onserver1.app.ui.theme.AccentYellow
import com.onserver1.app.ui.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteServicesScreen(
    onProductClick: (String) -> Unit,
    viewModel: RemoteServicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val d = LocalDimens.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.remote_services),
                    fontSize = d.font22,
                    fontWeight = FontWeight.Bold
                )
            }
        )

        // Group Dropdown
        if (state.groups.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = d.screenPadding)
            ) {
                OutlinedTextField(
                    value = state.selectedGroup ?: stringResource(R.string.all_groups),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(d.corner12),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.all_groups),
                                fontWeight = if (state.selectedGroup == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            viewModel.selectGroup(null)
                            expanded = false
                        }
                    )
                    state.groups.forEach { group ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    group,
                                    fontWeight = if (state.selectedGroup == group) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                viewModel.selectGroup(group)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(d.space8))
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchProducts(it)
            },
            placeholder = { Text(stringResource(R.string.search_remote)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.screenPadding),
            shape = RoundedCornerShape(d.corner12),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(d.space12))

        // Products Grid
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentYellow)
            }
        } else if (state.products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_products),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = d.font16
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(d.screenPadding),
                verticalArrangement = Arrangement.spacedBy(d.space12),
                horizontalArrangement = Arrangement.spacedBy(d.space12)
            ) {
                items(state.products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
