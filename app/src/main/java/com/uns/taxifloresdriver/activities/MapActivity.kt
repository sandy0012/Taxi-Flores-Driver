package com.uns.taxifloresdriver.activities

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.uns.taxifloresdriver.providers.AuthProvider
import com.uns.taxifloresdriver.providers.GeoProvider


class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private lateinit var binding : ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider= GeoProvider()
    private val authProvider= AuthProvider()

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

        binding.bntConnect.setOnClickListener{ connectDriver() }
        binding.bntDisconnect.setOnClickListener{ disconnectDriver() }

    }

    val locationPermissions =registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido")
                    //easyWayLocation?.startLocation();
                    checkIfDriverIsConnected()
                }
                permission.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido con Limitación")
                    //easyWayLocation?.startLocation();
                    checkIfDriverIsConnected()
                }
                else ->{
                    Log.d("LOCALIZACION","Permiso no Concedido")
                }
            }
        }
    }


    private fun checkIfDriverIsConnected(){
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener {
            document ->
            if(document.exists()){
                if(document.contains("l")){
                    connectDriver()
                }
                else{
                    showButtonConnect()
                }
            }
            else{
                showButtonConnect()
            }
        }
    }


    private fun saveLocation(){
        if(myLocationLatLng != null){
            geoProvider.saveLocation(authProvider.getId(),myLocationLatLng!!)
        }
    }


    private fun disconnectDriver(){
        easyWayLocation?.endUpdates()
        if(myLocationLatLng != null){
            geoProvider.removeLocation(authProvider.getId())
            showButtonConnect()
        }
    }

    private fun connectDriver(){
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
        showButtonDisconnect()

    }

    /**
     * ocultar boton de desconectarse
     * mostrar boton de conectarse
     */
    private fun showButtonConnect(){
        binding.bntDisconnect.visibility = View.GONE
        binding.bntConnect.visibility = View.VISIBLE
    }

    private fun showButtonDisconnect(){
        binding.bntDisconnect.visibility = View.VISIBLE
        binding.bntConnect.visibility = View.GONE
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

        val size = 1

        val width = 65 * size
        val heigh = 100 * size

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
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //easyWayLocation?.startLocation();

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



        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if (!success!!) {
                Log.d("MAPAS", "No se pudo encontrar el estilo")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Erro: ${e.toString()}")
        }
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
        saveLocation()
    }

    override fun locationCancelled() {

    }
}