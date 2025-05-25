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

import com.google.android.material.textview.MaterialTextView
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.AlarmsAdapter
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.db.AlarmViewModelFactory
import com.meenbeese.chronos.interfaces.ContextFragmentInstantiator

class AlarmsFragment : BasePagerFragment() {
    private lateinit var alarmsAdapter: AlarmsAdapter
    private var empty: View? = null
    lateinit var recyclerView: RecyclerView
    private var scrolledToEndListener: OnScrolledToEndListener? = null
    private lateinit var alarmViewModel: AlarmViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recycler, container, false)

        val app = requireActivity().application as Chronos
        val factory = AlarmViewModelFactory(app.repository)
        alarmViewModel = ViewModelProvider(this, factory)[AlarmViewModel::class.java]

        recyclerView = v.findViewById(R.id.recycler)
        empty = v.findViewById(R.id.empty)
        (v.findViewById<View>(R.id.emptyText) as MaterialTextView).setText(R.string.msg_alarms_empty)
        recyclerView.layoutManager = GridLayoutManager(context, 1)
        alarmsAdapter = AlarmsAdapter(chronos!!, recyclerView, parentFragmentManager) { alarmToDelete ->
            alarmViewModel.delete(alarmToDelete.toEntity())
        }
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

        return v
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
        empty?.visibility = if (alarmsAdapter.itemCount > 0) View.GONE else View.VISIBLE
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
