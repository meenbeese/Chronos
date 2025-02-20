package com.meenbeese.chronos.adapters

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.dialogs.SoundChooserDialog
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.utils.DimenUtils
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.views.DaySwitch
import com.meenbeese.chronos.views.ProgressLineView

import java.util.Calendar
import java.util.concurrent.TimeUnit


/**
 * View adapter for the "alarms" list; displays all timers and
 * alarms currently stored in the application.
 */
class AlarmsAdapter(
    private val chronos: Chronos,
    private val recycler: RecyclerView,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val alarmManager: AlarmManager? = chronos.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
    private val timers: List<TimerData> = chronos.timers
    private val alarms: List<AlarmData> = chronos.alarms

    private var expandedPosition = -1

    var colorAccent = Color.WHITE
        set(colorAccent) {
            field = colorAccent
            recycler.post { notifyDataSetChanged() }
        }

    var colorForeground = Color.TRANSPARENT
        set(colorForeground) {
            field = colorForeground
            if (expandedPosition > 0) {
                recycler.post { notifyItemChanged(expandedPosition) }
            }
        }

    var textColorPrimary = Color.WHITE
        set(textColorPrimary) {
            field = textColorPrimary
            recycler.post { notifyDataSetChanged() }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            TimerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_timer, parent, false))
        } else {
            AlarmViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false), chronos)
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

        holder.stop.setColorFilter(textColorPrimary)
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

            alarm.setDays(chronos, alarm.days)

            val transition = AutoTransition()
            transition.duration = 150
            TransitionManager.beginDelayedTransition(recycler, transition)

            recycler.post { notifyDataSetChanged() }
        }

        holder.days.visibility = if (alarm.isRepeat()) View.VISIBLE else View.GONE

        val listener : DaySwitch.OnCheckedChangeListener = object : DaySwitch.OnCheckedChangeListener {
            override fun onCheckedChanged(daySwitch: DaySwitch, isChecked: Boolean) {
                alarm.days[holder.days.indexOfChild(daySwitch)] = isChecked
                alarm.setDays(chronos, alarm.days)

                if (!alarm.isRepeat()) {
                    notifyItemChanged(holder.bindingAdapterPosition)
                } else {
                    // If the view isn't going to change size in the recycler,
                    // then I can just do this (prevents the background flickering as
                    // the recyclerview attempts to smooth the transition)
                    onBindAlarmViewHolder(holder, holder.bindingAdapterPosition)
                }
            }
        }

        for (i in 0..6) {
            val daySwitch = holder.days.getChildAt(i) as DaySwitch
            daySwitch.isChecked = alarm.days[i]
            daySwitch.onCheckedChangeListener = listener

            when (i) {
                0 -> daySwitch.setText(daySwitch.context.getString(R.string.day_sunday_abbr))
                1 -> daySwitch.setText(daySwitch.context.getString(R.string.day_monday_abbr))
                2 -> daySwitch.setText(daySwitch.context.getString(R.string.day_tuesday_abbr))
                3 -> daySwitch.setText(daySwitch.context.getString(R.string.day_wednesday_abbr))
                4 -> daySwitch.setText(daySwitch.context.getString(R.string.day_thursday_abbr))
                5 -> daySwitch.setText(daySwitch.context.getString(R.string.day_friday_abbr))
                6 -> daySwitch.setText(daySwitch.context.getString(R.string.day_saturday_abbr))
            }
        }
    }

    private fun onBindAlarmViewHolderToggles(holder: AlarmViewHolder, alarm: AlarmData) {
        holder.ringtoneImage.setImageResource(if (alarm.hasSound()) R.drawable.ic_ringtone else R.drawable.ic_ringtone_disabled)
        holder.ringtoneImage.alpha = if (alarm.hasSound()) 1f else 0.333f
        holder.ringtoneText.text = if (alarm.hasSound()) alarm.sound?.name else chronos.getString(R.string.title_sound_none)
        holder.ringtone.setOnClickListener {
            val dialog = SoundChooserDialog()
            dialog.setListener(object : SoundChooserListener {
                override fun onSoundChosen(sound: SoundData?) {
                    alarm.setSound(chronos, sound)
                    onBindAlarmViewHolderToggles(holder, alarm)
                }
            })
            dialog.show(fragmentManager, null)
        }

        val vibrateDrawable = AnimatedVectorDrawableCompat.create(chronos, if (alarm.isVibrate) R.drawable.ic_vibrate_to_none else R.drawable.ic_none_to_vibrate)
        holder.vibrateImage.setImageDrawable(vibrateDrawable)
        holder.vibrateImage.alpha = if (alarm.isVibrate) 1f else 0.333f
        holder.vibrate.setOnClickListener { view ->
            alarm.setVibrate(chronos, !alarm.isVibrate)

            val vibrateDrawable1 = AnimatedVectorDrawableCompat.create(chronos, if (alarm.isVibrate) R.drawable.ic_none_to_vibrate else R.drawable.ic_vibrate_to_none)
            if (vibrateDrawable1 != null) {
                holder.vibrateImage.setImageDrawable(vibrateDrawable1)
                vibrateDrawable1.start()
            } else
                holder.vibrateImage.setImageResource(if (alarm.isVibrate) R.drawable.ic_vibrate else R.drawable.ic_vibrate_none)

            holder.vibrateImage.animate().alpha(if (alarm.isVibrate) 1f else 0.333f).setDuration(250).start()
            if (alarm.isVibrate)
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    @SuppressLint("CheckResult")
    private fun onBindAlarmViewHolderExpansion(holder: AlarmViewHolder, position: Int) {
        val isExpanded = position == expandedPosition
        val visibility = if (isExpanded) View.VISIBLE else View.GONE

        if (visibility != holder.extra.visibility) {
            holder.extra.visibility = visibility

            ValueAnimator.ofFloat(
                if (isExpanded) 0f else DimenUtils.dpToPx(2f).toFloat(),
                if (isExpanded) DimenUtils.dpToPx(2f).toFloat() else 0f
            ).apply {
                addUpdateListener { animation ->
                    (animation.animatedValue as? Float)?.let { elevation ->
                        ViewCompat.setElevation(holder.itemView, elevation)
                    }
                }
                start()
            }
        } else {
            holder.itemView.setBackgroundColor(if (isExpanded) colorForeground else Color.TRANSPARENT)
            ViewCompat.setElevation(holder.itemView, (if (isExpanded) DimenUtils.dpToPx(2f) else 0).toFloat())
        }

        holder.itemView.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else holder.bindingAdapterPosition

            val transition = AutoTransition()
            transition.duration = 250
            TransitionManager.beginDelayedTransition(recycler, transition)

            recycler.post { notifyDataSetChanged() }
        }
    }

    private fun onBindAlarmViewHolder(holder: AlarmViewHolder, position: Int) {
        val isExpanded = position == expandedPosition

        val alarm = getAlarm(position) ?: return

        holder.name.isFocusableInTouchMode = isExpanded
        holder.name.isCursorVisible = false
        holder.name.clearFocus()
        holder.nameUnderline.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.name.setText(alarm.getName(chronos))

        if (isExpanded) holder.name.setOnClickListener(null)
        else holder.name.setOnClickListener { holder.itemView.callOnClick()}

        holder.name.setOnFocusChangeListener { _, hasFocus -> holder.name.isCursorVisible = hasFocus && holder.bindingAdapterPosition == expandedPosition }

        holder.enable.setOnCheckedChangeListener(null)
        holder.enable.isChecked = alarm.isEnabled
        holder.enable.setOnCheckedChangeListener { _, b ->
            alarm.setEnabled(chronos, b)

            val transition = AutoTransition()
            transition.duration = 200
            TransitionManager.beginDelayedTransition(recycler, transition)

            recycler.post { notifyDataSetChanged() }
        }

        holder.time.text = FormatUtils.formatShort(chronos, alarm.time.time)
        holder.time.setOnClickListener { view ->
            val hour = alarm.time.get(Calendar.HOUR_OF_DAY)
            val minute = alarm.time.get(Calendar.MINUTE)
            val style = if (chronos.isDarkTheme()) 0 else com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog

            val timePickerDialog = TimePickerDialog(view.context, style, { _, selectedHour, selectedMinute ->
                alarm.time.set(Calendar.HOUR_OF_DAY, selectedHour)
                alarm.time.set(Calendar.MINUTE, selectedMinute)
                alarm.setTime(chronos, alarm.time.timeInMillis)
                alarm.setEnabled(chronos, true)

                notifyItemChanged(holder.bindingAdapterPosition)
            }, hour, minute, true)

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
            holder.soundIndicator.alpha = if (alarm.hasSound()) 1f else 0.333f
            holder.vibrateIndicator.alpha = if (alarm.isVibrate) 1f else 0.333f
        }

        holder.expandImage.animate().rotationX((if (isExpanded) 180 else 0).toFloat()).start()
        holder.delete.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.delete.setOnClickListener { view ->
            MaterialAlertDialogBuilder(view.context, if(chronos.isDarkTheme()) com.google.android.material.R.style.Theme_MaterialComponents_Dialog_Alert else com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setMessage(chronos.getString(R.string.msg_delete_confirmation, alarm.getName(chronos)))
                .setPositiveButton(view.context.getString(android.R.string.ok)){_, _ ->  chronos.removeAlarm(alarm)}
                .setNegativeButton(view.context.getString(android.R.string.cancel), null)
                .show()
        }

        holder.repeat.setTextColor(textColorPrimary)
        holder.delete.setTextColor(textColorPrimary)
        holder.ringtoneImage.setColorFilter(textColorPrimary)
        holder.vibrateImage.setColorFilter(textColorPrimary)
        holder.expandImage.setColorFilter(textColorPrimary)
        holder.repeatIndicator.setColorFilter(textColorPrimary)
        holder.soundIndicator.setColorFilter(textColorPrimary)
        holder.vibrateIndicator.setColorFilter(textColorPrimary)
        holder.nameUnderline.setBackgroundColor(textColorPrimary)

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
    class TimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null
            set(runnable) {
                if (field != null)
                    handler.removeCallbacks(field!!)

                field = runnable
                handler.post(field!!)
            }

        val time: TextView = itemView.findViewById(R.id.time)
        val stop: ImageView = itemView.findViewById(R.id.stop)
        val progress: ProgressLineView = itemView.findViewById(R.id.progress)
    }

    /**
     * ViewHolder for alarm items.
     */
    class AlarmViewHolder(v: View, val chronos: Chronos) : RecyclerView.ViewHolder(v) {
        val name: EditText = v.findViewById(R.id.name)
        val nameUnderline: View = v.findViewById(R.id.underline)
        val enable: SwitchMaterial = v.findViewById(R.id.enable)
        val time: TextView = v.findViewById(R.id.time)
        val nextTime: TextView = v.findViewById(R.id.nextTime)
        val extra: View = v.findViewById(R.id.extra)
        val repeat: MaterialCheckBox = v.findViewById(R.id.repeat)
        val days: LinearLayout = v.findViewById(R.id.days)
        val ringtone: View = v.findViewById(R.id.ringtone)
        val ringtoneImage: ImageView = v.findViewById(R.id.ringtoneImage)
        val ringtoneText: TextView = v.findViewById(R.id.ringtoneText)
        val vibrate: View = v.findViewById(R.id.vibrate)
        val vibrateImage: ImageView = v.findViewById(R.id.vibrateImage)
        val expandImage: ImageView = v.findViewById(R.id.expandImage)
        val delete: TextView = v.findViewById(R.id.delete)
        val indicators: View = v.findViewById(R.id.indicators)
        val repeatIndicator: ImageView = v.findViewById(R.id.repeatIndicator)
        val soundIndicator: ImageView = v.findViewById(R.id.soundIndicator)
        val vibrateIndicator: ImageView = v.findViewById(R.id.vibrateIndicator)

        val alarms: List<AlarmData> = chronos.alarms

        init {
            name.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    // ignore
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    // ignore
                }

                override fun afterTextChanged(editable: Editable) {
                    if (bindingAdapterPosition < alarms.size) {
                        alarms[bindingAdapterPosition].setName(chronos, editable.toString())
                    }
                }
            })
        }
    }
}
