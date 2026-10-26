package com.example.azm_gymtracker

import android.app.Application
import com.example.azm_gymtracker.data.local.db.AZMDatabase
import com.example.azm_gymtracker.data.repository.ExerciseRepositoryImpl
import com.example.azm_gymtracker.data.repository.MemberRepositoryImpl
import com.example.azm_gymtracker.data.repository.WorkoutRepositoryImpl
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository

/**
 * Manual dependency injection container for the AZM application.
 *
 * This avoids introducing Hilt/Dagger as an external dependency in the MVP while still
 * maintaining testability via [MemberRepository], [ExerciseRepository], and
 * [WorkoutRepository] interfaces.
 *
 * ViewModels access this container via [AzmApplication.container].
 */
class AppContainer(application: Application) {

    private val database: AZMDatabase = AZMDatabase.getInstance(application)

    val memberRepository: MemberRepository = MemberRepositoryImpl(database.memberDao())
    val exerciseRepository: ExerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())
    val workoutRepository: WorkoutRepository = WorkoutRepositoryImpl(database.workoutDao())
}

/**
 * Application class that creates and holds the [AppContainer] singleton.
 */
class AzmApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
