package org.immuni.android.ui.ble

import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bendingspoons.base.extensions.invisible
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.extensions.visible
import de.fraunhofer.iis.DistanceEstimate
import kotlinx.android.synthetic.main.ble_debug_fragment.*
import org.immuni.android.R
import org.koin.androidx.viewmodel.ext.android.getViewModel

class BleDebugFragment : Fragment() {

    companion object {
        fun newInstance() = BleDebugFragment()
    }

    private lateinit var viewModel: BleDebugViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        return inflater.inflate(R.layout.ble_debug_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getViewModel()

        with(list) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = ListAdapter()
        }

        viewModel.distances.observe(viewLifecycleOwner, Observer {
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
    }

    inner class ListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var items = listOf<DistanceEstimate>()

        inner class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
            val transmitter: TextView = v.findViewById(R.id.transmitter)
            val receiver: TextView = v.findViewById(R.id.receiver)
            val distance: TextView = v.findViewById(R.id.distance)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.ble_debug_item, parent, false)
            return ItemVH(v)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as? ItemVH)?.apply {
                transmitter.text = items[position].deviceId1
                receiver.text = items[position].deviceId2
                distance.text = "%.1f".format(items[position].distance)

                holder.itemView.background = when {
                    items[position].distance > 3 -> Color.GREEN.toDrawable()
                    items[position].distance > 1.5 && items[position].distance < 3 -> Color.YELLOW.toDrawable()
                    else -> Color.RED.toDrawable()
                }
            }
        }

    }
}
