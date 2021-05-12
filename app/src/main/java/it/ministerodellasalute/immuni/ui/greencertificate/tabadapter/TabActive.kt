package it.ministerodellasalute.immuni.ui.greencertificate.tabadapter

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificate
import kotlinx.android.synthetic.main.green_certificate_active.*
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent

class TabActive : Fragment(R.layout.green_certificate_active), KoinComponent {

    private lateinit var userManager: UserManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userManager = get()

        val user = userManager.user.value

        if (userManager.user.value?.greenPass == null) {
            noQrCode.visibility = View.VISIBLE
            qrCode.visibility = View.GONE
        } else {
            qrCode.setImageBitmap(
                createQRCode(
                    userManager.user.value?.greenPass!!
                )
            )
            noQrCode.visibility = View.GONE
            qrCode.visibility = View.VISIBLE
        }
    }

    private fun createQRCode(item: GreenCertificate): Bitmap? {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            item.base45, BarcodeFormat.QR_CODE,
            ScreenUtils.convertPixelsToDp(requireContext(), 400F).toInt(),
            ScreenUtils.convertPixelsToDp(requireContext(), 400F).toInt()
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
