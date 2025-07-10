package com.meenbeese.chronos.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.AlarmsAdapter
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.databinding.FragmentRecyclerBinding
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.db.AlarmViewModelFactory
import com.meenbeese.chronos.interfaces.AlarmNavigator
import com.meenbeese.chronos.interfaces.ContextFragmentInstantiator

class AlarmsFragment : BasePagerFragment(), AlarmNavigator {
    private var _binding: FragmentRecyclerBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmsAdapter: AlarmsAdapter
    lateinit var recyclerView: RecyclerView
    private var scrolledToEndListener: OnScrolledToEndListener? = null
    private lateinit var alarmViewModel: AlarmViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecyclerBinding.inflate(inflater, container, false)

        val app = requireActivity().application as Chronos
        val factory = AlarmViewModelFactory(app.repository)
        alarmViewModel = ViewModelProvider(this, factory)[AlarmViewModel::class.java]

        recyclerView = binding.recycler
        binding.emptyText.setText(R.string.msg_alarms_empty)
        recyclerView.layoutManager = GridLayoutManager(context, 1)

        alarmsAdapter = AlarmsAdapter(
            chronos = chronos!!,
            recycler = recyclerView,
            onDeleteAlarm = { alarmViewModel.delete(it.toEntity()) },
            alarmViewModel = alarmViewModel
        )
        recyclerView.adapter = alarmsAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItem == totalItemCount - 1 && totalItemCount > 0) {
                    scrolledToEndListener?.onScrolledToEnd()
                }
            }
        })

        onChanged()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.title_alarms)
    }

    override fun onAlarmsChanged() {
        recyclerView.post {
            alarmsAdapter.updateAlarms(chronos!!.alarms)
            onChanged()
        }
    }

    override fun onTimersChanged() {
        recyclerView.post {
            alarmsAdapter.updateTimers(chronos!!.timers)
            onChanged()
        }
    }

    private fun onChanged() {
        binding.empty.visibility = if (alarmsAdapter.itemCount > 0) View.GONE else View.VISIBLE
    }

    override fun jumpToAlarm(alarmId: Int, openEditor: Boolean) {
        val position = alarmsAdapter.findPositionById(alarmId)
        if (position != -1) {
            recyclerView.scrollToPosition(position)

            if (openEditor) {
                alarmsAdapter.openEditorAt(position)
            }
        }
    }

    class Instantiator(context: Context?) : ContextFragmentInstantiator(context!!) {
        override fun getTitle(context: Context?, position: Int): String? {
            return context?.getString(R.string.title_alarms)
        }

        override fun newInstance(position: Int): BasePagerFragment {
            return AlarmsFragment()
        }
    }

    interface OnScrolledToEndListener {
        fun onScrolledToEnd()
    }
}
