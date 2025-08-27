package com.meenbeese.chronos.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

import arrow.core.Either
import arrow.core.raise.either

import com.meenbeese.chronos.R

fun safeStartActivity(
    context: Context,
    intent: Intent
): Either<ActivityNotFoundException, Boolean> = either {
    try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.unable_start_activity, Toast.LENGTH_SHORT).show()
        raise(e)
    }
}
