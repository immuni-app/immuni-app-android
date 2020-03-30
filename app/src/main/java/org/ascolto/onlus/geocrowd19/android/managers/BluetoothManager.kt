package org.ascolto.onlus.geocrowd19.android.managers

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.workers.BLEForegroundServiceWorker
import org.koin.core.KoinComponent

class BluetoothManager(val context: Context) : KoinComponent {
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    fun adapter(): BluetoothAdapter {
        return bluetoothAdapter
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled ?: false
    }

    fun openBluetoothSettings(fragment: Fragment, requestCode: Int = REQUEST_ENABLE_BT) {
        bluetoothAdapter.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            fragment.startActivityForResult(enableBtIntent, requestCode)
        }
    }

    fun openBluetoothSettings(activity: Activity, requestCode: Int = REQUEST_ENABLE_BT) {
        bluetoothAdapter.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, requestCode)
        }
    }

    fun scheduleBLEWorker() {
        val workManager = WorkManager.getInstance(AscoltoApplication.appContext)
        workManager.cancelAllWorkByTag(BLEForegroundServiceWorker.TAG)

        val notificationWork = OneTimeWorkRequestBuilder<BLEForegroundServiceWorker>().addTag(BLEForegroundServiceWorker.TAG)
        workManager.enqueue(notificationWork.build())
    }

    companion object {
        const val REQUEST_ENABLE_BT = 978
    }
}
