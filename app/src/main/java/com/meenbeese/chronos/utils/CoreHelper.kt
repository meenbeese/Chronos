package com.meenbeese.chronos.utils

import android.content.Context

object CoreHelper {
    var contextGetter: (() -> Context)? = null
}
