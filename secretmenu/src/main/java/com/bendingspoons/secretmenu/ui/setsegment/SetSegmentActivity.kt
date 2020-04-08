package com.bendingspoons.secretmenu.ui.setsegment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bendingspoons.base.extensions.EditTextDialogInterface
import com.bendingspoons.base.extensions.showAlert
import com.bendingspoons.base.extensions.showEditAlert
import com.bendingspoons.oracle.api.model.ForceExperimentRequest
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.oracle.api.model.RedeemGiftCodeRequest
import com.bendingspoons.oracle.api.toErrorResponse
import com.bendingspoons.secretmenu.R
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.secretmenu.ui.ExitActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class SetSegmentActivity : AppCompatActivity() {

    val config = SecretMenu.instance.config
    val oracle = SecretMenu.instance.oracle

    override fun onPause() {
        super.onPause()
        close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_activity)

        val experiments = oracle.settings()?.experimentsSegments

        if(experiments.isNullOrEmpty()) {
            val dialog = showAlert("Attention", "There is no experiment in the settings list.", "OK", DialogInterface.OnClickListener { _, _ -> close() })
            dialog.setOnDismissListener { close() }
            return
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Experiments")
            .setNegativeButton("Cancel") { _, _ -> close()}
            .setOnCancelListener { close() }
            .setSingleChoiceItems(
                experiments.keys.toTypedArray(),
                0
            ) { dialog, which ->
                dialog.dismiss()

                val experimentName = experiments.keys.toList().get(which)
                val experimentCurrentValue = experiments.values.toList().get(which)

                GlobalScope.launch(Dispatchers.Main) {
                    delay(600)
                    val editDialog = showEditAlert(
                        "$experimentName",
                        "Insert the new segment below",
                        experimentCurrentValue.toString(),
                        "CHANGE SEGMENT",
                        object : EditTextDialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int, text: String) {

                                GlobalScope.launch(Dispatchers.Main) {
                                    val result = oracle.api.forceSegment(
                                        ForceExperimentRequest(experimentName ?: "", text.toIntOrNull() ?: 0)
                                    )

                                    if (result.isSuccessful) {
                                        Toast.makeText(applicationContext, "Experiment segment changed.", Toast.LENGTH_SHORT).show()
                                        ExitActivity.exitApplication(SecretMenu.instance.context)
                                    } else {
                                        Toast.makeText(applicationContext, result.toErrorResponse()?.errorCode.toString(), Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    close()
                                }
                            }
                        },
                        getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ -> close() }
                    )

                    editDialog.setOnDismissListener { close() }
                }

            }
            .show();
    }

    private fun close() {
        try {
            this.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
