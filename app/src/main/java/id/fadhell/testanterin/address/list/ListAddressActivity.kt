package id.fadhell.testanterin.address.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import id.fadhell.testanterin.R
import id.fadhell.testanterin.address.form.FormAddressActivity
import id.fadhell.testanterin.base.BaseActivity
import id.fadhell.testanterin.database.AddressDbManager
import id.fadhell.testanterin.database.DataAddress
import id.fadhell.testanterin.maps.MapsActivity
import id.fadhell.testanterin.utils.AddressConstant.DATA_ADDRESS
import id.fadhell.testanterin.utils.AddressConstant.DATA_COORDINATE
import id.fadhell.testanterin.utils.AddressConstant.DATA_DESCRIPTION
import id.fadhell.testanterin.utils.AddressConstant.DATA_ID
import id.fadhell.testanterin.utils.AddressConstant.DATA_NAME
import id.fadhell.testanterin.utils.OnItemClickListener
import kotlinx.android.synthetic.main.list_address_activity.*
import kotlinx.android.synthetic.main.secondary_toolbar.*

class ListAddressActivity : BaseActivity() {

    private var adapter: ListAddressAdapter? = null
    private var listAddress = ArrayList<DataAddress>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_address_activity)
        init(savedInstanceState)
        initData(savedInstanceState)
        initListener(savedInstanceState)
    }

    override fun init(bundle: Bundle?) {
        textTitleToolbar.text = getString(R.string.list_address_title)

        adapter = ListAddressAdapter(listAddress, this , object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                updateAddress(listAddress[position])
            }

            override fun onItemClick(position: Int, view: View) {
                val dbManager = AddressDbManager(applicationContext)
                val selectionArgs = arrayOf(listAddress[position].id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
            }
        })
    }

    override fun initData(bundle: Bundle?) {
        loadQueryAll()
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack.setOnClickListener { onBackPressed() }
    }

    private fun loadQueryAll() {

        val dbManager = AddressDbManager(this)
        val cursor = dbManager.queryAll()

        listAddress.clear()
        if (cursor.moveToFirst()) {

            do {
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val nameAddress = cursor.getString(cursor.getColumnIndex("Name"))
                val address = cursor.getString(cursor.getColumnIndex("Address"))
                val description = cursor.getString(cursor.getColumnIndex("Description"))
                val coordinate = cursor.getString(cursor.getColumnIndex("Coordinate"))

                listAddress.add(DataAddress(id, nameAddress, address, description, coordinate))

            } while (cursor.moveToNext())
        }
        listAddressLayout.layoutManager = LinearLayoutManager(this)
        listAddressLayout.adapter = adapter
    }

    private fun updateAddress(data: DataAddress) {
        val intent = Intent(this, FormAddressActivity::class.java)
        intent.putExtra(DATA_ID, data.id)
        intent.putExtra(DATA_NAME, data.nameAddress)
        intent.putExtra(DATA_ADDRESS, data.address)
        intent.putExtra(DATA_DESCRIPTION, data.description)
        intent.putExtra(DATA_COORDINATE, data.coordinate)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
