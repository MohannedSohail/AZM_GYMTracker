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
            kotlinx.coroutines.flow.combine(
                repository.getAllMembers(),
                repository.getActiveMemberId(),
            ) { members, activeId ->
                val active = members.find { it.id == activeId } ?: members.firstOrNull()
                Pair(members, active)
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect { (members, active) ->
                _uiState.update { current ->
                    current.copy(
                        members = members,
                        isLoading = false,
                        activeMember = active,
                    )
                }
            }
        }
    }

    /** Sets the currently active member (switching between profiles). */
    fun selectMember(member: Member) {
        _uiState.update { it.copy(activeMember = member, error = null) }
        viewModelScope.launch {
            repository.setActiveMemberId(member.id)
        }
    }

    /**
     * Validates and creates a new member.
     * Updates [MemberUiState.nameError] inline if validation fails.
     * Only invokes [onSuccess] after successful database creation.
     */
    fun createMember(name: String, colorHex: String, onSuccess: () -> Unit = {}) {
        val validationResult = MemberNameValidator.validate(name)
        if (validationResult != MemberNameValidator.Result.Valid) {
            val errorMsg = when (validationResult) {
                MemberNameValidator.Result.Blank -> "الاسم لا يمكن أن يكون فارغاً"
                is MemberNameValidator.Result.TooLong ->
                    "يجب ألا يتجاوز الاسم ${validationResult.max} حرفاً"
                MemberNameValidator.Result.Valid -> null
            }
            _uiState.update { it.copy(nameError = errorMsg) }
            return
        }

        val trimmed = name.trim()
        if (_uiState.value.members.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _uiState.update { it.copy(nameError = "هذا الاسم مستخدم بالفعل") }
            return
        }

        _uiState.update { it.copy(nameError = null) }
        viewModelScope.launch {
            runCatching {
                val newId = repository.createMember(Member(name = trimmed, colorHex = colorHex))
                repository.setActiveMemberId(newId)
            }.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(nameError = e.message ?: "فشل إنشاء العضو")
                }
            }
        }
    }

    /**
     * Validates and updates an existing member's profile.
     */
    fun updateMember(member: Member, onSuccess: () -> Unit = {}) {
        val validationResult = MemberNameValidator.validate(member.name)
        if (validationResult != MemberNameValidator.Result.Valid) {
            val errorMsg = when (validationResult) {
                MemberNameValidator.Result.Blank -> "الاسم لا يمكن أن يكون فارغاً"
                is MemberNameValidator.Result.TooLong ->
                    "يجب ألا يتجاوز الاسم ${validationResult.max} حرفاً"
                MemberNameValidator.Result.Valid -> null
            }
            _uiState.update { it.copy(nameError = errorMsg) }
            return
        }

        val trimmed = member.name.trim()
        if (_uiState.value.members.any { it.id != member.id && it.name.equals(trimmed, ignoreCase = true) }) {
            _uiState.update { it.copy(nameError = "هذا الاسم مستخدم بالفعل") }
            return
        }

        _uiState.update { it.copy(nameError = null) }
        viewModelScope.launch {
            runCatching {
                repository.updateMember(member.copy(name = trimmed))
            }.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "فشل تعديل العضو") }
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
