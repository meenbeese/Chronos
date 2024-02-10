package me.jfenn.alarmio.utils

import me.jfenn.alarmio.Alarmio

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException


object DebugUtils {
    private val SETUP_TASKS = arrayOf(
        "me.jfenn.alarmio.utils.LeakCanaryTask",
        "me.jfenn.alarmio.utils.CrasherTask"
    )

    /**
     * Set up any debug modules from the registered tasks. Should
     * be called inside the Application class's onCreate.
     *
     * @param alarmio An instance of the current application class.
     */
    @JvmStatic
    fun setup(alarmio: Alarmio?) {
        for (task in SETUP_TASKS) {
            try {
                val constructor = Class.forName(task).getConstructor() as Constructor<SetupTask>
                constructor.isAccessible = true
                constructor.newInstance().setup(alarmio)
                break
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface SetupTask {
        fun setup(alarmio: Alarmio?)
    }
}
