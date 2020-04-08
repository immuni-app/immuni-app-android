package com.bendingspoons.concierge

import com.bendingspoons.concierge.Concierge.*
import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.IOException
import java.util.*

interface ConciergeProvider {
    fun provideAndroidId(): Id?
    fun provideAAID(): Id?
    fun provideBackupPersistentId(): Id
    fun provideNonBackupPersistentId(): Id
}

internal class ConciergeProviderImpl(val context: Context) : ConciergeProvider {
    @SuppressLint("HardwareIds")
    private fun androidId(): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun provideAndroidId(): Id? {
        val androidId = androidId()

        return when (androidId) {
            null -> null
            else -> Id.Internal(
                InternalId.ANDROID_ID,
                androidId,
                CreationType.justGenerated
            )
        }
    }

    override fun provideAAID(): Id? {
        val aaid = getAAID()

        return when (aaid) {
            null -> null
            else -> Id.Internal(
                InternalId.AAID,
                aaid,
                CreationType.justGenerated
            )
        }
    }

    override fun provideBackupPersistentId(): Id {
        return Id.Internal(
            InternalId.BACKUP_PERSISTENT_ID,
            androidId() ?: provideAAID()?.id ?: UUID.randomUUID().toString(),
            CreationType.justGenerated
        )
    }

    override fun provideNonBackupPersistentId(): Id {
        return Id.Internal(
            InternalId.NON_BACKUP_PERSISTENT_ID,
            UUID.randomUUID().toString(),
            CreationType.justGenerated
        )
    }

    private fun getAAID(): String? {
        try {
            Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
        } catch (e: ClassNotFoundException) {
            // return null because google gms ads library is not linked
            return null
        }

        var adInfo: AdvertisingIdClient.Info? = null
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        } catch (e: IOException) {
            e.printStackTrace()
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
            // Encountered a recoverable error connecting to Google Play services.
        } catch (e: GooglePlayServicesNotAvailableException) {
            // Google Play services is not available entirely.
            e.printStackTrace()
        }

        val aaid = adInfo?.id
        val isLimitAdTrackingEnabled = adInfo?.isLimitAdTrackingEnabled

        return if (isLimitAdTrackingEnabled == false) aaid else null
    }
}