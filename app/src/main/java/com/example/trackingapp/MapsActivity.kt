package com.example.trackingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.trackingapp.databinding.ActivityMapsBinding
import com.google.android.gms.dynamic.IFragmentWrapper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var database:FirebaseDatabase
    private lateinit var dbRef : DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Binin city, Nigeria.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getCurrentLocation()

// navigating to user location activity
    val findUserButton :Button = findViewById(R.id.findUserButton)
        findUserButton.setOnClickListener {
            val intent = Intent(this, UsersLocation::class.java)
            startActivity(intent)
        }

//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(6.2362 , 5.5732 )
//       mMap.addMarker(MarkerOptions().position(sydney).title("benin city")).setIcon(
//            BitmapDescriptorFactory.fromResource(R.drawable.cat)
//        )
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
    }

    // initailize the fudeLocationClient



    private fun getCurrentLocation() {
      if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED){
   // if the permission is not yet granted the call the request permission function
          requestLocationPermission()
      }else{
          map.isMyLocationEnabled = true

          fusedLocationClient.lastLocation.addOnCompleteListener {

              val  location = it.result //obtain the lastLocation running on the background thread
              //reference the database
              database = FirebaseDatabase.getInstance()
              dbRef= database.getReference("User")

// check if location is not empty then add a marker to the exact position
              if (location!= null){

                  val latLng = LatLng(location.latitude, location.longitude)
                  map.addMarker(MarkerOptions().position(latLng).title("you are here")).setIcon(
                      BitmapDescriptorFactory.fromResource(R.drawable.cat)
                  )

                  //instantial an object to spacify how the camera will be updated
                  var update = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                  map.moveCamera(update)
                  //set the location datas to the database
                  dbRef.setValue(location)
              }else{
                  Toast.makeText(this, "No location found", Toast.LENGTH_LONG).show()
              }
          }
      }
    }


// this function is used to request for te location permission
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), //permission in the manifest
            REQUEST_LOCATION)
    }

    companion object {
        private const val REQUEST_LOCATION = 1 //request code to identify specific permission request

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // check if the request code matches with the Request_location
        if (requestCode == REQUEST_LOCATION){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }else{
                Toast.makeText(this, "Location Permission is Denied",  Toast.LENGTH_LONG).show()
            }
        }

    }
}