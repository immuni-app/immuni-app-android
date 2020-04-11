package org.immuni.android.managers

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.Fragment
import org.immuni.android.R
import org.immuni.android.toast
import org.immuni.android.util.log
import org.immuni.android.workers.Actions
import org.immuni.android.workers.ImmuniForegroundService
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

    fun scheduleBLEWorker(appContext: Context) {

        // check if the hardware support BLE
        if(!isBluetoothSupported()) {
            toast(context.getString(R.string.ble_not_supported_by_this_device))
            return
        }

        /*
        val workManager = WorkManager.getInstance(appContext)
        val notificationWork = OneTimeWorkRequestBuilder<BLEForegroundServiceWorker>()
        workManager.beginUniqueWork(BLEForegroundServiceWorker.TAG, ExistingWorkPolicy.REPLACE, notificationWork.build()).enqueue()
         */

        Intent(appContext, ImmuniForegroundService::class.java).also {
            it.action = Actions.START.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                appContext.startForegroundService(it)
                return
            }
            log("Starting the service in < 26 Mode")
            appContext.startService(it)
        }
    }

    companion object {
        const val TAG = "BluetoothManager"
        const val REQUEST_ENABLE_BT = 978
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
}
