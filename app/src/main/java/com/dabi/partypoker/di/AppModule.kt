package com.dabi.partypoker.di

import android.content.Context
import androidx.room.Room
import com.dabi.partypoker.featureClient.model.ClientBridge
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureMenu.viewModel.MenuViewModel
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.UiTextsAdapter
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule2 {

    @Provides
    @Singleton
    fun provideConnectionsClient(
        @ApplicationContext context: Context
    ): ConnectionsClient = Nearby.getConnectionsClient(context)


    @Provides
    @Singleton
    fun provideGameSettingsDatabase(
        @ApplicationContext context: Context
    ): GameSettingsDatabase = Room.databaseBuilder(
        context,
        GameSettingsDatabase::class.java,
        "game_settings.db"
    )
        .fallbackToDestructiveMigration()
        .build()
}

@Module
@InstallIn(ViewModelComponent::class)
class AppModule {
    @Provides
    @ViewModelScoped
    fun providePlayerViewModel(
        connectionsClient: ConnectionsClient
    ): PlayerViewModel {
        return PlayerViewModel(connectionsClient)
    }

    @Provides
    @ViewModelScoped
    fun provideMenuViewModel(
        db: GameSettingsDatabase
    ): MenuViewModel {
        return MenuViewModel(db)
    }

//    @Provides
//    @ViewModelScoped
//    fun provideServerOwnerViewModel(
//        connectionsClient: ConnectionsClient,
//        db: GameSettingsDatabase
//    ): ServerOwnerViewModel {
//        return ServerOwnerViewModel(connectionsClient, db, 0)
//    }

//    @Provides
//    @ViewModelScoped
//    fun provideServerPlayerViewModel(
//        connectionsClient: ConnectionsClient
//    ): ServerPlayerViewModel {
//        return ServerPlayerViewModel(connectionsClient)
//    }
}


@Module
@InstallIn(ActivityComponent::class)
object ViewModelModule {

    @Provides
    fun provideServerOwnerViewModelFactory(
        factory: ServerOwnerViewModel.ServerOwnerViewModelFactory
    ): ServerOwnerViewModelFactoryProvider {
        return ServerOwnerViewModelFactoryProvider(factory)
    }

    @Provides
    fun provideServerPlayerViewModelFactory(
        factory: ServerPlayerViewModel.ServerPlayerViewModelFactory
    ): ServerPlayerViewModelFactoryProvider {
        return ServerPlayerViewModelFactoryProvider(factory)
    }
}

class ServerOwnerViewModelFactoryProvider @Inject constructor(
    private val factory: ServerOwnerViewModel.ServerOwnerViewModelFactory
) {
    fun create(gameSettingsId: Long): ServerOwnerViewModel {
        return factory.create(gameSettingsId)
    }
}
class ServerPlayerViewModelFactoryProvider @Inject constructor(
    private val factory: ServerPlayerViewModel.ServerPlayerViewModelFactory
) {
    fun create(gameSettingsId: Long): ServerPlayerViewModel {
        return factory.create(gameSettingsId)
    }
}