package fr.paulinedecouvertebordeaux;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
// import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager localisationManager; // gere les mises a jours de la localisation GPS
    private TextView cibleTv; // nom de la cible
    private Location cibleLoc; // localisation (long.lat de la cible)
    private Marker myMarker ; // marqueur de l'utilisateur
    private Marker cibleMarker ; // marqueur de la cible

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //notification quand la carte est prete a etre utilisée
        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException {
        mMap = googleMap;

        mMap.setMyLocationEnabled(false); // n'affiche pas le rond bleu de localisation
        mMap.getUiSettings().setZoomControlsEnabled(true); // affiche les boutons de zoom
        mMap.getUiSettings().setZoomGesturesEnabled(true); // autorise le zoom tactile
        mMap.getUiSettings().setCompassEnabled(true); // affiche pas le compas
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // affiche le bouton de localisation

        String cibleDesc ="ENSC";
        double cibleLatitude =  44.806287;
        double cibleLongitude = -0.596923;
        cibleLoc = new Location("Cible");
        cibleLoc.setLongitude(cibleLatitude);
        cibleLoc.setLatitude(cibleLatitude) ;
        cibleTv = (TextView)findViewById(R.id.tv);
        // String cibleTexte = "Cible : " + cibleDesc;
        // cibleTv.setText(cibleTexte);

        myMarker = mMap.addMarker(new MarkerOptions()
            .title("Moi")
            .position(new LatLng(0,0))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.petitbonhomme)));

        cibleMarker = mMap.addMarker(new MarkerOptions()
            .title("Cible")
            .snippet(cibleDesc)
            .position(new LatLng(cibleLatitude,cibleLongitude))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
    }

    public void abonnementGPS() throws SecurityException{
        localisationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10, this);
    }

    public void desabonnementGPS() throws SecurityException{
        localisationManager.removeUpdates(this);
    }

    // affichage d'un alertDialog
    public void showAlert(String titre, String message)
    {
        AlertDialog.Builder ADBuilder = new AlertDialog.Builder(this);
        ADBuilder.setTitle(titre);
        ADBuilder.setMessage(message);
        ADBuilder.setCancelable(true);
        ADBuilder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });
        AlertDialog AD = ADBuilder.create();
        AD.show();
    }

    @Override // quand la localisation est activée
    public void onProviderEnabled (final String provider)
    {
        if("gps".equals(provider)){
            abonnementGPS();
        }
    }

    @Override // quand la localisation est désactivée
    public void onProviderDisabled(final String provider)
    {
        if("gps".equals(provider)) {
            desabonnementGPS();
        }
    }

    @Override // quand la position change
    public void onLocationChanged (final Location myLocalisation)
    {
        final LatLng myPosition = new LatLng(myLocalisation.getLatitude(), myLocalisation.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16)); // zoom autour de la localisation de l'utilisateur
        myMarker.setPosition(myPosition);

        double distance = myLocalisation.distanceTo(cibleLoc)*0.000001;
        myMarker.setSnippet("à "+ Math.round(distance) + " mètres de la cible");
        myMarker.showInfoWindow();
        if (distance < 30.0)
        {
            this.showAlert("Félicitation !", "Vous avez atteint la cible.");
            this.desabonnementGPS();
        }
    }

    @Override // quand le status d’une source de localisation change
    public void onStatusChanged(final String provider, final int status, final Bundle extras)
    {
    }

    // Pour eviter d'utiliser des données et faire perdre trop de batterie
    @Override // ne pas utiliser le GPS quand l'appli n'est pas active
    public void onPause()
    {
        super.onPause();
        desabonnementGPS();
    }

    @Override // utiliser le GPS quand l'appli est active
    public void onResume()
    {
        super.onResume();
        localisationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
       if (localisationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            abonnementGPS();
        }
    }
}

