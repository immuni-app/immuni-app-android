package com.bendingspoons.secretmenu.ui.redeemgiftcode

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bendingspoons.base.extensions.EditTextDialogInterface
import com.bendingspoons.oracle.api.model.RedeemGiftCodeRequest
import com.bendingspoons.oracle.api.toErrorResponse
import com.bendingspoons.secretmenu.R
import com.bendingspoons.secretmenu.SecretMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import com.bendingspoons.base.extensions.*
import kotlinx.coroutines.launch

class RedeemGiftCodeActivity : AppCompatActivity() {

    val config = SecretMenu.instance.config
    val oracle = SecretMenu.instance.oracle

    override fun onPause() {
        super.onPause()
        close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_activity)

        val dialog = showEditAlert(
            getString(R.string.redeem_gift_code),
            getString(R.string.redeem_gift_code_message),
            getString(R.string.gift_code),
            getString(R.string.redeem),
            object : EditTextDialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int, text: String) {

                    GlobalScope.launch(Dispatchers.Main) {
                        runCatching {
                            val result = oracle.api.redeemGiftCode(
                                RedeemGiftCodeRequest(text)
                            )

                            if (result.isSuccessful) {
                                oracle.api.fetchMe()
                            } else {
                                val errorMessage = when (result.toErrorResponse()?.errorCode) {
                                    820 -> getString(R.string.redeem_gift_code_error_820)
                                    821 -> getString(R.string.redeem_gift_code_error_821)
                                    822 -> getString(R.string.redeem_gift_code_error_822)
                                    823 -> getString(R.string.redeem_gift_code_error_823)
                                    else -> getString(R.string.generic_error)
                                }

                                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                            close()
                        }
                    }
                }
            },
            getString(R.string.cancel),
            DialogInterface.OnClickListener { _, _ -> close() }
        )

        dialog.setOnDismissListener { close() }
    }

    private fun close() {
        try {
            this.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
