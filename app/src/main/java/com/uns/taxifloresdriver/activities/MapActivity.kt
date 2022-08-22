package com.uns.taxifloresdriver.activities

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private lateinit var binding : ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)

        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply{
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermissions.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))

    }

    val locationPermissions =registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido")
                    easyWayLocation?.startLocation();
                }
                permission.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido con Limitación")
                    easyWayLocation?.startLocation();
                }
                else ->{
                    Log.d("LOCALIZACION","Permiso no Concedido")
                }
            }
        }
    }


    private fun addMarker(){
        val drawable = ContextCompat.getDrawable(applicationContext,R.drawable.car)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if(markerDriver != null){
            markerDriver?.remove()
        }
        markerDriver = googleMap?.addMarker(
            MarkerOptions()
                .position(myLocationLatLng!!)
                .anchor(0.5f,0.5f)
                .flat(true)
                .icon(markerIcon)
        )

    }

    //insercion de drawable
    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()
        val width = 65
        val heigh = 100

        val bitmap = Bitmap.createBitmap(
            width,
            heigh,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,width,heigh)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    //ejecuta cada ves que se abre la aplicacio
    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {//CUANDO CERRAMOS LA APP O PASAMOS A OTRA ACTIVYTI
        super.onDestroy()
        easyWayLocation?.endUpdates();
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        easyWayLocation?.startLocation();

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false
    }

    override fun locationOn() {

    }

    /**
     * Actualizacion de la ubicacion en tiempo real
     */
    override fun currentLocation(location: Location) {
        myLocationLatLng = LatLng(location.latitude, location.longitude) //latitud y longitud de la posicion actual

        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
        ))

        addMarker()
    }

    override fun locationCancelled() {

    }
}