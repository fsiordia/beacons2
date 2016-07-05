package mx.com.amarello.fsiordia.beacons2;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;

    TextView cerca;
    TextView med;
    TextView lejos;

    EditText nombreTextEdit;
    Button guardarBoton;
    TextView nombreGuardado;


    final String idBeacons = new String("10203040-1010-0000-BEBE-FEFE21052016");
    //final String idBeacons = new String("B9407F30-F5F8-466E-AFF9-25556B57FE6D");

    static String terraza = new String("1000:1");
    //final String terraza = new String("31440:58790");

    static String salaJuntas = new String("1000:2");
    //final String salaJuntas = new String("43544:12085");

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    public static final String PREFS_NAME = "MyPrefsFile";



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


        // find View-elements
        nombreTextEdit = (EditText) findViewById(R.id.nombreText);
        nombreGuardado = (TextView) findViewById(R.id.savedName);
        guardarBoton = (Button) findViewById(R.id.guardarButton);


        View.OnClickListener oclBtnGuardar = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // change text of the TextView (tvOut)
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("nombre", nombreTextEdit.getText().toString());
                nombreGuardado.setText(nombreTextEdit.getText().toString());
                editor.commit();
            }
        };

        // assign click listener to the OK button (btnOK)
        guardarBoton.setOnClickListener(oclBtnGuardar);
        // Restore preferences




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
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    String nombreGuardado = settings.getString("nombre", "Visitante");

                    String statusBeaconMsj = new String(nombreGuardado.toString()+" " + places.get(0).toString());



                    sendRequest(statusBeaconMsj);
                    Log.d("Enviamos el mensaje: ",statusBeaconMsj);
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

    private void sendRequest(String mensaje){
        final TextView mTextView = (TextView) findViewById(R.id.statusText);

        String mensajeEncoded = new String("visitante");
        try {
            mensajeEncoded = URLEncoder.encode(mensaje.toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://estimote.amarellodev.com/save.php?u=" + mensajeEncoded;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("respuestaes: "+ response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("noFunciono!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
