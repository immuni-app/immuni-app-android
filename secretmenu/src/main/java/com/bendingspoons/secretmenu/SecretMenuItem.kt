package com.bendingspoons.secretmenu

import android.content.Context

abstract class SecretMenuItem(
    val title: String,
    val action: (context: Context, config: SecretMenuConfiguration) -> Unit
)
