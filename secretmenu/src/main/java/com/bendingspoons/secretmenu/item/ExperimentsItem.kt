package com.bendingspoons.secretmenu.item

import android.widget.Toast
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.secretmenu.SecretMenuItem
import java.lang.StringBuilder

class ExperimentsItem: SecretMenuItem(
    "\uD83D\uDCCA Experiments",
    { context, config ->

        val settings = SecretMenu.instance.oracle.settings()

        val experiments = StringBuilder("")
        settings?.experimentsSegments?.forEach {
            experiments.append("${it.key}: ${it.value}")
        }

        if(experiments.toString().isEmpty()) experiments.append("[]")

        Toast.makeText(context, experiments.toString(), Toast.LENGTH_LONG).show()
    }
)
