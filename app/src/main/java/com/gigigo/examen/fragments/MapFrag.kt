package com.gigigo.examen.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import com.gigigo.examen.R
import com.gigigo.examen.activities.GalleryActivity
import com.gigigo.examen.activities.MainActivity
import com.gigigo.examen.activities.MapActivity
import com.gigigo.examen.adapters.ReviewsListAdapter
import com.gigigo.examen.models.Places
import com.gigigo.examen.utils.SharePlace
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.c_map.*
import kotlinx.android.synthetic.main.c_map.view.*

class MapFrag(): Fragment(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
     companion object {
         const val name="Map_Fragment"
        private val GPS_REQUEST = 1000
        private val REQUEST_LOCATION = 13
        val onMapReady = 11
    }
    internal var m: Marker? = null
    internal var _mapa: GoogleMap? = null
    private var address:TextView? = null
    private var stateInformation: TextView? = null
    private var bar: ProgressBar? = null
    private var name:TextView? = null
    private var root:ViewGroup?=null
    private var share:ViewGroup?=null
    internal lateinit var mapFragment: SupportMapFragment
    private lateinit var place:Places.Place

    //internal var reciver: ResultReceiver? = null

    @SuppressLint("ValidFragment")
    constructor(place: Places.Place) : this() {
        this.place=place
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places(activity as Context).getDetailForPlace(place
        ) { place->
            if(root!=null){
                val size=place.allPhotos.size
                root!!.c_counter_images.setText("$size fotos")
                if(place.rate>0){
                    root!!.c_rating.numStars=5
                    root!!.c_rating.rating=place.rate
                    root!!.c_rating.visibility=View.VISIBLE
                }

                if(place.allComents.size>0){
                    c_reviews.setOnClickListener(View.OnClickListener {
                        showComments(place.allComents)
                    })
                    c_reviews.visibility=View.VISIBLE
                }else{
                    c_reviews.visibility=View.INVISIBLE
                }
            }
            //root.findViewById(R.id.c_counter_images)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //reciver = arguments!!.getParcelable<Parcelable>("receiver") as ResultReceiver
        val view = inflater.inflate(R.layout.c_map, container, false)
        this.stateInformation = view.findViewById(R.id.c_reviews)
        this.bar = view.findViewById(R.id.c_progressBar)
        val image=view.findViewById(R.id.c_image) as ImageView
        this.name=view.findViewById(R.id.c_name)
        this.address=view.findViewById(R.id.c_vicinity)
        this.root=view.findViewById(R.id.c_container)
        this.share=view.findViewById(R.id.c_share)
        share?.setOnClickListener(View.OnClickListener {
            SharePlace().sharingToSocialMedia(activity as Activity,place.name,place.photo,place.latitud,place.longitud)
        })
        place?.let {
            name?.text=place.name
            address?.text=place.vicinity
            ImageLoader.getInstance().displayImage(place.photo,image)
            image.setOnClickListener(View.OnClickListener {
                var intent=Intent(activity as MapActivity, GalleryActivity::class.java)
                intent.putExtra("Images",place.allPhotos)
                intent.putExtra("Title",place.name)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val options = ActivityOptions.makeSceneTransitionAnimation(activity as MapActivity,
                            Pair.create(it.findViewById(R.id.c_image), "shared_image"))
                    (activity as Context).startActivity(intent,options.toBundle())
                } else {

                    (activity as Context).startActivity(intent)
                }
            })
        }
    return view
    }//32380675


    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        val handler =  Handler()
        handler.postDelayed( Runnable() {
            kotlin.run {
                if (_mapa == null) {
                    try
                    {
                        MapsInitializer.initialize(activity)
                    }
                    catch (e:Exception) {
                        e.printStackTrace()
                    }
                    val FT = childFragmentManager.beginTransaction()
                    mapFragment = SupportMapFragment.newInstance()
                    mapFragment.getMapAsync(this@MapFrag)
                    FT.replace(R.id.c_map_container, mapFragment, "mapa")
                    FT.commit()
                }
            }
          }, 1000)




    }
     override fun onMapReady(map:GoogleMap) {

        _mapa = map
         _mapa!!.setPadding(0, Math.round((activity!!.window.decorView.height / 3 * 2).toFloat()), 0, 0)
        _mapa!!.getUiSettings().setZoomControlsEnabled(false)
        _mapa!!.getUiSettings().setCompassEnabled(true)
        _mapa!!.getUiSettings().setMyLocationButtonEnabled(true)
        _mapa!!.getUiSettings().setZoomGesturesEnabled(true)
         startMap()
    }


    fun hasPermission():Boolean{
        if(ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }


    fun onSelectedlocation(latitud:Double, longitude:Double) {


        if (m == null)
        {
            m = _mapa!!.addMarker(MarkerOptions()
            .position(LatLng(latitud, longitude)))
            m!!.setDraggable(false)
            val cameraposition = LatLng(latitud, longitude)
            _mapa!!.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraposition, 17f))
        }
        stateInformation!!.text = "Reseñas..."
        bar!!.visibility = View.GONE

    }
    override fun onRequestPermissionsResult(requestCode:Int,
        permissions:Array<String>,
        grantResults:IntArray) {
        if (requestCode == REQUEST_LOCATION)
        {
            if ((grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                startMap()
            }
        }
    }

     @SuppressLint("MissingPermission")
     fun startMap() {
         _mapa!!.setPadding(0, Math.round((root?.height!! / 3 * 2).toFloat()), 0, 0)
         //_mapa!!.setMyLocationEnabled(true)
        val cdmx = LatLng(19.432608, -99.133209)
        _mapa!!.moveCamera(CameraUpdateFactory.newLatLngZoom(cdmx, 10f))
        _mapa!!.setOnMyLocationButtonClickListener(object: GoogleMap.OnMyLocationButtonClickListener {
            override fun onMyLocationButtonClick():Boolean {
                if (!hasPermission())
                {
                    ActivityCompat.requestPermissions(activity!!,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
                    return true
                }
                val manager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    displayPromptForEnablingGPS()
                    return true
                }
                return false

            }

        })
         place?.let { onSelectedlocation(it.latitud,it.longitud) }

     }



    fun displayPromptForEnablingGPS() {

        val builder = AlertDialog.Builder(activity!!)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = "Deseas activar GPS?"
        builder.setMessage(message)
        .setPositiveButton("OK") { d, id ->
        activity!!.startActivityForResult(Intent(action), GPS_REQUEST)
        d.dismiss() }
        .setNegativeButton("Cancel",
        object: DialogInterface.OnClickListener {
            override fun onClick(d: DialogInterface, id:Int) {
                d.cancel()
            }
        })
        builder.create().show()
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_REQUEST && resultCode == 0) {
            val provider = Settings.Secure.getString(activity!!.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            if (provider != null) {
            _mapa!!.getMyLocation()
            }
        }
    }


    fun  showComments(reviews: ArrayList<Places.Review>){
        var dialog = Dialog(activity);

        var list = ListView(activity)
        list.layoutParams= LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);


        var adapter = ReviewsListAdapter(activity as Activity, reviews)

        list.setAdapter(adapter)

        dialog.setContentView(list);

        dialog.show();
    }


}