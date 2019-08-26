package id.fadhell.testanterin.address.form

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.yalantis.ucrop.UCrop
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
import id.fadhell.testanterin.utils.PhotoProvider
import id.fadhell.testanterin.utils.ImageUtils
import id.fadhell.testanterin.BuildConfig
import id.fadhell.testanterin.address.list.ListAddressActivity
import id.fadhell.testanterin.utils.AddressConstant.DATA_PHOTO
import id.fadhell.testanterin.utils.AddressConstant.PHOTO
import id.fadhell.testanterin.utils.loadImage
import kotlinx.android.synthetic.main.form_address_activity.*
import kotlinx.android.synthetic.main.secondary_toolbar.*
import java.io.File
import java.util.*

class FormAddressActivity : BaseActivity() {

    companion object {
        const val INTERVAL_TIME = 1000L
        const val REQ_CODE_CAMERA = 100
        const val REQ_CODE_GALLERY = 200
        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE : Int = 123
    }
    private var userChoosenTask: Int? = null
    lateinit var cameraTempUri: Uri
    private var imageBase64: String? = ""
    private var imageFile: File? = null
    private var uriString: String? = null
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
        validatePhoto()
    }

    override fun initData(bundle: Bundle?) {
    }

    override fun initListener(bundle: Bundle?) {
        buttonSave.setOnClickListener {
            saveData()
        }
        buttonBack.setOnClickListener { onBackPressed() }

        buttonImage.setOnClickListener {
            openCamera()
        }
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
                val uri = bundle.getString(DATA_PHOTO)
                imagePhoto.setImageURI(Uri.parse(uri))
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
        values.put(PHOTO, uriString)

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

    private fun openCamera() {
        val items = resources.getStringArray(R.array.choose_photo)
        val builder = AlertDialog.Builder(this)

        builder.setItems(items) { _, item ->
            if (item == 0) {
                cameraIntent()
            } else if (item == 1) {
                galleryIntent()
            }
        }
        builder.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>
                                            , grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userChoosenTask == REQ_CODE_CAMERA)
                    cameraIntent()
                else if (userChoosenTask == REQ_CODE_GALLERY)
                    galleryIntent()
            } else {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> {

                    val uri = PhotoProvider().getPhotoUri(data!!)
                    Thread(Runnable {
                        imageBase64 = ImageUtils.encodeImage(ImageUtils.convertUriToBitmap
                            (uri, this))
                        contentResolver.notifyChange(uri, null)
                    }).start()
                    uriString = uri.toString()
                    imageFile = File(uri.path)
                    imagePhoto.setImageURI(uri)
                    return
                }
            }
            when (userChoosenTask) {
                REQ_CODE_GALLERY -> {
                    val ucrop = data?.data?.let { ImageUtils.createCropActivity(this, it) }
                    ucrop?.start(this)
                }
                REQ_CODE_CAMERA -> {
                    val ucrop = ImageUtils.createCropActivity(this, cameraTempUri)
                    ucrop.start(this)
                }
            }
        }
    }

    private fun cameraIntent() {
        userChoosenTask = REQ_CODE_CAMERA
        val result = checkPermission(this)
        if (result) {

            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

            cameraTempUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val file = File(Environment.getExternalStorageDirectory(), "qasir")
                if (!file.exists()) {
                    file.mkdir()
                }
                val imageFile = File(file.path, Calendar.getInstance()
                    .timeInMillis.toString() + "" + ".jpg")
                FileProvider.getUriForFile(applicationContext,
                    BuildConfig.APPLICATION_ID + "" + ".provider", imageFile)
            } else {
                applicationContext?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            }

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri)
            startActivityForResult(intent, REQ_CODE_CAMERA)
        }
    }

    private fun galleryIntent() {
        userChoosenTask = REQ_CODE_GALLERY
        val result = checkPermission(this)
        if (result) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            val intent = Intent()
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Pilih Photo")
                , REQ_CODE_GALLERY)
        }
    }

    private fun goToListPage() {
        val intent = Intent(this, ListAddressActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validatePhoto() {
        if (imageFile == null) {
            buttonImage.visibility = View.VISIBLE
        } else {
            buttonImage.visibility = View.GONE
        }
    }

    private fun checkPermission(context: Context) : Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        var permissionsGranted  = false
        val currentApiVersion : Int = Build.VERSION.SDK_INT

        if(currentApiVersion >= Build.VERSION_CODES.M){
            for(permission in permissions){
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)){
                        ActivityCompat.requestPermissions(context,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                    }else{
                        ActivityCompat.requestPermissions(context,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                    }
                    permissionsGranted = false
                    break
                }else{
                    permissionsGranted = true
                }
            }
            return permissionsGranted
        }else{
            return true
        }
    }
}
