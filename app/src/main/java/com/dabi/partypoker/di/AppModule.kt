package com.dabi.partypoker.di

import android.content.Context
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideConnectionsClient(
        @ApplicationContext context: Context
    ): ConnectionsClient = Nearby.getConnectionsClient(context)


    @Provides
    @Singleton
    fun providePlayerViewModel(
        connectionsClient: ConnectionsClient
    ): PlayerViewModel {
        return PlayerViewModel(connectionsClient)
    }


    @Provides
    @Singleton
    fun provideServerOwnerViewModel(
        connectionsClient: ConnectionsClient
    ): ServerOwnerViewModel {
        return ServerOwnerViewModel(connectionsClient)
    }

    @Provides
    @Singleton
    fun provideServerPlayerViewModel(
        connectionsClient: ConnectionsClient
    ): ServerPlayerViewModel {
        return ServerPlayerViewModel(connectionsClient)
    }
}