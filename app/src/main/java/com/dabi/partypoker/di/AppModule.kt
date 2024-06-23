package com.dabi.partypoker.di

import android.content.Context
import com.dabi.partypoker.featureClient.model.ClientBridge
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
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
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule2 {

    @Provides
    @Singleton
    fun provideConnectionsClient(
        @ApplicationContext context: Context
    ): ConnectionsClient = Nearby.getConnectionsClient(context)
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
    fun provideServerOwnerViewModel(
        connectionsClient: ConnectionsClient
    ): ServerOwnerViewModel {
        return ServerOwnerViewModel(connectionsClient)
    }
    @Provides
    @ViewModelScoped
    fun provideServerPlayerViewModel(
        connectionsClient: ConnectionsClient
    ): ServerPlayerViewModel {
        return ServerPlayerViewModel(connectionsClient)
    }
}