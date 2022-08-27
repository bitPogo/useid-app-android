package de.digitalService.useID.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.digitalService.useID.StorageManager
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.pinstorage.PinStorage
import de.digitalService.useID.pinstorage.PinStorageContract
import de.digitalService.useID.pinstorage.EncryptedSharedPreferencesFactory
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideIDCardManager() = IDCardManager()
}

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {
    @Provides
    fun provideViewModelCoroutineScope(): CoroutineScope? = null
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonBindingModule {
    @Binds
    abstract fun bindAppCoordinator(appCoordinator: AppCoordinator): AppCoordinatorType

    @Binds
    abstract fun bindCoroutineContextProvider(coroutineContextProvider: CoroutineContextProvider): CoroutineContextProviderType

    @Binds
    abstract fun bindStorageManager(storageManager: StorageManager): StorageManagerType
}

@Module
@InstallIn(SingletonComponent::class)
object PinProvider {
    @Singleton
    @Provides
    fun provideEncryptedSharedPreferencesFactory(): PinStorageContract.EncryptedSharedPreferencesFactory {
        return EncryptedSharedPreferencesFactory
    }

    @Singleton
    @Provides
    fun providePinStorage(
        @ApplicationContext context: Context,
        encryptedSharedPreferencesFactory: PinStorageContract.EncryptedSharedPreferencesFactory
    ): PinStorageContract.PinStorage {
        return PinStorage.PinStorageFactory(
            encryptedSharedPreferencesFactory
        ).getInstance(context)
    }
}
