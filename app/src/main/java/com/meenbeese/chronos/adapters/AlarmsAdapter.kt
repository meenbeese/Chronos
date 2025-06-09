package com.meenbeese.chronos.adapters

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast

import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.databinding.ItemAlarmBinding
import com.meenbeese.chronos.databinding.ItemTimerBinding
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.dialogs.SoundChooserDialog
import com.meenbeese.chronos.dialogs.TimePickerDialog
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.utils.AlarmsDiffCallback
import com.meenbeese.chronos.utils.DimenUtils
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.views.DayCircleView

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * View adapter for the "alarms" list; displays all timers and
 * alarms currently stored in the application.
 */
class AlarmsAdapter(
    private val chronos: Chronos,
    private val recycler: RecyclerView,
    private val fragmentManager: FragmentManager,
    private val onDeleteAlarm: (AlarmData) -> Unit,
    private val alarmViewModel: AlarmViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var timers: MutableList<TimerData> = chronos.timers.toMutableList()
    private var alarms: MutableList<AlarmData> = chronos.alarms.toMutableList()

    private var expandedPosition = -1

    var colorForeground = Color.TRANSPARENT
        set(colorForeground) {
            field = colorForeground
            if (expandedPosition > 0) {
                recycler.post { notifyItemChanged(expandedPosition) }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val binding = ItemTimerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TimerViewHolder(binding)
        } else {
            val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AlarmViewHolder(binding, chronos)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is AlarmViewHolder) {
            saveAlarmNameIfNeeded(holder)
        }
        super.onViewRecycled(holder)
    }

    private fun saveAlarmNameIfNeeded(holder: AlarmViewHolder) {
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val alarm = getAlarm(position)
            val newName = holder.name.text.toString()
            if (alarm != null && alarm.name != newName) {
                alarm.name = newName
                alarmViewModel.update(alarm.toEntity())
            }
        }
    }

    private fun onBindTimerViewHolder(holder: TimerViewHolder) {
        holder.runnable = object : Runnable {
            override fun run() {
                try {
                    getTimer(holder.bindingAdapterPosition)?.let { timer ->
                        val text = FormatUtils.formatMillis(timer.remainingMillis)
                        holder.time.text = text.substring(0, text.length - 3)
                        holder.progress.update(1 - timer.remainingMillis.toFloat() / timer.duration)
                    }

                    holder.handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        holder.stop.setOnClickListener {
            getTimer(holder.bindingAdapterPosition)?.let { timer ->
                chronos.removeTimer(timer)
            }
        }
    }

    private fun onBindAlarmViewHolderRepeat(holder: AlarmViewHolder, alarm: AlarmData) {
        holder.repeat.setOnCheckedChangeListener(null)
        holder.repeat.isChecked = alarm.isRepeat()
        holder.repeat.setOnCheckedChangeListener { _, b ->
            for (i in 0 until alarm.days.size) {
                alarm.days[i] = b
            }

            alarm.days = alarm.days
            alarmViewModel.update(alarm.toEntity())

            val transition = AutoTransition()
            transition.duration = 150
            TransitionManager.beginDelayedTransition(recycler, transition)

            recycler.post { notifyItemChanged(holder.bindingAdapterPosition) }
        }

        holder.days.visibility = if (alarm.isRepeat()) View.VISIBLE else View.GONE

        val dayLabels = listOf(
            R.string.day_sunday_abbr,
            R.string.day_monday_abbr,
            R.string.day_tuesday_abbr,
            R.string.day_wednesday_abbr,
            R.string.day_thursday_abbr,
            R.string.day_friday_abbr,
            R.string.day_saturday_abbr
        )

        for (i in 0..6) {
            val isChecked = alarm.days[i]
            val label = holder.days.context.getString(dayLabels[i])

            holder.dayComposeViews[i].setContent {
                DayCircleView(
                    text = label,
                    isChecked = isChecked,
                    onCheckedChange = { checked ->
                        alarm.days[i] = checked
                        alarm.days = alarm.days
                        alarmViewModel.update(alarm.toEntity())

                        if (!alarm.isRepeat()) {
                            notifyItemChanged(holder.bindingAdapterPosition)
                        } else {
                            onBindAlarmViewHolder(holder, holder.bindingAdapterPosition)
                        }
                    }
                )
            }
        }
    }

    private fun onBindAlarmViewHolderToggles(holder: AlarmViewHolder, alarm: AlarmData) {
        holder.ringtoneImage.setImageResource(if (alarm.sound != null) R.drawable.ic_ringtone else R.drawable.ic_ringtone_disabled)
        holder.ringtoneImage.alpha = if (alarm.sound != null) 1f else 0.333f
        holder.ringtoneText.text = if (alarm.sound != null) alarm.sound?.name else chronos.getString(R.string.title_sound_none)
        holder.ringtone.setOnClickListener {
            val dialog = SoundChooserDialog()
            dialog.setListener(object : SoundChooserListener {
                override fun onSoundChosen(sound: SoundData?) {
                    alarm.sound = sound
                    alarmViewModel.update(alarm.toEntity())
                    onBindAlarmViewHolderToggles(holder, alarm)
                }
            })
            dialog.show(fragmentManager, null)
        }

        val vibrateDrawable = AnimatedVectorDrawableCompat.create(chronos, if (alarm.isVibrate) R.drawable.ic_vibrate_to_none else R.drawable.ic_none_to_vibrate)
        holder.vibrateImage.setImageDrawable(vibrateDrawable)
        holder.vibrateImage.alpha = if (alarm.isVibrate) 1f else 0.333f
        holder.vibrate.setOnClickListener { view ->
            alarm.isVibrate = !alarm.isVibrate
            alarmViewModel.update(alarm.toEntity())

            val vibrateDrawable1 = AnimatedVectorDrawableCompat.create(chronos, if (alarm.isVibrate) R.drawable.ic_none_to_vibrate else R.drawable.ic_vibrate_to_none)
            if (vibrateDrawable1 != null) {
                holder.vibrateImage.setImageDrawable(vibrateDrawable1)
                vibrateDrawable1.start()
            } else {
                holder.vibrateImage.setImageResource(if (alarm.isVibrate) R.drawable.ic_vibrate else R.drawable.ic_vibrate_none)
            }

            holder.vibrateImage.animate().alpha(if (alarm.isVibrate) 1f else 0.333f).setDuration(250).start()
            if (alarm.isVibrate) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    private fun onBindAlarmViewHolderExpansion(holder: AlarmViewHolder, position: Int) {
        val isExpanded = position == expandedPosition

        val collapsedElevation = 0f
        val expandedElevation = DimenUtils.dpToPx(2f).toFloat()

        if (holder.extra.visibility != if (isExpanded) View.VISIBLE else View.GONE) {
            val transition = AutoTransition().apply {
                duration = 200
                ordering = TransitionSet.ORDERING_TOGETHER
            }

            TransitionManager.beginDelayedTransition(recycler, transition)

            holder.extra.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Animate elevation
            ValueAnimator.ofFloat(
                if (isExpanded) collapsedElevation else expandedElevation,
                if (isExpanded) expandedElevation else collapsedElevation
            ).apply {
                duration = 200
                addUpdateListener { animation ->
                    ViewCompat.setElevation(holder.itemView, animation.animatedValue as Float)
                }
                start()
            }

            // Animate background color
            ValueAnimator.ofArgb(
                if (isExpanded) Color.TRANSPARENT else colorForeground,
                if (isExpanded) colorForeground else Color.TRANSPARENT
            ).apply {
                duration = 200
                addUpdateListener { animation ->
                    holder.itemView.setBackgroundColor(animation.animatedValue as Int)
                }
                start()
            }
        }

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View?) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 200) return
                lastClickTime = currentTime

                if (expandedPosition != -1 && expandedPosition != holder.bindingAdapterPosition) {
                    val previousHolder = recycler.findViewHolderForAdapterPosition(expandedPosition)
                    if (previousHolder is AlarmViewHolder) {
                        saveAlarmNameIfNeeded(previousHolder)
                    }
                }

                val wasExpanded = position == expandedPosition
                expandedPosition = if (wasExpanded) -1 else position

                val transition = AutoTransition().apply {
                    duration = 200
                    ordering = TransitionSet.ORDERING_TOGETHER
                }
                TransitionManager.beginDelayedTransition(recycler, transition)

                if (wasExpanded) {
                    notifyItemChanged(position)
                } else {
                    val previousExpanded = expandedPosition
                    if (previousExpanded >= 0) {
                        notifyItemChanged(previousExpanded)
                    }
                    notifyItemChanged(position)
                }
            }
        })
    }

    private fun onBindAlarmViewHolder(holder: AlarmViewHolder, position: Int) {
        val isExpanded = position == expandedPosition
        val alarm = getAlarm(position) ?: return

        holder.name.isFocusableInTouchMode = isExpanded
        holder.name.isCursorVisible = false
        holder.name.clearFocus()
        holder.nameUnderline.visibility = if (isExpanded) View.VISIBLE else View.GONE

        if (holder.name.text.toString() != alarm.name) {
            holder.name.setText(alarm.name)
        }

        holder.expandImage.animate()
            .rotationX((if (isExpanded) 180 else 0).toFloat())
            .setDuration(200)
            .setInterpolator(DecelerateInterpolator())
            .start()

        holder.indicators.animate()
            .alpha(if (isExpanded) 0f else 1f)
            .setDuration(200)
            .withEndAction {
                holder.indicators.visibility = if (isExpanded) View.GONE else View.VISIBLE
            }
            .start()

        holder.delete.visibility = if (isExpanded) View.VISIBLE else View.GONE

        if (isExpanded) {
            holder.name.setOnClickListener(null)
        } else {
            holder.name.setOnClickListener { holder.itemView.callOnClick()}
        }

        holder.name.setOnFocusChangeListener { _, hasFocus ->
            holder.name.isCursorVisible = hasFocus && holder.bindingAdapterPosition == expandedPosition

            if (!hasFocus && holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                val updatedAlarm = getAlarm(holder.bindingAdapterPosition)
                if (updatedAlarm != null) {
                    updatedAlarm.name = holder.name.text.toString()
                    alarmViewModel.update(updatedAlarm.toEntity())
                }
            }
        }

        holder.enable.setOnCheckedChangeListener(null)
        holder.enable.isChecked = alarm.isEnabled
        holder.enable.setOnCheckedChangeListener { _, isChecked ->
            alarm.isEnabled = isChecked
            alarmViewModel.update(alarm.toEntity())

            val transition = AutoTransition()
            transition.duration = 200
            TransitionManager.beginDelayedTransition(recycler, transition)

            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(currentPosition)
            }
        }

        holder.time.text = FormatUtils.formatShort(chronos, alarm.time.time)
        holder.time.setOnClickListener { view ->
            val context = view.context
            val hour = alarm.time.get(Calendar.HOUR_OF_DAY)
            val minute = alarm.time.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                context = context,
                initialHour = hour,
                initialMinute = minute,
                is24HourClock = Preferences.MILITARY_TIME.get(context)
            ) { selectedHour, selectedMinute ->
                alarm.time.set(Calendar.HOUR_OF_DAY, selectedHour)
                alarm.time.set(Calendar.MINUTE, selectedMinute)
                alarm.time = Calendar.getInstance().apply { timeInMillis = alarm.time.timeInMillis }
                alarm.isEnabled = true

                alarmViewModel.update(alarm.toEntity())
                notifyItemChanged(holder.bindingAdapterPosition)
            }

            timePickerDialog.show()
        }

        holder.nextTime.visibility = if (alarm.isEnabled) View.VISIBLE else View.GONE

        val nextAlarm = alarm.getNext()
        if (alarm.isEnabled && nextAlarm != null) {
            // Minutes in a week: 10080
            // Maximum value of an integer: 2147483647
            // We do not need to check this int cast
            val minutes = TimeUnit.MILLISECONDS.toMinutes(nextAlarm.timeInMillis - Calendar.getInstance().timeInMillis).toInt()

            holder.nextTime.text = String.format(chronos.getString(R.string.title_alarm_next), FormatUtils.formatUnit(chronos, minutes))
        }

        holder.indicators.visibility = if (isExpanded) View.GONE else View.VISIBLE
        if (isExpanded) {
            onBindAlarmViewHolderRepeat(holder, alarm)
            onBindAlarmViewHolderToggles(holder, alarm)
        } else {
            holder.repeatIndicator.alpha = if (alarm.isRepeat()) 1f else 0.333f
            holder.soundIndicator.alpha = if (alarm.sound != null) 1f else 0.333f
            holder.vibrateIndicator.alpha = if (alarm.isVibrate) 1f else 0.333f
        }

        holder.expandImage.animate().rotationX((if (isExpanded) 180 else 0).toFloat()).start()
        holder.delete.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.delete.setOnClickListener { view ->
            MaterialAlertDialogBuilder(view.context, if (chronos.isDarkTheme()) com.google.android.material.R.style.Theme_MaterialComponents_Dialog_Alert else com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setMessage(chronos.getString(
                    R.string.msg_delete_confirmation,
                    alarm.name?.takeIf { it.isNotBlank() } ?: chronos.getString(R.string.default_alarm_label)
                ))
                .setPositiveButton(view.context.getString(android.R.string.ok)) { _, _ ->
                    val positionInAdapter = holder.bindingAdapterPosition
                    if (positionInAdapter != RecyclerView.NO_POSITION) {
                        alarms.remove(alarm)
                        chronos.alarms.remove(alarm)

                        notifyItemRemoved(positionInAdapter)

                        if (expandedPosition == positionInAdapter) {
                            expandedPosition = -1
                        } else if (expandedPosition > positionInAdapter) {
                            expandedPosition -= 1
                        }

                        onDeleteAlarm(alarm)

                        Toast.makeText(holder.chronos.applicationContext, "Alarm has been removed", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(view.context.getString(android.R.string.cancel), null)
                .show()
        }

        onBindAlarmViewHolderExpansion(holder, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 0 && holder is TimerViewHolder) {
            onBindTimerViewHolder(holder)
        } else if (holder is AlarmViewHolder) {
            onBindAlarmViewHolder(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < timers.size) 0 else 1
    }

    override fun getItemCount(): Int {
        return timers.size + alarms.size
    }

    fun updateTimers(newTimers: List<TimerData>) {
        val diffCallback = AlarmsDiffCallback(timers, newTimers, alarms, alarms)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        timers = newTimers.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateAlarms(newAlarms: List<AlarmData>) {
        val diffCallback = AlarmsDiffCallback(timers, timers, alarms, newAlarms)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        alarms = newAlarms.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun findPositionById(alarmId: Int): Int {
        return alarms.indexOfFirst { it.id == alarmId }
    }

    fun openEditorAt(position: Int) {
        val adapterPosition = position + timers.size

        if (adapterPosition < itemCount) {
            if (expandedPosition != -1 && expandedPosition != adapterPosition) {
                val previousHolder = recycler.findViewHolderForAdapterPosition(expandedPosition)
                if (previousHolder is AlarmViewHolder) {
                    saveAlarmNameIfNeeded(previousHolder)
                }
            }

            expandedPosition = adapterPosition

            recycler.post {
                if (expandedPosition != -1) notifyItemChanged(expandedPosition)
                notifyItemChanged(adapterPosition)
                recycler.scrollToPosition(adapterPosition)
            }
        }
    }

    /**
     * Returns the timer that should be bound to the
     * specified position in the list - null if there
     * is no timer to be bound.
     */
    private fun getTimer(position: Int): TimerData? {
        return if (position in (timers.indices))
            timers[position]
        else null
    }

    /**
     * Returns the alarm that should be bound to
     * the specified position in the list - null if
     * there is no alarm to be bound.
     */
    private fun getAlarm(position: Int): AlarmData? {
        val alarmPosition = position - timers.size

        return if (alarmPosition in (alarms.indices))
            alarms[alarmPosition]
        else null
    }

    /**
     * ViewHolder for timer items.
     */
    class TimerViewHolder(val binding: ItemTimerBinding) : RecyclerView.ViewHolder(binding.root) {
        val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null
            set(runnable) {
                if (field != null)
                    handler.removeCallbacks(field!!)
                field = runnable
                handler.post(field!!)
            }

        val time = binding.time
        val stop = binding.stop
        val progress = binding.progress
    }

    /**
     * ViewHolder for alarm items.
     */
    class AlarmViewHolder(val binding: ItemAlarmBinding, val chronos: Chronos) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.name
        val nameUnderline = binding.underline
        val enable = binding.enable
        val time = binding.time
        val nextTime = binding.nextTime
        val extra = binding.extra
        val repeat = binding.repeat
        val days = binding.days
        val ringtone = binding.ringtone
        val ringtoneImage = binding.ringtoneImage
        val ringtoneText = binding.ringtoneText
        val vibrate = binding.vibrate
        val vibrateImage = binding.vibrateImage
        val expandImage = binding.expandImage
        val delete = binding.delete
        val indicators = binding.indicators
        val repeatIndicator = binding.repeatIndicator
        val soundIndicator = binding.soundIndicator
        val vibrateIndicator = binding.vibrateIndicator

        val alarms: List<AlarmData> = chronos.alarms

        val dayComposeViews = listOf(
            binding.day1,
            binding.day2,
            binding.day3,
            binding.day0,
            binding.day4,
            binding.day5,
            binding.day6
        )
    }
}
