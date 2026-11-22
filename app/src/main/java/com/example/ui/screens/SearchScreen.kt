package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Category
import com.example.domain.model.ClipContentType
import com.example.domain.model.ClipItem
import com.example.domain.usecase.DateFilter
import com.example.domain.usecase.SearchFilter
import com.example.domain.usecase.SearchSortOrder
import com.example.ui.components.ClipCard
import com.example.ui.components.EmptyStateView

@Composable
fun SearchScreen(
  filter: SearchFilter,
  results: List<ClipItem>,
  categories: List<Category>,
  onQueryChange: (String) -> Unit,
  onCategoryChange: (Long?) -> Unit,
  onTypeChange: (ClipContentType?) -> Unit,
  onDateFilterChange: (DateFilter) -> Unit,
  onSortOrderChange: (SearchSortOrder) -> Unit,
  onOpenClipDetails: (Long) -> Unit,
  onCopyClip: (ClipItem) -> Unit,
  onTogglePin: (ClipItem) -> Unit,
  onToggleFavorite: (ClipItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val categoryMap = categories.associateBy { it.id }

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
      OutlinedTextField(
        value = filter.query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
          if (filter.query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
              Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
            }
          }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("search_input"),
      )

      // Date & Sort chips
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DateFilter.entries.forEach { df ->
          val label =
            when (df) {
              DateFilter.ALL -> stringResource(R.string.filter_all)
              DateFilter.TODAY -> stringResource(R.string.filter_today)
              DateFilter.LAST_7_DAYS -> stringResource(R.string.filter_week)
              DateFilter.LAST_30_DAYS -> stringResource(R.string.filter_month)
            }
          FilterChip(
            selected = filter.dateFilter == df,
            onClick = { onDateFilterChange(df) },
            label = { Text(label) },
          )
        }
        SearchSortOrder.entries.forEach { order ->
          FilterChip(
            selected = filter.sortOrder == order,
            onClick = { onSortOrderChange(order) },
            label = { Text(order.name.lowercase().replaceFirstChar { it.uppercase() }) },
          )
        }
      }

      // Type & Category chips
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ClipContentType.entries.forEach { type ->
          FilterChip(
            selected = filter.contentType == type,
            onClick = { onTypeChange(if (filter.contentType == type) null else type) },
            label = { Text(type.name) },
          )
        }
        categories.forEach { cat ->
          FilterChip(
            selected = filter.categoryId == cat.id,
            onClick = { onCategoryChange(if (filter.categoryId == cat.id) null else cat.id) },
            label = { Text(cat.name) },
          )
        }
      }

      Text(
        text = "${results.size} results (offline)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
      )

      if (results.isEmpty()) {
        EmptyStateView(
          title = stringResource(R.string.search_no_results),
          subtitle = "Try another keyword, tag, URL, or clear filters.",
          showArtwork = false,
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(results, key = { it.id }) { clip ->
            ClipCard(
              clip = clip,
              category = categoryMap[clip.categoryId],
              onClick = { onOpenClipDetails(clip.id) },
              onCopyClick = { onCopyClip(clip) },
              onPinToggle = { onTogglePin(clip) },
              onFavoriteToggle = { onToggleFavorite(clip) },
            )
          }
          item { Spacer(modifier = Modifier.height(72.dp)) }
        }
      }
    }
  }
}
