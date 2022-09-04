package com.uns.taxifloresdriver.fragments

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.uns.taxifloresdriver.R
import com.uns.taxifloresdriver.databinding.FragmentMapTripBinding
import com.uns.taxifloresdriver.models.Booking
import com.uns.taxifloresdriver.models.History
import com.uns.taxifloresdriver.models.Prices
import com.uns.taxifloresdriver.providers.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MapTripFragment : Fragment(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private var totalPrice = 0.0
    private val configProvider = ConfigProvider()
    private var markerDestination: Marker? = null
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var markerOrigin: Marker? = null
    private var bookingListener: ListenerRegistration? =null
    private var _binding: FragmentMapTripBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider= GeoProvider()
    private val authProvider= AuthProvider()
    private val bookingProvider= BookingProvider()
    private val historyProvider= HistoryProvider()
    private val modalBooking = ModalBottomSheetBooking()

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG ="way_point_tag"
    private lateinit var directionUtil : DirectionUtil

    private var isLocationEnabled = false
    private var isCloseToOrigin = false
    //DISTANCIA
    private var meters = 0.0
    private var km = 0.0
    private var currentLocation = Location("")
    private var previusLocation = Location("")
    private var isStartedTrip = false

    //MODAL
    private var modalTrip = ModalBottomSheetTripInfo()

    //TIEMPO
    private var counter = 0
    private var min = 0
    private var handler = Handler(Looper.myLooper()!!)
    private var runnable = Runnable{
        kotlin.run {
            counter++

            if (min == 0){
                binding.textViewTimer.text = "$counter Seg"
            }else{
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            if (counter == 60){
                min = min+(counter/60)
                counter = 0
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            startTimer()
        }
    }


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
        _binding = FragmentMapTripBinding.inflate(inflater, container, false)

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

        binding.bntStartTrip.setOnClickListener { updateToStarted() }
        binding.bntFinishTrip.setOnClickListener { updateToFinish()}
        binding.imageViewInfo.setOnClickListener { showModalInfo() }
//        binding.bntConnect.setOnClickListener{ connectDriver() }
//        binding.bntDisconnect.setOnClickListener{ disconnectDriver() }

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



    private fun showModalInfo(){
        modalTrip.show(childFragmentManager, ModalBottomSheetTripInfo.TAG)
    }

    private fun startTimer(){
        handler.postDelayed(runnable, 1000)
    }


    private fun getDistanceBetween(originLatLng: LatLng, destinationLatLng: LatLng): Float{
        var distance = 0.0f
        val originLocation = Location("")
        val destinationLocation = Location("")

        originLocation.latitude=originLatLng.latitude
        originLocation.longitude=originLatLng.longitude

        destinationLocation.latitude=destinationLatLng.latitude
        destinationLocation.longitude=destinationLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return distance

    }

    private fun getBooking(){
        bookingProvider.getBooking().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.size()>0){
                    booking = query.documents[0].toObject(Booking::class.java)
                    originLatLng = LatLng(booking?.originLat!!,booking?.originLng!!)
                    destinationLatLng = LatLng(booking?.destinationLat!!,booking?.destinationLng!!)
                    easyDrawRoute(originLatLng!!)
                    addOriginMarker(originLatLng!!)
                }
            }

        }
    }


    private fun addOriginMarker(position: LatLng){
        markerOrigin=googleMap?.addMarker(MarkerOptions().position(position).title("Recoger aqui!")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker(){
        if (destinationLatLng != null){
            markerDestination=googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger aqui!")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin24)))
        }
    }

    private fun easyDrawRoute(position: LatLng){

        wayPoints.clear()
        wayPoints.add(myLocationLatLng!!)
        wayPoints.add(position)

        directionUtil=DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocationLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.back)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position)
            .build()

        directionUtil.initPath()

    }


    private fun createHistory(){
        val history = History(
            idDriver = authProvider.getId(),
            idClient = booking?.idClient,
            origin = booking?.origin,
            destination = booking?.destination,
            originLat = booking?.originLat,
            originLng = booking?.originLng,
            destinationLat = booking?.destinationLat,
            destinationLng = booking?.destinationLng,
            time = min,
            km = km,
            price = totalPrice,
            timestamp = Date().time
        )
        historyProvider.create(history).addOnCompleteListener{
            if (it.isSuccessful){
                bookingProvider.updateStatus(booking?.idClient!!,"finished").addOnCompleteListener{
                    if (it.isSuccessful){
                        goToCalificationClient()
                    }
                }
            }
        }
    }



    private fun getPrices(distance : Double, time : Double){
        configProvider.getPrices().addOnSuccessListener { document ->
            if (document.exists()){
                val prices = document.toObject(Prices::class.java) //DOCUMENTO CON LA INFORMACION
                val totalDistance = distance * prices?.km!!
                val totalTime = time * prices.min!!

                totalPrice =totalDistance + totalTime
                totalPrice = if (totalPrice < 5.0) prices.minValue!! else totalPrice

                createHistory()
            }
        }
    }


    private fun showModalBooking(booking: Booking){
        val bundle = Bundle()
        bundle.putString("booking",booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.show(childFragmentManager,ModalBottomSheetBooking.TAG)
        timer.start()
    }





    private fun saveLocation(){
        if(myLocationLatLng != null){
            geoProvider.saveLocationWorking(authProvider.getId(),myLocationLatLng!!)
        }
    }


    private fun disconnectDriver(){
        easyWayLocation?.endUpdates()
        if(myLocationLatLng != null){
            geoProvider.removeLocation(authProvider.getId())
        }
    }

    private fun showButtonFinish(){
        binding.bntStartTrip.visibility = View.GONE
        binding.bntFinishTrip.visibility = View.VISIBLE
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


    override fun onDestroy() {//CUANDO CERRAMOS LA APP O PASAMOS A OTRA ACTIVYTI
        super.onDestroy()
        if (myLocationLatLng != null){
            easyWayLocation?.endUpdates();
        }
        handler.removeCallbacks(runnable)

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


    private fun updateToStarted(){
        if (isCloseToOrigin){
            bookingProvider.updateStatus(booking?.idClient!!, "started").addOnCompleteListener{
                if (it.isSuccessful){
                    if (destinationLatLng != null){
                        isStartedTrip = true
                        googleMap?.clear()
                        addMarker()
                        easyDrawRoute(destinationLatLng!!)
                        markerOrigin?.remove()
                        addDestinationMarker()
                        startTimer()
                    }
                    showButtonFinish()
                }
            }
        }
        else{
            Toast.makeText(context, "Debes estar más cerca a la posición de recogida", Toast.LENGTH_LONG).show()
        }
    }


    private fun updateToFinish(){
        handler.removeCallbacks(runnable)
        isStartedTrip = false
        easyWayLocation?.endUpdates()
        geoProvider.removeLocationWorking(authProvider.getId())

        if (min ==0 ){
            min = 1
        }
        getPrices(km,min.toDouble())


    }

    private fun goToCalificationClient(){
        val bundle = Bundle()
        bundle.putDouble("price",totalPrice)
        view?.findNavController()?.navigate(R.id.action_mapTripFragment_to_calificationClientFragment,bundle)
    }

    override fun locationOn() {

    }

    /**
     * Actualizacion de la ubicacion en tiempo real
     */
    override fun currentLocation(location: Location) {
        myLocationLatLng = LatLng(location.latitude, location.longitude)
        //latitud y longitud de la posicion actual

        currentLocation = location

        if (isStartedTrip){
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters/1000
            binding.textViewDistance.text= "${String.format("%.1f",km)} Km"
        }
        previusLocation = location

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
            )
        )

        addMarker()
        saveLocation()

        if (booking != null && originLatLng != null){
            var distance = getDistanceBetween(myLocationLatLng!!,originLatLng!!)
            Log.d("DISTANCE","DISTANCE: $distance")
            if (distance <= 3000){
                isCloseToOrigin = true
            }
            Log.d("LOCATION", "Distance: ${distance}")
        }

        if (!isLocationEnabled){
            isLocationEnabled = true
            getBooking()
        }
    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) = directionUtil.drawPath(WAY_POINT_TAG)
}