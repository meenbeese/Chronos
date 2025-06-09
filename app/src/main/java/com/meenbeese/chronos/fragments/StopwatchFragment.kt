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
import android.widget.LinearLayout

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.google.android.material.textview.MaterialTextView
import com.meenbeese.chronos.R
import com.meenbeese.chronos.databinding.FragmentStopwatchBinding
import com.meenbeese.chronos.services.StopwatchService
import com.meenbeese.chronos.utils.FormatUtils.formatMillis
import com.meenbeese.chronos.views.ProgressTextView

class StopwatchFragment : BaseFragment(), StopwatchService.Listener, ServiceConnection {
    private var _binding: FragmentStopwatchBinding? = null
    private val binding get() = _binding!!

    private var service: StopwatchService? = null

    private var timeText by mutableStateOf("0s 00")
    private var currentProgress by mutableStateOf(0f)
    private var maxProgress by mutableStateOf(0f)
    private var referenceProgress by mutableStateOf<Float?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStopwatchBinding.inflate(inflater, container, false)

        binding.apply {
            reset.setOnClickListener { service?.reset() }
            reset.isClickable = false
            toggle.setOnClickListener { service?.toggle() }
            lap.setOnClickListener { service?.lap() }
            share.setOnClickListener {
                service?.let {
                    val time = formatMillis(it.elapsedTime)
                    val content = StringBuilder().append(getString(R.string.title_time, time)).append("\n")
                    var total: Long = 0
                    val laps = it.laps
                    for (i in laps!!.indices) {
                        val lapTime = laps[i]
                        total += lapTime
                        content.append(getString(R.string.title_lap_number, laps.size - i))
                            .append("    \t")
                            .append(getString(R.string.title_lap_time, formatMillis(lapTime)))
                            .append("    \t")
                            .append(getString(R.string.title_total_time, formatMillis(total)))
                        if (i < laps.size - 1) content.append("\n")
                    }
                    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            getString(R.string.title_stopwatch_share, getString(R.string.app_name), time)
                        )
                        putExtra(Intent.EXTRA_TEXT, content.toString())
                    }
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_share_results)))
                }
            }
            back.setOnClickListener { parentFragmentManager.popBackStack() }
            time.setContent {
                ProgressTextView(
                    text = timeText,
                    progress = currentProgress,
                    maxProgress = maxProgress,
                    referenceProgress = referenceProgress,
                    animate = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }

        val intent = Intent(context, StopwatchService::class.java)
        context?.startService(intent)
        context?.bindService(intent, this, Context.BIND_AUTO_CREATE)

        return binding.root
    }

    override fun onDestroyView() {
        service?.let {
            it.setListener(null)
            val isRunning = it.isRunning
            context?.unbindService(this)
            if (!isRunning) {
                context?.stopService(Intent(context, StopwatchService::class.java))
            }
        }
        _binding = null
        super.onDestroyView()
    }

    override fun onStateChanged(isRunning: Boolean) {
        if (isRunning) {
            configureRunningState()
        } else {
            configureStoppedState()
        }
    }

    private fun configureRunningState() {
        binding.reset.isClickable = false
        binding.reset.animate()?.alpha(0f)?.start()
        binding.lap.visibility = View.VISIBLE
        binding.share.visibility = View.GONE
        setToggleDrawable(R.drawable.ic_play_to_pause, R.drawable.ic_pause)
    }

    private fun configureStoppedState() {
        service?.let {
            if (it.elapsedTime > 0) {
                binding.reset.isClickable = true
                binding.reset.animate()?.alpha(1f)?.start()
                binding.share.visibility = View.VISIBLE
            } else {
                binding.share.visibility = View.INVISIBLE
            }
        }
        binding.lap.visibility = View.GONE
        setToggleDrawable(R.drawable.ic_pause_to_play, R.drawable.ic_play)
    }

    private fun setToggleDrawable(drawableId: Int, fallbackId: Int) {
        val drawable = AnimatedVectorDrawableCompat.create(requireContext(), drawableId)
        if (drawable != null) {
            binding.toggle.setImageDrawable(drawable)
            drawable.start()
        } else {
            binding.toggle.setImageResource(fallbackId)
        }
    }

    override fun onReset() {
        binding.laps.removeAllViews()
        maxProgress = 0f
        referenceProgress = 0f
        timeText = "0s 00"
        currentProgress = 0f

        binding.reset.isClickable = false
        binding.reset.alpha = 0f
        binding.lap.visibility = View.INVISIBLE
        binding.share.visibility = View.GONE
    }

    override fun onTick(currentTime: Long, text: String) {
        service?.let {
            timeText = text
            val lapBase = if (it.lastLapTime == 0L) currentTime else it.lastLapTime
            currentProgress = (currentTime - lapBase).toFloat()
        }
    }

    override fun onLap(lapNum: Int, lapTime: Long, lastLapTime: Long, lapDiff: Long) {
        if (lastLapTime == 0L)
            maxProgress = lapDiff.toFloat()
        else
            referenceProgress = lapDiff.toFloat()

        val layout = LinearLayout(context)
        val number = MaterialTextView(requireContext())
        number.text = getString(R.string.title_lap_number, lapNum)
        number.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
        layout.addView(number)

        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.weight = 1f

        val lap = MaterialTextView(requireContext())
        lap.layoutParams = layoutParams
        lap.gravity = GravityCompat.END
        lap.text = getString(R.string.title_lap_time, formatMillis(lapDiff))
        lap.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
        layout.addView(lap)

        val total = MaterialTextView(requireContext())
        total.layoutParams = layoutParams
        total.gravity = GravityCompat.END
        total.text = getString(R.string.title_total_time, formatMillis(lapTime))
        total.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
        layout.addView(total)

        binding.laps.addView(layout, 0)
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
