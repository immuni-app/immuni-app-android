package org.immuni.android.ui.onboarding.fragments.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.immuni.android.extensions.view.gone
import org.immuni.android.R
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.extensions.view.hideKeyboard
import org.immuni.android.extensions.view.visible
import kotlinx.android.synthetic.main.onboarding_permissions_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.BluetoothListenerLifecycle
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.GeolocalisationListenerLifecycle
import org.immuni.android.extensions.activity.toast
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class PermissionsFragment : FullScreenDialogDarkFragment() {

    private lateinit var viewModel: OnboardingViewModel
    val permissionsManager: PermissionsManager by inject()
    val bluetoothManager: BluetoothManager by inject()

    private lateinit var lifecycleBluetooth: BluetoothListenerLifecycle
    private lateinit var lifecycleGeolocation: GeolocalisationListenerLifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleBluetooth = BluetoothListenerLifecycle(requireContext(), this.lifecycle) { bluetoothState ->
            updateUI()
            checkAllGood()
        }
        lifecycleGeolocation = GeolocalisationListenerLifecycle(requireContext(), this.lifecycle) { active ->
            updateUI()
            checkAllGood()
        }
        lifecycle.addObserver(lifecycleBluetooth)
        lifecycle.addObserver(lifecycleGeolocation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.onboarding_permissions_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        checkAllGood()
        this.view?.hideKeyboard()
    }

    private fun checkAllGood() {
        if(btON() && permissionsON() && geolocationON() && whiteListON()) {
            viewModel.onOnboardingComplete()
        }
    }

    private fun checkBatteryOptimization() {
        val action = ProfileFragmentDirections.actionWhitelistDialog()
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        whitelist.setOnClickListener {
            //val action = ProfileFragmentDirections.actionWhitelistDialog()
            //findNavController().navigate(action)
            PermissionsManager.startChangeBatteryOptimization(requireContext())
        }

        bluetooth.setOnClickListener {
            if(!bluetoothManager.isBluetoothSupported()) {
                toast(
                    requireContext(),
                    requireContext().getString(
                        R.string.ble_not_supported_by_this_device
                    )
                )
                //viewModel.onNextTap()
                return@setOnClickListener
            }
            if(!bluetoothManager.isBluetoothEnabled()) {
                //val action = ProfileFragmentDirections.actionBluetoothDialog()
                //findNavController().navigate(action)
                bluetoothManager.openBluetoothSettings(this)
            } else updateUI()
        }

        geoPermissions.setOnClickListener {
            if(permissionsManager.shouldShowPermissions(activity as AppCompatActivity,
                    *permissionsManager.geolocationPermissions())) {
                val action = ProfileFragmentDirections.actionGlobalPermissionsTutorial()
                findNavController().navigate(action)
            } else {
                val action = ProfileFragmentDirections.actionGeoPermissionsDialog()
                findNavController().navigate(action)
            }
        }

        geolocation.setOnClickListener {
           
        }

        knowMore.setOnClickListener { updateKnowMore(it as TextView) }
        knowMore2.setOnClickListener { updateKnowMore(it as TextView) }
        knowMore3.setOnClickListener { updateKnowMore(it as TextView) }
        knowMore4.setOnClickListener { updateKnowMore(it as TextView) }

        /*
        bluetoothBox.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bluetoothBox2.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bluetoothBox3.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bluetoothBox4.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
         */

        viewModel.permissionsChanged.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        viewModel.onFinishPermissionsTutorial.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    permissionsManager.requestPermissions(activity as AppCompatActivity)
                }
            }
        })
    }

    private fun updateKnowMore(textView: TextView) {
        var more = false
        // update button text
        if(textView.text == getString(R.string.know_more)) {
            textView.text = getString(R.string.hide)
            more = true
        }
        else {
            textView.text = getString(R.string.know_more)
            more = false
        }

        // update content text
        when(textView.id) {
            R.id.knowMore -> {
                when(more) {
                    true -> description.text = getString(R.string.onboarding_permission_bt_short) + "\n\n" + getString(R.string.onboarding_permission_bt_long)
                    false -> description.text = getString(R.string.onboarding_permission_bt_short)
                }
            }
            R.id.knowMore2 -> {
                when(more) {
                    true -> description2.text =  getString(R.string.onboarding_permission_permissions_short) + "\n\n" + getString(R.string.onboarding_permission_permissions_long)
                    false -> description2.text = getString(R.string.onboarding_permission_permissions_short)
                }
            }
            R.id.knowMore3 -> {
                when(more) {
                    true -> description3.text =  getString(R.string.onboarding_permission_geo_short) + "\n\n" + getString(R.string.onboarding_permission_geo_long)
                    false -> description3.text = getString(R.string.onboarding_permission_geo_short)
                }
            }
            R.id.knowMore4 -> {
                when(more) {
                    true -> description4.text =  getString(R.string.onboarding_permission_whitelist_short) + "\n\n" + getString(R.string.onboarding_permission_whitelist_long)
                    false -> description4.text = getString(R.string.onboarding_permission_whitelist_short)
                }
            }
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
            updateUI()
        }
    }

    private fun btON(): Boolean {
        return bluetoothManager.isBluetoothSupported() && bluetoothManager.isBluetoothEnabled()
    }

    private fun permissionsON(): Boolean {
        return PermissionsManager.hasAllPermissions(requireContext())
    }

    private fun geolocationON(): Boolean {
        return PermissionsManager.globalLocalisationEnabled(requireContext())
    }

    private fun whiteListON(): Boolean {
        return PermissionsManager.isIgnoringBatteryOptimizations(requireContext())
    }

    private fun updateUI() {

        // BLUETOOTH
        if(btON()) {
            // SUCCESS
            description.gone()
            circle.setImageResource(R.drawable.ic_check_permissions)
            bluetooth.gone()
            bluetoothBox.alpha = 0.4f
            separator.visible()
            knowMore.gone()
            TextViewCompat.setTextAppearance(btTitle, R.style.H4Section)
        } else {
            // SHOW OPEN ALWAYS
            description.visible()
            bluetooth.visible()
            circle.setImageResource(R.drawable.ic_bluetooth)
            bluetoothBox.alpha = 1f
            separator.gone()
            knowMore.visible()
            TextViewCompat.setTextAppearance(btTitle, R.style.H3SectionHeading)
        }

        // PERMISSIONS
        if(permissionsON()) {
            // SUCCESS
            description2.gone()
            circle2.setImageResource(R.drawable.ic_check_permissions)
            geoPermissions.gone()
            bluetoothBox2.alpha = 0.4f
            separator2.visible()
            knowMore2.gone()
            TextViewCompat.setTextAppearance(permissionsTitle, R.style.H4Section)
        } else {
            // DISABLED
            if(!btON()) {
                description2.gone()
                geoPermissions.gone()
                circle2.setImageResource(R.drawable.ic_localization)
                bluetoothBox2.alpha = 0.4f
                separator2.visible()
                knowMore2.gone()
                TextViewCompat.setTextAppearance(permissionsTitle, R.style.H4Section)
            } else {
                // ACTIVE
                description2.visible()
                geoPermissions.visible()
                circle2.setImageResource(R.drawable.ic_localization)
                bluetoothBox2.alpha = 1f
                separator2.gone()
                knowMore2.visible()
                TextViewCompat.setTextAppearance(permissionsTitle, R.style.H3SectionHeading)
            }
        }

        // LOCALIZATION
        if(geolocationON()) {
            // SUCCESS
            description3.gone()
            circle3.setImageResource(R.drawable.ic_check_permissions)
            geolocation.gone()
            bluetoothBox3.alpha = 0.4f
            separator3.visible()
            knowMore3.gone()
            TextViewCompat.setTextAppearance(geoTitle, R.style.H4Section)
        } else {
            // DISABLED
            if(!permissionsON() || !btON()) {
                description3.gone()
                geolocation.gone()
                circle3.setImageResource(R.drawable.ic_localization)
                bluetoothBox3.alpha = 0.4f
                separator3.visible()
                knowMore3.gone()
                TextViewCompat.setTextAppearance(geoTitle, R.style.H4Section)
            } else {
                // ACTIVE
                description3.visible()
                geolocation.visible()
                circle3.setImageResource(R.drawable.ic_localization)
                bluetoothBox3.alpha = 1f
                separator3.gone()
                knowMore3.visible()
                TextViewCompat.setTextAppearance(geoTitle, R.style.H3SectionHeading)
            }
        }

        // WHITELIST
        if(whiteListON()) {
            // SUCCESS
            description4.gone()
            circle4.setImageResource(R.drawable.ic_check_permissions)
            whitelist.gone()
            bluetoothBox4.alpha = 0.4f
            knowMore4.gone()
            TextViewCompat.setTextAppearance(whitelistTitle, R.style.H4Section)
        } else {
            // DISABLED
            if(!geolocationON() || !btON() || !permissionsON()) {
                description4.gone()
                whitelist.gone()
                circle4.setImageResource(R.drawable.ic_localization)
                bluetoothBox4.alpha = 0.4f
                knowMore4.gone()
                TextViewCompat.setTextAppearance(whitelistTitle, R.style.H4Section)
            } else {
                // ACTIVE
                description4.visible()
                whitelist.visible()
                circle4.setImageResource(R.drawable.ic_localization)
                bluetoothBox4.alpha = 1f
                knowMore4.visible()
                TextViewCompat.setTextAppearance(whitelistTitle, R.style.H3SectionHeading)
            }
        }
    }
}
