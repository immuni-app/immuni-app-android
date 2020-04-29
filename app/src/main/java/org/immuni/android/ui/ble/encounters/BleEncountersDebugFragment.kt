package org.immuni.android.ui.ble.encounters

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.immuni.android.extensions.view.invisible
import org.immuni.android.extensions.activity.setLightStatusBarFullscreen
import org.immuni.android.extensions.view.visible
import kotlinx.android.synthetic.main.ble_distance_debug_fragment.list
import kotlinx.android.synthetic.main.ble_distance_debug_fragment.noDevicesText
import kotlinx.android.synthetic.main.ble_encounters_debug_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.R
import org.immuni.android.managers.BtIdsManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class BleEncountersDebugFragment : Fragment() {

    companion object {
        fun newInstance() =
            BleEncountersDebugFragment()
    }

    private lateinit var viewModel: BleEncountersDebugViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        return inflater.inflate(R.layout.ble_encounters_debug_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getViewModel()

        with(list) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = ListAdapter()
        }

        viewModel.listModel.observe(viewLifecycleOwner, Observer {
            (list.adapter as ListAdapter).apply {
                items = it
                notifyDataSetChanged()
            }

            if(it.isEmpty()) {
                noDevicesText.visible()
            } else {
                noDevicesText.invisible()
            }
        })

        viewModel.lastEncounter.observe(viewLifecycleOwner, Observer {
            val latestEntry = it.enumeratedEvents.last()
            lastEncounter.text = "Last encounter data: txPower=${latestEntry.txPower} rssi=${latestEntry.rssi} btId=${it.btId}"
        })

        val btIdsManager: BtIdsManager by inject()
        GlobalScope.launch(Dispatchers.Main) {
            val btid = btIdsManager.getOrFetchActiveBtId()
            currentBtId.text = "My bt_id: ${btid.id ?: "-"}"
        }

    }

    inner class ListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var items = listOf<EncountersItem>()

        inner class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
            val time: TextView = v.findViewById(R.id.time)
            val counter: TextView = v.findViewById(R.id.counter)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.ble_encounters_debug_item, parent, false)
            return ItemVH(v)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as? ItemVH)?.apply {
                time.text = items[position].timeWindows
                counter.text = items[position].encounters.toString()
            }
        }

    }
}
