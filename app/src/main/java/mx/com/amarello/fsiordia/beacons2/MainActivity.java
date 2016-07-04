package mx.com.amarello.fsiordia.beacons2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;

    TextView cerca;
    TextView med;
    TextView lejos;

    final String idBeacons = new String("10203040-1010-0000-BEBE-FEFE21052016");
    //final String idBeacons = new String("B9407F30-F5F8-466E-AFF9-25556B57FE6D");

    static String terraza = new String("1000:1");
    //final String terraza = new String("31440:58790");

    static String salaJuntas = new String("1000:2");
    //final String salaJuntas = new String("43544:12085");

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put(salaJuntas, new ArrayList<String>() {{
            add("Estás en terraza - Morado");
            add("A medio camino está Damian");
            add("Sala de juntas Lejos - Azul");
        }});
        placesByBeacons.put(terraza, new ArrayList<String>() {{
            add("Estas en Sala de Juntas - Azul");
            add("A medio camino está Damian");
            add("Terraza Lejos - Morado");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cerca=new TextView(this);
        cerca=(TextView)findViewById(R.id.cercaText);

        med=new TextView(this);
        med=(TextView)findViewById(R.id.medText);

        lejos=new TextView(this);
        lejos=(TextView)findViewById(R.id.lejosText);

        beaconManager = new BeaconManager(this);
        region = new Region("ranged region",
                UUID.fromString(idBeacons),  null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    List<String> places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here

                    updateInterfaz(places);

                    Log.d("Airport", "Nearest places: " + places);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }


    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

    private void updateInterfaz(List lugares){


        cerca.setText(lugares.get(0).toString());
        med.setText(lugares.get(1).toString());
        lejos.setText(lugares.get(2).toString());
    }
}
