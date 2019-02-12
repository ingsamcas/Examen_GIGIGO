package eclipseapps.mobility.parkeame.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eclipseapps.libraries.library.general.functions.OrderMap;
import eclipseapps.libraries.library.general.functions.general;
import eclipseapps.mobility.parkeame.R;
import eclipseapps.mobility.parkeame.cloud.Parkimetros;
import eclipseapps.mobility.parkeame.cloud.Estacionamientos;

/**
 * Created by usuario on 07/09/17.
 */

public class fragment_map extends Fragment implements OnMapReadyCallback,ActivityCompat.OnRequestPermissionsResultCallback, PlaceSelectionListener {
    /**Codigo del mapa*/

    private static final int GPS_REQUEST = 1000;
    private Place direccion;
    AutoComplete autocompleteFragment;
    private static final int REQUEST_LOCATION = 13;
    boolean withService;
    Marker m;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    GoogleMap _mapa;
    private String address;
    private Address completeAdress;
    private String coordenadas;
    SupportMapFragment mapFragment;
    mapResult listener;
    FrameLayout mCustomMarkerView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).addApi(LocationServices.API).build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCustomMarkerView = (FrameLayout) inflater.inflate(R.layout.a_a_view_custom_marker, null);
        mCustomMarkerView=mCustomMarkerView.findViewById(R.id.custom_marker_view);
        return inflater.inflate(R.layout.a_a_map_layout,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        // Retrieve the PlaceAutocompleteFragment.
        FragmentTransaction FT=getChildFragmentManager().beginTransaction();
        autocompleteFragment = new AutoComplete().setInter(new fragCallbacks() {
            @Override
            public void onActivityCreated() {
                autocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(14.43468, -85.619888), new LatLng(32.7176, -117.1943)));
                autocompleteFragment.setOnPlaceSelectedListener(fragment_map.this);
                autocompleteFragment.setHint("Donde Parkeas?");

            }
        });
        FT.replace(R.id.a_a_direction_container,autocompleteFragment,"Direccion");
        // Register a editAccountListener to receive callbacks when a place has been selected or an error has
        // occurred.


        if (_mapa == null) {
            mapFragment =  SupportMapFragment.newInstance();
            mapFragment.getMapAsync(fragment_map.this);
            FT.replace(R.id.a_a_map_container,mapFragment,"mapa");
        }
        FT.commit();
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        direccion = place;
        if (_mapa == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            _mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            onSelectedlocation(place.getLatLng().latitude,place.getLatLng().longitude);
        }
    }


    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {

        Toast.makeText(getActivity(), "Place selection failed: " + status.getStatusMessage(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        _mapa = map;
        _mapa.getUiSettings().setZoomControlsEnabled(true);
        _mapa.getUiSettings().setCompassEnabled(true);
        _mapa.getUiSettings().setMyLocationButtonEnabled(true);
        _mapa.getUiSettings().setZoomGesturesEnabled(true);
        _mapa.setPadding(0, Math.round(getActivity().getWindow().getDecorView().getHeight() / 3*2), 0, 0);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }else{
            startMapwithPermissions();
        }
    }
    public void onSelectedlocation(double latitud,double longitude) {

        Geocoder geocoder;
        List<Address> addresses=new ArrayList<Address>() ;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitud, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses.size()>0) {
            completeAdress=addresses.get(0);
            address = completeAdress.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            coordenadas=String.valueOf(completeAdress.getLatitude())+"_"+
                    String.valueOf(completeAdress.getLongitude());
            String city = completeAdress.getLocality();
            String state = completeAdress.getAdminArea();
            String country = completeAdress.getCountryName();
            String postalCode = completeAdress.getPostalCode();
            String knownName = completeAdress.getFeatureName(); // Only if available else return NULL
            String municipio=completeAdress.getSubAdminArea();
            if (m == null) {
                m = _mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(latitud, longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView,0))));

                m.setDraggable(true);

            }
            withService = false;

            if (country!=null&&country.matches("México")){
                m.setPosition(new LatLng(latitud, longitude));
                if (city!=null && city.matches("Ciudad de México"))withService = true;
                if(!withService&&municipio!=null){
                    Resources r = getResources();
                    withService = general.ExistItem(r.getStringArray(R.array.municipios),municipio);
                }
                if (!withService&&postalCode!=null){
                    int _code = Integer.valueOf(postalCode);
                    Resources r = getResources();
                    int[] postalCodes = r.getIntArray(R.array.PostalCodes);
                    for (int code : postalCodes) {
                        if (code == _code) {
                            withService = true;
                        }
                    }
                }
            }
            if (withService) {
                m.setPosition(new LatLng(latitud, longitude));
                m.setTitle("Parkeo");
                m.setSnippet("Toca para confirmar que parkeas aqui");
                m.showInfoWindow();
            } else {
                m.setPosition(new LatLng(latitud, longitude));
                m.setTitle("Sin cobertura");
                m.setSnippet("Por el momento no tenemos cobertura en esta zona");
                m.showInfoWindow();
            }
            autocompleteFragment.setText(address);
        }
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMapwithPermissions();
            }
        }
    }

    public void startMapwithPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        _mapa.setMyLocationEnabled(true);
        LatLng cdmx = new LatLng(19.432608, -99.133209);
        _mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(cdmx, 10));
        _mapa.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION);
                    return true;
                }
                final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    displayPromptForEnablingGPS();
                    return true;
                }
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                if (mLastLocation != null) {
                    onSelectedlocation(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                }
                return false;

            }

        });
        _mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (latLng != null) {
                    onSelectedlocation(latLng.latitude,latLng.longitude);
                }
            }
        });
        _mapa.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker.getPosition()!= null) {
                    onSelectedlocation(marker.getPosition().latitude,marker.getPosition().longitude);
                }
            }
        });
        _mapa.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //_mapa.clear();
                Action viewAction = Action.newAction(
                        Action.TYPE_VIEW, // TODO: choose an action type.
                        "_mapa Page", // TODO: Define a title for the content shown.
                        // TODO: If you have web page content that matches this app activity's content,
                        // make sure this auto-generated web page URL is correct.
                        // Otherwise, set the URL to null.
                        Uri.parse("http://host/path"),
                        // TODO: Make sure this auto-generated app URL is correct.
                        Uri.parse("android-app://eclipseapps.mobility.parkeame/http/host/path")
                );
                if (withService){
                    if (!address.matches("")){
                        Intent intent=new Intent();
                        intent.putExtra("mapa",address);
                        intent.putExtra("coordenadas",coordenadas);
                        if(listener!=null)listener.onPlaceSelected(completeAdress);
                        //setResult(RESULT_OK,intent);
                    }else{
                       // setResult(RESULT_CANCELED,null);
                    }
                }
            }

        });


        final OrderMap<Marker,Estacionamientos> parkings= new OrderMap<Marker,Estacionamientos>();
        final List<Estacionamientos> estacionamientos=listener.getEstacionamientos();
        for (int i=0;i<estacionamientos.size();i++) {
            Marker m = _mapa.addMarker(new MarkerOptions()
                    .position((LatLng) estacionamientos.get(i).getUbicacion())
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, estacionamientos.get(i).getParkimetrosDisponibles()))));
            m.setDraggable(false);
            parkings.put(m,estacionamientos.get(i));
        }
        _mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(parkings.containsKey(marker)){
                    listener.onEstacionamientoSelected((Estacionamientos) parkings.get(marker));
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "_mapa Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://eclipseapps.mobility.parkeame/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
//
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "_mapa Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://eclipseapps.mobility.parkeame/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
        super.onStop();
    }


    public void displayPromptForEnablingGPS() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Deseas activar GPS?";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                getActivity().startActivityForResult(new Intent(action), GPS_REQUEST);
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST && resultCode == 0) {
            String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (provider != null) {
                _mapa.getMyLocation();
            }
        }
    }
    /**
     * @param view is custom marker layout which we will convert into bitmap.
     * @param NumberOfParking The numbre that will appear in the marker.
     * @return
     */
    private Bitmap getMarkerBitmapFromView(FrameLayout view,int NumberOfParking) {
        TextView numberParking=view.findViewById(R.id.a_a_parking_number);
        if (NumberOfParking>0){
            numberParking.setText(String.valueOf(NumberOfParking));
        }else{
            //((RelativeLayout)(numberParking.getParent())).setVisibility(View.GONE);
        }
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        view.draw(canvas);

        ((RelativeLayout)(numberParking.getParent())).setVisibility(View.VISIBLE);
        Drawable drawable = ((RelativeLayout)(numberParking.getParent())).getBackground();
        if (drawable != null){
            drawable.setBounds(30,view.getMeasuredHeight()-30,30,30);
            drawable.draw(canvas);
        }


        return returnedBitmap;
    }
    public static class AutoComplete extends SupportPlaceAutocompleteFragment{
        //View GeneralView;
        fragCallbacks inter;
        private View zziej;
        private View zziek;
        private View zziel;


        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);
            inter.onActivityCreated();
        }
        public AutoComplete setInter(fragCallbacks inter) {
            this.inter = inter;
            return this;
        }

        @Override
        public void setHint(CharSequence charSequence) {
            super.setHint(charSequence);
        }
    }

    public fragment_map setListener(mapResult listener) {
        this.listener = listener;
        return this;
    }

    interface fragCallbacks{
        void onActivityCreated();
    }
    interface mapResult{
        void onPlaceSelected(Address adress);
        void onEstacionamientoSelected(Estacionamientos estacionamiento);
        List<Estacionamientos> getEstacionamientos();
        List<Parkimetros> getParkings(LatLng ll);
    }
}
