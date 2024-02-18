package com.meenbeese.chronos.utils

import com.meenbeese.chronos.Chronos

import java.lang.reflect.Constructor


object DebugUtils {
    private val SETUP_TASKS = arrayOf(
        "com.meenbeese.chronos.utils.LeakCanaryTask",
        "com.meenbeese.chronos.utils.CrasherTask"
    )

    /**
     * Set up any debug modules from the registered tasks. Should
     * be called inside the Application class's onCreate.
     *
     * @param chronos An instance of the current application class.
     */
    @JvmStatic
    fun setup(chronos: Chronos?) {
        for (task in SETUP_TASKS) {
            try {
                val constructor = Class.forName(task).getConstructor() as Constructor<SetupTask>
                constructor.isAccessible = true
                constructor.newInstance().setup(chronos)
                break
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface SetupTask {
        fun setup(chronos: Chronos?)
    }
}
