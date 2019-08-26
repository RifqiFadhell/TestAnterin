package id.fadhell.testanterin.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.common.PositioningManager
import com.here.android.mpa.common.PositioningManager.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapState
import com.here.android.mpa.mapping.SupportMapFragment
import id.fadhell.testanterin.R
import id.fadhell.testanterin.address.form.FormAddressActivity
import id.fadhell.testanterin.address.list.ListAddressActivity
import id.fadhell.testanterin.base.BaseActivity
import id.fadhell.testanterin.utils.AddressConstant.DATA_COORDINATE
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.android.synthetic.main.maps_activity.*
import java.lang.ref.WeakReference
import java.util.*

class MapsActivity : BaseActivity(), OnPositionChangedListener, Map.OnTransformListener {

    private var map: Map? = null
    private lateinit var supportMapFragment: SupportMapFragment
    private val LOG_TAG = MapsActivity::class.java.simpleName
    private var positioningManager: PositioningManager? = null
    private var locationInfo: Runnable? = null
    private var paused: Boolean = false
    private var transforming: Boolean = false
    private var locationCoordinate: String? = null

    //Permissions
    private val REQUEST_CODE_ASK_PERMISSIONS = 1
    private val REQUIRED_SDK_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)
        init(savedInstanceState)
        initData(savedInstanceState)
        initListener(savedInstanceState)
    }

    override fun init(bundle: Bundle?) {
        checkPermissions()
    }

    override fun initData(bundle: Bundle?) {
    }

    override fun initListener(bundle: Bundle?) {
        buttonSaveLocation.setOnClickListener {
            saveLocation()
        }
        buttonMenu.setOnClickListener {
            goToListPage()
        }
    }

    private fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        // check all required dynamic permissions
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            val permissions = missingPermissions
                .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(
                REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                grantResults
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                for (index in permissions.indices.reversed()) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(
                            this, "Required permission '" + permissions[index]
                                    + "' not granted, exiting", Toast.LENGTH_LONG
                        ).show()
                        finish()
                        return
                    }
                }
                // all permissions were granted
                checkGPS()
            }
        }
    }

    private fun checkGPS() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this)
                .setTitle("Koordinat Lokasi")  // GPS not found
                .setMessage("kami membutuhkan koordinat lokasi") // Want to enable?
                .setPositiveButton("Oke") { _, _ -> this.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("Ngga", null)
                .show()
        } else {
            initialize()
        }
    }

    private fun getSupportMapFragment(): SupportMapFragment {
        return supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
    }

    private fun initialize() {
        supportMapFragment = getSupportMapFragment()
        supportMapFragment.retainInstance = false

        supportMapFragment.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                // retrieve a reference of the map from the map fragment
                map = supportMapFragment.map

                map?.addTransformListener(this@MapsActivity)

                positioningManager = getInstance()
                positioningManager?.addListener(WeakReference<OnPositionChangedListener>(this@MapsActivity))

                positioningManager?.start(LocationMethod.GPS_NETWORK)

                if (positioningManager!!.start(LocationMethod.GPS_NETWORK)) run {
                    map?.positionIndicator?.setVisible(true)
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "PositioningdManager.start: failed, exiting",
                        Toast.LENGTH_LONG
                    ).show()
                    map?.zoomLevel = (map?.maxZoomLevel?.plus(map?.minZoomLevel!!))?.div(1)!!
                }
            } else {
                Log.e(LOG_TAG, "Cannot initialize SupportMapFragment ($error)")
            }
        }
    }

    override fun onPause() {
        positioningManager?.stop()
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        checkGPS()
        paused = false
        positioningManager?.start(LocationMethod.GPS_NETWORK)
    }

    override fun onDestroy() {
        super.onDestroy()
        map = null
    }

    override fun onMapTransformStart() {
        transforming = true
    }

    override fun onMapTransformEnd(mapState: MapState?) {
        transforming = false
        if (locationInfo != null) {
            locationInfo?.run()
            locationInfo = null
        }
    }

    override fun onPositionFixChanged(
        p0: LocationMethod?,
        p1: LocationStatus?
    ) {
        //IGNORE
    }

    override fun onPositionUpdated(
        locationMethod: LocationMethod,
        geoPosition: GeoPosition,
        mapMatched: Boolean
    ) {
        val coordinate: GeoCoordinate = geoPosition.coordinate
        if (transforming) {
            locationInfo = Runnable {
                onPositionUpdated(locationMethod, geoPosition, mapMatched)
            }
        } else {
            map?.setCenter(coordinate, Map.Animation.NONE)
            updateLocationInfo(locationMethod, geoPosition)
        }
    }

    private fun updateLocationInfo(locationMethod: LocationMethod, geoPosition: GeoPosition) {

        val location = StringBuffer()
        val coordinate = geoPosition.coordinate
        location.append(
            String.format(
                Locale.US,
                "%.6f, %.6f\n",
                coordinate.latitude,
                coordinate.longitude
            )
        )

        location.deleteCharAt(location.length - 1)
        textLocation.text = location.toString()
        locationCoordinate = location.toString()
    }

    private fun saveLocation() {
        if (!locationCoordinate.isNullOrEmpty()) {
            val intent = Intent(this, FormAddressActivity::class.java)
            intent.putExtra(DATA_COORDINATE, locationCoordinate)
            startActivity(intent)
        }
    }

    private fun goToListPage() {
        val intent = Intent(this, ListAddressActivity::class.java)
        startActivity(intent)
    }
}
