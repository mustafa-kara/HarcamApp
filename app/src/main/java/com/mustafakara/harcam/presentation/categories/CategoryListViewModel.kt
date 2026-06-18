package com.mustafakara.harcam.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.DeleteCategoryUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategorySpendUseCase
import com.mustafakara.harcam.domain.usecase.UpsertCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Category list ViewModel — category_list.md §7. Composes the per-category month spend and the
 * preferred currency into one reactive [CategoryListUiState]; the add/edit sheet and the delete
 * dialog are local UI state layered on top via [editorState] (combined into the stream).
 */
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    observeCategorySpend: ObserveCategorySpendUseCase,
    preferences: PreferencesRepository,
    private val upsertCategory: UpsertCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
) : ViewModel() {

    private data class EditorState(
        val editing: CategoryEditState? = null,
        val deleteTarget: Category? = null,
    )

    private val editorState = MutableStateFlow(EditorState())

    val uiState: StateFlow<CategoryListUiState> = combine(
        observeCategorySpend(),
        preferences.observe(),
        editorState,
    ) { items, prefs, editor ->
        CategoryListUiState(
            isLoading = false,
            currency = prefs.currency,
            items = items,
            editing = editor.editing,
            deleteTarget = editor.deleteTarget,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = CategoryListUiState(isLoading = true),
    )

    fun startAdd() {
        editorState.update { it.copy(editing = CategoryEditState()) }
    }

    fun startEdit(category: Category) {
        editorState.update {
            it.copy(
                editing = CategoryEditState(
                    id = category.id,
                    name = category.name,
                    colorKey = category.colorKey,
                    iconKey = category.iconKey,
                ),
            )
        }
    }

    fun updateName(name: String) {
        editorState.update { state ->
            state.editing?.let { state.copy(editing = it.copy(name = name, nameError = false)) }
                ?: state
        }
    }

    fun updateColor(colorKey: String) {
        editorState.update { state ->
            state.editing?.let { state.copy(editing = it.copy(colorKey = colorKey)) } ?: state
        }
    }

    fun updateIcon(iconKey: String) {
        editorState.update { state ->
            state.editing?.let { state.copy(editing = it.copy(iconKey = iconKey)) } ?: state
        }
    }

    fun saveEdit() {
        val editing = editorState.value.editing ?: return
        val name = editing.name.trim()
        val existingNames = uiState.value.items
            .map { it.category }
            .filter { it.id != editing.id }
            .map { it.name.trim().lowercase() }
        if (name.isBlank() || name.lowercase() in existingNames) {
            editorState.update { it.copy(editing = editing.copy(nameError = true)) }
            return
        }
        viewModelScope.launch {
            upsertCategory(
                Category(
                    id = editing.id ?: 0L,
                    name = name,
                    colorKey = editing.colorKey,
                    iconKey = editing.iconKey,
                ),
            )
            editorState.update { it.copy(editing = null) }
        }
    }

    fun dismissEdit() {
        editorState.update { it.copy(editing = null) }
    }

    fun requestDelete(category: Category) {
        if (isOther(category)) return
        editorState.update { it.copy(deleteTarget = category) }
    }

    fun confirmDelete() {
        val target = editorState.value.deleteTarget ?: return
        if (isOther(target)) {
            editorState.update { it.copy(deleteTarget = null) }
            return
        }
        val other = uiState.value.items.map { it.category }.firstOrNull { isOther(it) }
        if (other == null) {
            editorState.update { it.copy(deleteTarget = null) }
            return
        }
        viewModelScope.launch {
            deleteCategory(target.id, other.id)
            editorState.update { it.copy(deleteTarget = null) }
        }
    }

    fun cancelDelete() {
        editorState.update { it.copy(deleteTarget = null) }
    }

    private fun isOther(category: Category): Boolean =
        category.colorKey == "other" || category.name.equals("Other", ignoreCase = true)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
