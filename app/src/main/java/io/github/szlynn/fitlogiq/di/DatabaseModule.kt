package io.github.szlynn.fitlogiq.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.szlynn.fitlogiq.data.db.FitLogIQDatabase
import io.github.szlynn.fitlogiq.data.db.RunDao
import io.github.szlynn.fitlogiq.data.db.TrainingPlanDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitLogIQDatabase =
        Room.databaseBuilder(
            context,
            FitLogIQDatabase::class.java,
            "fitlogiq.db",
        ).build()

    @Provides
    fun provideRunDao(db: FitLogIQDatabase): RunDao = db.runDao()

    @Provides
    fun provideTrainingPlanDao(db: FitLogIQDatabase): TrainingPlanDao = db.trainingPlanDao()
}
