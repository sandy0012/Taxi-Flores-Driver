package com.uns.taxifloresdriver.fragments

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentMapBinding
import com.uns.taxifloresdriver.models.Booking
import com.uns.taxifloresdriver.providers.AuthProvider
import com.uns.taxifloresdriver.providers.BookingProvider
import com.uns.taxifloresdriver.providers.GeoProvider


class MapFragment : Fragment(), OnMapReadyCallback, Listener {

    private var bookingListener: ListenerRegistration? =null
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider= GeoProvider()
    private val authProvider= AuthProvider()
    private val bookingProvider= BookingProvider()
    private val modalBooking = ModalBottomSheetBooking()

    val timer = object : CountDownTimer(20000,1000){
        override fun onTick(counter: Long) {
           Log.d("TIMER","Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER","On Finish")
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationRequest = LocationRequest.create().apply{
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(context, locationRequest, false, false, this)

        locationPermissions.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
        listenerBooking()
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

    private fun showModalBooking(booking: Booking){
        val bundle = Bundle()
        bundle.putString("booking",booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false
        modalBooking.show(childFragmentManager,ModalBottomSheetBooking.TAG)
        timer.start()
    }

    private fun listenerBooking(){
        bookingListener = bookingProvider.getBooking().addSnapshotListener{ snapshot, e ->
            if (e != null){
                Log.d("FIRESTORE","ERROR: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null){
                if (snapshot.documents.size > 0) {
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if (booking?.status == "create"){
                        showModalBooking(booking)
                    }
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
        val drawable = ContextCompat.getDrawable(requireContext(),R.drawable.car)
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
        bookingListener?.remove()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //easyWayLocation?.startLocation();

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style)
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