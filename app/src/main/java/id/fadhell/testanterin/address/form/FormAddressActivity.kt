package id.fadhell.testanterin.address.form

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import id.fadhell.testanterin.R
import id.fadhell.testanterin.base.BaseActivity
import  id.fadhell.testanterin.database.AddressDbManager
import id.fadhell.testanterin.utils.AddressConstant.ADDRESS
import id.fadhell.testanterin.utils.AddressConstant.COORDINATE
import id.fadhell.testanterin.utils.AddressConstant.DATA_ADDRESS
import id.fadhell.testanterin.utils.AddressConstant.DATA_COORDINATE
import id.fadhell.testanterin.utils.AddressConstant.DATA_DESCRIPTION
import id.fadhell.testanterin.utils.AddressConstant.DATA_ID
import id.fadhell.testanterin.utils.AddressConstant.DATA_NAME
import id.fadhell.testanterin.utils.AddressConstant.DESCRIPTION
import id.fadhell.testanterin.utils.AddressConstant.NAME
import id.fadhell.testanterin.address.list.ListAddressActivity
import kotlinx.android.synthetic.main.form_address_activity.*
import kotlinx.android.synthetic.main.secondary_toolbar.*

class FormAddressActivity : BaseActivity() {

    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_address_activity)
        init(savedInstanceState)
        initData(savedInstanceState)
        initListener(savedInstanceState)
    }

    override fun init(bundle: Bundle?) {
        validateUser()
        textTitleToolbar.text = getString(R.string.edit_address_title)
    }

    override fun initData(bundle: Bundle?) {
    }

    override fun initListener(bundle: Bundle?) {
        buttonSave.setOnClickListener {
            saveData()
        }
        buttonBack.setOnClickListener { onBackPressed() }
    }

    private fun validateUser() {
        val intent = intent.extras

        if (intent != null) {
            val bundle: Bundle = intent
            id = bundle.getInt(DATA_ID, 0)

            editCoordinate.setText(intent.getString(DATA_COORDINATE))

            if (id != 0) {
                editName.setText(bundle.getString(DATA_NAME))
                editAddress.setText(bundle.getString(DATA_ADDRESS))
                editCoordinate.setText(bundle.getString(DATA_COORDINATE))
                editDescription.setText(bundle.getString(DATA_DESCRIPTION))
            }
        }
    }

    private fun saveData() {
        val dbManager = AddressDbManager(this)

        val values = ContentValues()
        values.put(NAME, editName.text.toString())
        values.put(ADDRESS, editAddress.text.toString())
        values.put(DESCRIPTION, editDescription.text.toString())
        values.put(COORDINATE, editCoordinate.text.toString())

        if (id == 0) {
            val id = dbManager.insert(values)

            if (id > 0) {
                Toast.makeText(this, R.string.success_add_address, Toast.LENGTH_LONG).show()
                goToListPage()
            } else {
                Toast.makeText(this, R.string.fail_add_address, Toast.LENGTH_LONG).show()
            }
        } else {
            val selectionArs = arrayOf(id.toString())
            val id = dbManager.update(values, "Id=?", selectionArs)

            if (id > 0) {
                Toast.makeText(this, R.string.success_update_address, Toast.LENGTH_LONG).show()
                goToListPage()
            } else {
                Toast.makeText(this, R.string.fail_update_address, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToListPage() {
        val intent = Intent(this, ListAddressActivity::class.java)
        startActivity(intent)
        finish()
    }
}
