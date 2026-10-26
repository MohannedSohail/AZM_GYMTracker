package com.example.azm_gymtracker.presentation.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.validator.MemberNameValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable UI state for the member management screen. */
data class MemberUiState(
    val members: List<Member> = emptyList(),
    val activeMember: Member? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Validation error for the member name input field. */
    val nameError: String? = null,
)

/**
 * ViewModel for member creation, listing, selection, and deletion.
 *
 * Exposes [uiState] as an immutable [StateFlow] collected with lifecycle awareness.
 * All mutations follow Unidirectional Data Flow (UDF).
 */
class MemberViewModel(private val repository: MemberRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberUiState())
    val uiState: StateFlow<MemberUiState> = _uiState.asStateFlow()

    init {
        observeMembers()
    }

    private fun observeMembers() {
        viewModelScope.launch {
            repository.getAllMembers()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { members ->
                    _uiState.update { current ->
                        current.copy(
                            members = members,
                            isLoading = false,
                            // Auto-select first member if none is selected
                            activeMember = current.activeMember ?: members.firstOrNull(),
                        )
                    }
                }
        }
    }

    /** Sets the currently active member (switching between profiles). */
    fun selectMember(member: Member) {
        _uiState.update { it.copy(activeMember = member, error = null) }
    }

    /**
     * Validates and creates a new member.
     * Updates [MemberUiState.nameError] inline if validation fails.
     */
    fun createMember(name: String, colorHex: String) {
        val validationResult = MemberNameValidator.validate(name)
        if (validationResult != MemberNameValidator.Result.Valid) {
            val errorMsg = when (validationResult) {
                MemberNameValidator.Result.Blank -> "Name cannot be blank"
                is MemberNameValidator.Result.TooLong ->
                    "Name must be ${validationResult.max} characters or fewer"
                MemberNameValidator.Result.Valid -> null
            }
            _uiState.update { it.copy(nameError = errorMsg) }
            return
        }

        _uiState.update { it.copy(nameError = null) }
        viewModelScope.launch {
            runCatching {
                repository.createMember(Member(name = name, colorHex = colorHex))
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to create member")
                }
            }
        }
    }

    /** Clears the name validation error (e.g. when the user starts typing). */
    fun clearNameError() {
        _uiState.update { it.copy(nameError = null) }
    }

    /**
     * Permanently deletes [member] and all their workout history.
     * If the deleted member was active, the next available member is auto-selected.
     */
    fun deleteMember(member: Member) {
        viewModelScope.launch {
            runCatching { repository.deleteMember(member) }
                .onSuccess {
                    _uiState.update { current ->
                        val newActive = if (current.activeMember?.id == member.id) {
                            current.members.firstOrNull { it.id != member.id }
                        } else {
                            current.activeMember
                        }
                        current.copy(activeMember = newActive, error = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to delete member") }
                }
        }
    }

    /** Factory for creating [MemberViewModel] with a [MemberRepository] dependency. */
    class Factory(private val repository: MemberRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MemberViewModel(repository) as T
    }
}
