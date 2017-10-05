package org.padmal.cellscanner;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TelephonyManager tm;
    private TextView board, fullList;
    private Timer timer;
    private ArrayList<Integer> listOfPSCs;
    private HashMap<Integer, String> cellIDwithPSC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listOfPSCs = new ArrayList<>();
        cellIDwithPSC = new HashMap<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Click Start to log", Snackbar.LENGTH_LONG)
                        .setAction("Log", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startLogging();
                            }
                        }).show();
            }
        });
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                startLogging();
            }
        }, 1000, 1000);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new CellTowerLocator(this),
                PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                        | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
        board = (TextView) findViewById(R.id.board);
        fullList = (TextView) findViewById(R.id.full_list);
    }

    private void startLogging() {

        GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();

        if (location == null) {
            Log.d("Padmal", "CellLocation null");
        }

        final int cid = location.getCid();
        final int lac = location.getLac();

        final String networkOperator = tm.getNetworkOperator();
        final int mcc = Integer.parseInt(networkOperator.substring(0, 3));
        final int mnc = Integer.parseInt(networkOperator.substring(3));
        int psc = location.getPsc();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CID -> ").append(cid)
                .append(" Lac -> ").append(lac)
                .append(" Mcc -> ").append(mcc) // mobile country code
                .append(" Mnc -> ").append(mnc) // mobile network code
                .append(" PSC -> ").append(psc)
                .append("\n\n");

        if (!cellIDwithPSC.containsKey(psc)) {
            cellIDwithPSC.put(psc, String.valueOf(cid));
        }

        for (NeighboringCellInfo i : tm.getNeighboringCellInfo()) {
            stringBuilder.append("CID : ");
            stringBuilder.append(i.getCid()); // cell id
            stringBuilder.append("; RSSI : ");
            stringBuilder.append(i.getRssi()); // received signal strength
            stringBuilder.append("; PSC : ");
            stringBuilder.append(i.getPsc()); // primary scrambling code
            stringBuilder.append("; LAC : ");
            stringBuilder.append(i.getLac()); // location area code
            stringBuilder.append("; NET : ");
            stringBuilder.append(i.getNetworkType());
            stringBuilder.append(";\n");
            if (!listOfPSCs.contains(i.getPsc())) {
                listOfPSCs.add(i.getPsc());
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                board.setText(stringBuilder.toString());
                StringBuilder sb = new StringBuilder();
                for (int i : listOfPSCs) {
                    sb.append(i).append(", ");
                }
                sb.append("\n\nHash map\n");
                sb.append(Collections.singletonList(cellIDwithPSC));
                fullList.setText(sb.toString());
            }
        });
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
