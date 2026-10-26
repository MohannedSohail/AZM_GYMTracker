package com.example.azm_gymtracker.presentation

import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.presentation.member.MemberViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemberViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMemberRepository
    private lateinit var viewModel: MemberViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMemberRepository()
        viewModel = MemberViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsMembersAndSelectsFirst() = runTest(testDispatcher) {
        val member1 = Member(id = 1, name = "Mohanned", colorHex = "#8CFF68")
        val member2 = Member(id = 2, name = "Ahmed", colorHex = "#42E879")
        fakeRepository.emitMembers(listOf(member1, member2))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.members.size)
        assertEquals(member1, state.activeMember)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun selectMember_updatesActiveMemberAndPersistsId() = runTest(testDispatcher) {
        val member1 = Member(id = 1, name = "Mohanned", colorHex = "#8CFF68")
        val member2 = Member(id = 2, name = "Ahmed", colorHex = "#42E879")
        fakeRepository.emitMembers(listOf(member1, member2))
        advanceUntilIdle()

        viewModel.selectMember(member2)
        advanceUntilIdle()

        assertEquals(member2, viewModel.uiState.value.activeMember)
        assertEquals(2L, fakeRepository.activeMemberIdFlow.value)
    }

    @Test
    fun createMember_withBlankName_setsNameError() = runTest(testDispatcher) {
        viewModel.createMember("   ", "#8CFF68")
        advanceUntilIdle()

        assertEquals("الاسم لا يمكن أن يكون فارغاً", viewModel.uiState.value.nameError)
    }

    @Test
    fun createMember_withValidName_callsRepository() = runTest(testDispatcher) {
        viewModel.createMember("Khaled", "#8CFF68")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.nameError)
        assertEquals(1, fakeRepository.createdMembers.size)
        assertEquals("Khaled", fakeRepository.createdMembers.first().name)
    }

    @Test
    fun deleteMember_whenActive_autoSelectsNextMember() = runTest(testDispatcher) {
        val member1 = Member(id = 1, name = "Mohanned", colorHex = "#8CFF68")
        val member2 = Member(id = 2, name = "Ahmed", colorHex = "#42E879")
        fakeRepository.emitMembers(listOf(member1, member2))
        advanceUntilIdle()

        viewModel.deleteMember(member1)
        advanceUntilIdle()

        assertEquals(member2, viewModel.uiState.value.activeMember)
    }

    private class FakeMemberRepository : MemberRepository {
        private val _flow = MutableStateFlow<List<Member>>(emptyList())
        val activeMemberIdFlow = MutableStateFlow<Long?>(null)
        val createdMembers = mutableListOf<Member>()

        fun emitMembers(list: List<Member>) {
            _flow.value = list
        }

        override fun getAllMembers(): Flow<List<Member>> = _flow.asStateFlow()
        override suspend fun getMemberCount(): Int = _flow.value.size

        override suspend fun createMember(member: Member): Long {
            createdMembers.add(member)
            val updated = _flow.value + member.copy(id = (createdMembers.size).toLong())
            _flow.value = updated
            return updated.last().id
        }

        override suspend fun updateMember(member: Member) {
            _flow.value = _flow.value.map { if (it.id == member.id) member else it }
        }

        override suspend fun deleteMember(member: Member) {
            _flow.value = _flow.value.filter { it.id != member.id }
        }

        override fun getActiveMemberId(): Flow<Long?> = activeMemberIdFlow.asStateFlow()

        override suspend fun setActiveMemberId(memberId: Long) {
            activeMemberIdFlow.value = memberId
        }
    }
}
