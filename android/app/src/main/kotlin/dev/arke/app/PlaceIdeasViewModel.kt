package dev.arke.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.arke.core.PlaceDraft
import dev.arke.core.PlaceIdea
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaceIdeasUiState(val ideas: List<PlaceIdea> = emptyList(), val ready: Boolean = false, val busy: Boolean = false, val error: String? = null)

class PlaceIdeasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlaceIdeasRepository(application)
    private val mutableState = MutableStateFlow(PlaceIdeasUiState())
    val state = mutableState.asStateFlow()
    init { load() }

    fun load() {
        if (mutableState.value.busy) return
        mutableState.value = mutableState.value.copy(busy = true)
        viewModelScope.launch {
            try { mutableState.value = PlaceIdeasUiState(ideas = repository.load(), ready = true) }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { mutableState.value = mutableState.value.copy(busy = false, ready = false, error = "Não foi possível abrir seus lugares. Tente novamente; os dados existentes foram preservados.") }
        }
    }

    fun add(draft: PlaceDraft) {
        if (!mutableState.value.ready || mutableState.value.busy) return
        mutableState.value = mutableState.value.copy(busy = true)
        viewModelScope.launch {
            try {
                mutableState.value = PlaceIdeasUiState(ideas = repository.add(draft), ready = true)
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { mutableState.value = mutableState.value.copy(busy = false, error = "Não foi possível salvar. Seu rascunho continua aberto para tentar novamente.") }
        }
    }

    fun remove(id: String) {
        if (!mutableState.value.ready || mutableState.value.busy) return
        mutableState.value = mutableState.value.copy(busy = true)
        viewModelScope.launch {
            try { mutableState.value = PlaceIdeasUiState(ideas = repository.remove(id), ready = true) }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { mutableState.value = mutableState.value.copy(busy = false, error = "Não foi possível excluir o lugar. Tente novamente.") }
        }
    }
}
