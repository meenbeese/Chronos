package com.meenbeese.chronos.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.view.GravityCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.meenbeese.chronos.R
import com.meenbeese.chronos.services.StopwatchService
import com.meenbeese.chronos.utils.FormatUtils.formatMillis
import com.meenbeese.chronos.views.ProgressTextView
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.google.android.material.floatingactionbutton.FloatingActionButton

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


class StopwatchFragment : BaseFragment(), StopwatchService.Listener, ServiceConnection {
    private lateinit var back: ImageView
    private lateinit var reset: ImageView
    private lateinit var share: ImageView
    private lateinit var lap: TextView
    private lateinit var toggle: FloatingActionButton
    private lateinit var time: ProgressTextView
    private lateinit var lapsLayout: LinearLayout
    private var textColorPrimary = 0
    private var textColorPrimarySubscription: Disposable? = null
    private val disposables = CompositeDisposable()
    private var service: StopwatchService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stopwatch, container, false)
        back = view.findViewById(R.id.back)
        reset = view.findViewById(R.id.reset)
        share = view.findViewById(R.id.share)
        lap = view.findViewById(R.id.lap)
        toggle = view.findViewById(R.id.toggle)
        time = view.findViewById(R.id.time)
        lapsLayout = view.findViewById(R.id.laps)
        reset.setOnClickListener { service?.reset() }
        reset.isClickable = false
        toggle.setOnClickListener { service?.toggle() }
        lap.setOnClickListener { service?.lap() }
        share.setOnClickListener {
            service?.let {
                val time = formatMillis(it.elapsedTime)
                val content = StringBuilder().append(requireContext().getString(R.string.title_time, time)).append("\n")
                var total: Long = 0
                val laps = it.laps
                for (i in laps!!.indices) {
                    val lapTime = laps[i]
                    total += lapTime
                    content.append(context?.getString(R.string.title_lap_number, laps.size - i))
                        .append("    \t")
                        .append(context?.getString(R.string.title_lap_time, formatMillis(lapTime)))
                        .append("    \t")
                        .append(context?.getString(R.string.title_total_time, formatMillis(total)))
                    if (i < laps.size - 1) content.append("\n")
                }
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.setType("text/plain")
                sharingIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    context?.getString(
                        R.string.title_stopwatch_share,
                        context?.getString(R.string.app_name),
                        time
                    )
                )
                sharingIntent.putExtra(Intent.EXTRA_TEXT, content.toString())
                startActivity(
                    Intent.createChooser(
                        sharingIntent,
                        context?.getString(R.string.title_share_results)
                    )
                )
            }
        }
        back.setOnClickListener { parentFragmentManager.popBackStack() }
        textColorPrimarySubscription = get()
            .textColorPrimary()
            .subscribeBy(
                onNext = { integer: Int ->
                    textColorPrimary = integer
                    back.setColorFilter(integer)
                    reset.setColorFilter(integer)
                    lap.setTextColor(integer)
                    share.setColorFilter(integer)
                    for (i in 0 until lapsLayout.childCount) {
                        val layout = lapsLayout.getChildAt(i) as LinearLayout
                        for (i2 in 0 until layout.childCount) {
                            (layout.getChildAt(i2) as TextView).setTextColor(integer)
                        }
                    }
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }
        val intent = Intent(context, StopwatchService::class.java)
        context?.startService(intent)
        context?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        return view
    }

    override fun onDestroyView() {
        disposables.dispose()
        time.unsubscribe()
        service?.let {
            it.setListener(null)
            val isRunning = it.isRunning
            context?.unbindService(this)
            if (!isRunning) context?.stopService(Intent(context, StopwatchService::class.java))
        }
        super.onDestroyView()
    }

    override fun onStateChanged(isRunning: Boolean) {
        if (isRunning) {
            reset.isClickable = false
            reset.animate()?.alpha(0f)?.start()
            lap.visibility = View.VISIBLE
            share.visibility = View.GONE
            val drawable =
                AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.ic_play_to_pause)
            if (drawable != null) {
                toggle.setImageDrawable(drawable)
                drawable.start()
            } else toggle.setImageResource(R.drawable.ic_pause)
        } else {
            if (service!!.elapsedTime > 0) {
                reset.isClickable = true
                reset.animate()?.alpha(1f)?.start()
                share.visibility = View.VISIBLE
            } else share.visibility = View.INVISIBLE
            lap.visibility = View.GONE
            val drawable =
                AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.ic_pause_to_play)
            if (drawable != null) {
                toggle.setImageDrawable(drawable)
                drawable.start()
            } else toggle.setImageResource(R.drawable.ic_play)
        }
    }

    override fun onReset() {
        lapsLayout.removeAllViews()
        time.setMaxProgress(0)
        time.setReferenceProgress(0)
        reset.isClickable = false
        reset.alpha = 0f
        lap.visibility = View.INVISIBLE
        share.visibility = View.GONE
    }

    override fun onTick(currentTime: Long, text: String) {
        service?.let {
            time.setText(text)
            time.setProgress(currentTime - if (it.lastLapTime == 0L) currentTime else it.lastLapTime)
        }
    }

    override fun onLap(lapNum: Int, lapTime: Long, lastLapTime: Long, lapDiff: Long) {
        if (lastLapTime == 0L) time.setMaxProgress(lapDiff) else time.setReferenceProgress(
            lapDiff
        )
        val layout = LinearLayout(context)
        val number = TextView(context)
        number.text = getString(R.string.title_lap_number, lapNum)
        number.setTextColor(textColorPrimary)
        layout.addView(number)
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.weight = 1f
        val lap = TextView(context)
        lap.layoutParams = layoutParams
        lap.gravity = GravityCompat.END
        lap.text = getString(R.string.title_lap_time, formatMillis(lapDiff))
        lap.setTextColor(textColorPrimary)
        layout.addView(lap)
        val total = TextView(context)
        total.layoutParams = layoutParams
        total.gravity = GravityCompat.END
        total.text = getString(R.string.title_total_time, formatMillis(lapTime))
        total.setTextColor(textColorPrimary)
        layout.addView(total)
        lapsLayout.addView(layout, 0)
    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        if (iBinder is StopwatchService.LocalBinder) {
            service = iBinder.service
            service?.isRunning?.let { onStateChanged(it) }
            onTick(0, "0s 00")
            service?.setListener(this)
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        service = null
    }
}
