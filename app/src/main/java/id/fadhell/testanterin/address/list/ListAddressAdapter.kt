package id.fadhell.testanterin.address.list

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.fadhell.testanterin.R
import id.fadhell.testanterin.database.DataAddress
import id.fadhell.testanterin.utils.OnItemClickListener

class ListAddressAdapter(private val dataList: ArrayList<DataAddress>,  private var context: Context? = null, private var listenerClick: OnItemClickListener? = null) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.address_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        holder.textTitle?.text = data.nameAddress
        holder.textAddress?.text = data.address

        holder.buttonEdit?.setOnClickListener {
            listenerClick?.onItemClicked(position, holder.buttonEdit)
        }
        holder.buttonDelete?.setOnClickListener {
            listenerClick?.onItemClick(position, holder.buttonDelete)
        }
    }
}

class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val textTitle: TextView? = view.findViewById(R.id.textName) as TextView
    val textAddress: TextView? = view.findViewById(R.id.textAddress) as TextView
//    val textDescription: TextView = view.findViewById(R.id.editDescription) as TextView
//    val textCoordinate: TextView = view.findViewById(R.id.editCoordinate) as TextView
    val buttonEdit: ImageButton? = view.findViewById(R.id.buttonEdit) as ImageButton
    val buttonDelete: ImageButton? = view.findViewById(R.id.buttonDelete) as ImageButton

}