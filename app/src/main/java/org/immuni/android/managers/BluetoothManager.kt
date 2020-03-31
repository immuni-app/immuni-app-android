package org.immuni.android.managers

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.AscoltoApplication
import org.immuni.android.R
import org.immuni.android.toast
import org.immuni.android.workers.BLEForegroundServiceWorker
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

    fun isBluetoothSupported(): Boolean {
        return !context.packageManager.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
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

        // check if the hardware support BLE
        if(!isBluetoothSupported()) {
            toast(context.getString(R.string.ble_not_supported_by_this_device))
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            val workManager = WorkManager.getInstance(AscoltoApplication.appContext)
            workManager.cancelAllWorkByTag(BLEForegroundServiceWorker.TAG)

            // let the previous worker stop before restarting it
            delay(2000)

            val notificationWork = OneTimeWorkRequestBuilder<BLEForegroundServiceWorker>().addTag(BLEForegroundServiceWorker.TAG)
            workManager.enqueue(notificationWork.build())
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT = 978
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
}
