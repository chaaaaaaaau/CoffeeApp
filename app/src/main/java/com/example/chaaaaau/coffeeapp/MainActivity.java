package com.example.chaaaaau.coffeeapp;

/**
 * Reference to Google Samples - android-bluetooth
 * https://developers.google.com/
 * */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Rating;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static java.sql.Types.NULL;


public class MainActivity  extends AppCompatActivity implements Constants{

    private LocalDatabaseHelper dbHelper = new LocalDatabaseHelper(MainActivity.this);

    /*Action bar elements*/
    private TextView TV_Console, TV_status, tv_signal;
    private LinearLayout LL_statusBar;
    private Button btn_NewConnection, btn_Disconnect, btn1, btn2;

    /*Navigation Drawer*/
    private DrawerLayout mDrawer;
    private int currentFragment = 0;
    private final static int FRAGMENT_PANEL = 0;
    private final static int FRAGMENT_TASK = 1;

    protected BluetoothLeHelper mBluetoothHelper;
    protected int currentRemoteState = REMOTE_NORMAL;
    protected int lastRemoteState = currentRemoteState;
    protected int superRemoteState = -1;

    LinearLayout dataStat, tasteStat;

    LineChart lChart;
    CombinedChart chart;
    List<Entry> tempEntries, inEntries, outEntries;
    LineDataSet tempDataSet, inDataSet, outDataSet;
    LineData tempLineData, inLineData, outLineData;

    ArrayList<LineDataSet> lines;

    ArrayList<ILineDataSet> dataSets;
    LineData data;
    ImageButton dataChart;
    ImageButton cleatDataBase;
    ImageButton record;
    ImageButton save;
    EditText name;
    ListView coffeeList;
    ArrayList<CoffeeRecordData> recordData;
    CoffeeDataListAdapter adapter;

    TextView showTimeText, connectedDevice1, connected1, connectedDevice2, connected2 ;
    TextView showTemp, showInVol, showOutVol;
    ProgressBar stateBar;
    TextView stateText;
    Runnable runnable ;
    Handler handler;
    boolean showList;
    boolean showChart;
    boolean showSetting;
    boolean pause = false;
    boolean isConnected = false;
    ImageButton timeStart, timePause, timeReset, scanDevice;
    ImageButton userHelp;
    LinearLayout userGuide;
    boolean guide = false;
    int seconds = 0 , minutes = 0;
    int miliseconds = 0;
    Switch switchMode;
    double temp = 0, inVol = 0, outVol = 0;
    int refTime = 0;
    String tStr = "", iStr = "", oStr = "";
    String aMsg = "", bMsg = "";
    private long referenceTime = NULL;
    private long referenceTimeOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        //  dbHelper.DropTable();
        dbHelper.CreateTable();
        //
        mBluetoothHelper = new BluetoothLeHelper(MainActivity.this, mHandler);
        mBluetoothHelper.start();
        //getActionBar().hide();
        initialiseLayout();


    }


    //=============================================================

    void updateAll(String t, String i, String o, int time ){
        if (t != "") temp = Double.parseDouble(t);
        if (i != "") inVol = Double.parseDouble(i);
        if (o != "") outVol = Double.parseDouble(o);
        tempEntries.add(new Entry( (float)(seconds+minutes*60 - time), (float) temp));
        tempDataSet.notifyDataSetChanged();
        inEntries.add(new Entry( (float)(seconds+minutes*60 - time), (float) inVol));
        inDataSet.notifyDataSetChanged();
        outEntries.add(new Entry( (float)(seconds+minutes*60 - time), (float) outVol));
        outDataSet.notifyDataSetChanged();
        data.notifyDataChanged();
        lChart.notifyDataSetChanged();
        lChart.invalidate();
        showTemp.setText(String.format("%.1f", temp));
        showInVol.setText(String.format("%.1f", inVol));
        showOutVol.setText(String.format("%.1f", outVol));
    }
    void updateTemperature(String t, int time) {
        if (t != "") temp = Double.parseDouble(t);
        tempEntries.add(new Entry( (float)(seconds+minutes*60 - time), (float) temp));
        tempDataSet.notifyDataSetChanged();
        data.notifyDataChanged();
        lChart.notifyDataSetChanged();
        lChart.invalidate();
        showTemp.setText(String.format("%.1f", temp));
    }
    void updateInVolume(String i, int t) {
        /*
        inVol = seconds*1.1 + minutes*60*1.14 - t*1.18;
        if (inVol < 0)
            inVol = 0;
        */
        if (i != "")
        inVol = Double.parseDouble(i);
        inEntries.add(new Entry( (float)(seconds+minutes*60 - t), (float) inVol));
        inDataSet.notifyDataSetChanged();
        data.notifyDataChanged();
        lChart.notifyDataSetChanged();
        lChart.invalidate();

        showInVol.setText(String.format("%.1f", inVol));
    }
    void updateOutVolume(String o, int t) {
        /*
        outVol = seconds*0.96 + minutes*60*0.96 - t*0.975;
        if (outVol < 0)
            outVol = 0;
        */
        if ( o != "")
        outVol = Double.parseDouble(o);
        outEntries.add(new Entry( (float)(seconds+minutes*60 - t), (float) outVol));
        outDataSet.notifyDataSetChanged();
        data.notifyDataChanged();
        lChart.notifyDataSetChanged();
        lChart.invalidate();

        showOutVol.setText(String.format("%.1f", outVol));
    }
    void clearChartData(){
        inEntries.clear();
        inDataSet.clear();
        outEntries.clear();
        outDataSet.clear();
        tempEntries.clear();
        tempDataSet.clear();
        data.clearValues();
        lChart.clear();
        lChart.invalidate();
    }
    void addChartData(){
        tempEntries = new ArrayList<Entry>();
        tempDataSet = new LineDataSet(tempEntries, "Temp");// add entries to dataset
        tempDataSet.setColor(getResources().getColor(R.color.colorGroup2_09));
        tempDataSet.setCircleColor(getResources().getColor(R.color.colorGroup2_09));
        tempDataSet.setValueTextColor(getResources().getColor(R.color.GRAY_4));
        tempDataSet.setValueTextSize(8);
        tempDataSet.setLineWidth(2);

        inEntries = new ArrayList<Entry>();
        inDataSet = new LineDataSet(inEntries, "InVol"); // add entries to dataset
        inDataSet.setColor(getResources().getColor(R.color.colorGroup2_07));
        inDataSet.setCircleColor(getResources().getColor(R.color.colorGroup2_07));
        inDataSet.setValueTextColor(getResources().getColor(R.color.GRAY_4));
        inDataSet.setValueTextSize(8);
        inDataSet.setLineWidth(2);

        outEntries = new ArrayList<Entry>();
        outDataSet = new LineDataSet(outEntries, "OutVol"); // add entries to dataset
        outDataSet.setColor(getResources().getColor(R.color.colorGroup2_08));
        outDataSet.setCircleColor(getResources().getColor(R.color.colorGroup2_08));
        outDataSet.setValueTextColor(getResources().getColor(R.color.GRAY_4));
        outDataSet.setValueTextSize(8);
        outDataSet.setLineWidth(2);

        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(inDataSet);
        dataSets.add(outDataSet);
        dataSets.add(tempDataSet);

        data = new LineData(dataSets);
        data.setHighlightEnabled(false);
        lChart.setData(data);
        lChart.invalidate();
      //  Toast.makeText(this,, Toast.LENGTH_SHORT).show();

    }
    void initialiseChart(){
        lChart = (LineChart) findViewById(R.id.chart);
        lChart.clear();
        lChart.getDescription().setText("Time");
        lChart.getDescription().setTextColor(getResources().getColor(R.color.colorGroup2_06));
        lChart.setBackgroundColor(getResources().getColor(R.color.GRAY_5));
        lChart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorGroup2_06));
        lChart.getAxisRight().setTextColor(getResources().getColor(R.color.colorGroup2_06));
        lChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lChart.getXAxis().setTextColor(getResources().getColor(R.color.colorGroup2_06));
        lChart.getLegend().setTextColor(getResources().getColor(R.color.colorGroup2_06));
        lChart.setGridBackgroundColor(getResources().getColor(R.color.GRAY_0));
    } // Set Chart Color and Format
    void initialiseProgressBar(){
        stateText = (TextView) findViewById(R.id.textState);
        stateText.setText("State");
        stateBar = (ProgressBar) findViewById(R.id.progressState);
        stateBar.setProgress(0);
    } // Set State Description and Progress
    void updateChartStateProgress(int t) {
        if (switchMode.isChecked()) { // Brewing Mode. Show Chart. Check State Text. Update Progress
            if (lChart.isEmpty())
                addChartData();
            if (stateText.getText() != "State: Brewing")
                stateText.setText("State: Brewing");
            if (minutes * 60 + seconds - t < 120)
                stateBar.setProgress(((minutes * 60 + seconds - t)));
            else {
                stateBar.setProgress(stateBar.getMax());
                if (seconds % 2 == 1)
                    showTimeText.setTextColor(Color.parseColor("#FFF13C39"));
                else showTimeText.setTextColor(Color.parseColor("#fffd6b"));
            }
        } else {                    // Blooming Mode. No Chart. Check State Text. Check Time Over
                if (!lChart.isEmpty())
                    clearChartData();
                if (stateText.getText() != "State: Blooming")
                    stateText.setText("State: Blooming");
                if (minutes*60 + seconds < 30)  // pre-infuse finish
                    stateBar.setProgress((minutes*60 + seconds)*4);
                else {
                    stateBar.setProgress(stateBar.getMax());
                    if (seconds % 2 == 1)
                        showTimeText.setTextColor(Color.parseColor("#FFF13C39"));
                    else showTimeText.setTextColor(Color.parseColor("#fffd6b"));
                }
            }
        }
    void analyzeData(String data){ // TXXXTIYYYIOZZZO
        int i = 0;
        tStr = "";
        iStr = "";
        oStr = "";
            while (i < data.length()) {
                if (data.charAt(i) == 'T') {
                    i++;
                    while (data.charAt(i) != 'T'){
                        if ((data.charAt(i) >= '0' && data.charAt(i) <= '9') || data.charAt(i) == '.')
                        tStr = tStr + data.charAt(i);
                        i++;
                    }
                    i++;
                }
                if (data.charAt(i) == 'I') {
                    i++;
                    while (data.charAt(i) != 'I'){
                        if ((data.charAt(i) >= '0' && data.charAt(i) <= '9') || data.charAt(i) == '.')
                            iStr = iStr + data.charAt(i);
                        i++;
                    }
                    i++;
                }
                if (data.charAt(i) == 'O') {
                    i++;
                    while (data.charAt(i) != 'O'){
                        if ((data.charAt(i) >= '0' && data.charAt(i) <= '9') || data.charAt(i) == '.')
                            oStr = oStr + data.charAt(i);
                        i++;
                    }
                    i++;
                }

            }
        if (tStr != "") {
                System.out.println("============================= T = " + tStr);
        }
        if (iStr != "") {
                System.out.println("============================= I = " + iStr);
        }
        if (oStr != "") {
            System.out.println("============================= O = " + oStr);
        }
    }
    void showDeviceName(){

            if (mBluetoothHelper.connectedDevice.size() > 0) {
            connectedDevice1.setText(mBluetoothHelper.connectedDevice.get(0).getDeviceName()); //just for testing. No use real name
            // connectedDevice1.setText(mBluetoothHelper.connectedDevice.getDeviceName());
            connected1.setText("Connected");
            connected1.setTextColor(Color.parseColor("#82FA58"));
            }

        if (mBluetoothHelper.connectedDevice.size() > 1) {
                connectedDevice2.setText(mBluetoothHelper.connectedDevice.get(1).getDeviceName()); //just for testing. No use real name
                // connectedDevice1.setText(mBluetoothHelper.connectedDevice.getDeviceName());
                connected2.setText("Connected");
                connected2.setTextColor(Color.parseColor("#82FA58"));
            }
        }

        public void showDialog(){
            LayoutInflater li= LayoutInflater.from(this);
            View  view = li.inflate(R.layout.dialog_record, null);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.customDialog));
            dialogBuilder.setView(view);
            final EditText textCoffeeName = (EditText) view.findViewById(R.id.coffeeName);
            final RatingBar rateC = (RatingBar) view.findViewById(R.id.ratingBarConc);
            final RatingBar rateB = (RatingBar) view.findViewById(R.id.ratingBarBit);
            final RatingBar rateA = (RatingBar) view.findViewById(R.id.ratingBarAcid);
            final RatingBar rateO = (RatingBar) view.findViewById(R.id.ratingBarOverall);

            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Cursor cursor = dbHelper.SelectFromLocalDB("SELECT * FROM coffee_record");
                    cursor.moveToFirst();
                    int id = cursor.getCount() + 1;
                    float temp, in, out, time;
                    dbHelper.ExecuteSQL_Local("INSERT INTO coffee_record VALUES (" + id +", '"+ textCoffeeName.getText() +"', " + rateO.getRating() + ", " + rateB.getRating() + ", " + rateC.getRating() + ", " + rateA.getRating()+ ");");
                    for (int i = 0; i < tempEntries.size() ; i++) {
                        temp = tempEntries.get(i).getY();
                        in = inEntries.get(i).getY();
                        out = outEntries.get(i).getY();
                        time = tempEntries.get(i).getX();
                        dbHelper.ExecuteSQL_Local("INSERT INTO chart_data VALUES (" + ( i + 1) + ", " + time + ", " + temp + ", " + in + ", " + out + ", " + id + ");");
                    }

                    System.out.println("Data Saved ========== ============================================= do list");
                    recordData.add(new CoffeeRecordData(textCoffeeName.getText().toString(), rateO.getRating()));
                    adapter.notifyDataSetChanged();
                    System.out.println("Data Saved ========== ============================================= finish list");

                    Toast.makeText(MainActivity.this, textCoffeeName.getText() + " Data Saved ", Toast.LENGTH_SHORT).show();




                }
            });
            //TextView title = (TextView) dialog.findViewById(R.id.coffeeRecord);
            dialogBuilder.setCancelable(true);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

        }


    private void initialiseLayout() {
        initialiseChart();
        initialiseProgressBar();
        lChart.setVisibility(View.VISIBLE);


        dataStat = (LinearLayout) findViewById(R.id.LayoutDataStat);
        dataStat.setVisibility(View.VISIBLE);

        tasteStat = (LinearLayout) findViewById(R.id.LayoutTasteStat);
        tasteStat.setVisibility(View.INVISIBLE);


        //================================================= Scan Device
        scanDevice = (ImageButton) findViewById(R.id.btnScan);
        scanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   if(mBluetoothHelper.connectedDevice == null) {
                if(mBluetoothHelper.connectedDevice.size() == 0) {
                    mBluetoothHelper.startReceiverService();
                    //launchDeviceDialog();
                }
                if (mBluetoothHelper.connectedDevice.size() < 2)
                    mBluetoothHelper.setupConnection();

            }
        });

        cleatDataBase = (ImageButton) findViewById(R.id.btnClearDatabase);
        cleatDataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.DropTable();
                dbHelper.CreateTable();
                Toast.makeText(MainActivity.this, "Database Clear! ", Toast.LENGTH_SHORT).show();
            }
        });


        dataChart = (ImageButton) findViewById(R.id.btnChart) ;
        dataChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchMode.isChecked()) {
                    lChart.setVisibility(View.VISIBLE);
                    coffeeList.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Coffee List is not enabled in Brewing Mode! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        record = (ImageButton) findViewById(R.id.btnRecord);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchMode.isChecked()) {
                    lChart.setVisibility(View.INVISIBLE);
                    coffeeList.setVisibility(View.VISIBLE);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Coffee List is not enabled in Brewing Mode! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        name = (EditText) findViewById(R.id.coffeeName);
        save = (ImageButton) findViewById(R.id.btnSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        //========================================================= DB COFFEE LIST

        coffeeList = (ListView) findViewById(R.id.listCoffeeRecord);
        recordData = new ArrayList<>();
        adapter = new CoffeeDataListAdapter(MainActivity.this, recordData);
        coffeeList.setAdapter(adapter);
        coffeeList.setVisibility(View.INVISIBLE);
        coffeeList.setBackgroundColor(getResources().getColor(R.color.GRAY_5));
        coffeeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!lChart.isEmpty())
                    clearChartData();
                addChartData();

                Cursor cursor = dbHelper.SelectFromLocalDB("SELECT * FROM chart_data WHERE groupID = " + (position + 1) + ";");
                cursor.moveToFirst();

                Cursor cursorCoffee = dbHelper.SelectFromLocalDB("SELECT * FROM coffee_record WHERE id = " + (position + 1) + ";");
                cursorCoffee.moveToFirst();
                //bitterness NUMBER, concentration NUMBER, acidity NUMBER

                System.out.println("============================ sql ================= "+ "SELECT * FROM coffee_record WHERE id = " + (position + 1) + ";");
                System.out.println("============================ bitter ================= "+ cursorCoffee.getFloat(cursorCoffee.getColumnIndex("bitterness")));
                System.out.println("============================ c ================= "+ cursorCoffee.getFloat(cursorCoffee.getColumnIndex("concentration")));
                System.out.println("============================ a ================= "+ cursorCoffee.getFloat(cursorCoffee.getColumnIndex("acidity")));

                showTemp.setText(String.format("%.1f", cursorCoffee.getFloat(cursorCoffee.getColumnIndex("bitterness"))));
                showInVol.setText(String.format("%.1f", cursorCoffee.getFloat(cursorCoffee.getColumnIndex("concentration"))));
                showOutVol.setText(String.format("%.1f", cursorCoffee.getFloat(cursorCoffee.getColumnIndex("acidity"))));

                for (int i = 0; i < cursor.getCount() ; i++) {
                    tempEntries.add(new Entry(cursor.getFloat(cursor.getColumnIndex("time")), cursor.getFloat(cursor.getColumnIndex("temperature"))));
                    inEntries.add(new Entry(cursor.getFloat(cursor.getColumnIndex("time")), cursor.getFloat(cursor.getColumnIndex("inVol"))));
                    outEntries.add(new Entry(cursor.getFloat(cursor.getColumnIndex("time")), cursor.getFloat(cursor.getColumnIndex("outVol"))));
                    cursor.moveToNext();
                }
                tempDataSet.notifyDataSetChanged();
                inDataSet.notifyDataSetChanged();
                outDataSet.notifyDataSetChanged();
                data.notifyDataChanged();
                lChart.notifyDataSetChanged();
                lChart.invalidate();

                lChart.setVisibility(View.VISIBLE);
                coffeeList.setVisibility(View.INVISIBLE);
                dataStat.setVisibility(View.INVISIBLE);
                tasteStat.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, "Chart Show", Toast.LENGTH_SHORT).show();
            }
        });






        //========================================================== Button initial Setup =============================
        connectedDevice1 = (TextView) findViewById(R.id.textDevice1);
        connected1 = (TextView) findViewById(R.id.textConnect1);
        connectedDevice2 = (TextView) findViewById(R.id.textDevice2);
        connected2 = (TextView) findViewById(R.id.textConnect2);

        showTemp = (TextView) findViewById(R.id.textTempValue);
        showInVol = (TextView) findViewById(R.id.textInValue);
        showOutVol = (TextView) findViewById(R.id.textOutValue);
        switchMode = (Switch) findViewById(R.id.switch1);
        switchMode.setEnabled(true);
        showTimeText = (TextView) findViewById(R.id.textTime);

        //=========================================================== Timer ==============================

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis() - referenceTime + referenceTimeOffset;
                seconds = (int) (now / 1000) % 60;
                minutes = (int) ((now / (1000 * 60)) % 60);
                updateChartStateProgress(refTime);
                if (switchMode.isChecked()) {
                    updateAll(tStr, iStr, oStr, refTime);
                }
                showTimeText.setText(String.format("%02d", minutes) + " : " + String.format("%02d", seconds));
                handler.postDelayed(runnable, 1000);
            }
        } ;

        timeStart = (ImageButton) findViewById(R.id.btnStart);
        timeStart.setEnabled(true);
        timeStart.setBackground(getResources().getDrawable(R.drawable.stroke));
        timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                referenceTime = System.currentTimeMillis();
                handler.postDelayed(runnable, 0);
                switchMode.setEnabled(false);
                timePause.setEnabled(true);
                timePause.setBackground(getResources().getDrawable(R.drawable.stroke));
                timeReset.setEnabled(true);
                timeReset.setBackground(getResources().getDrawable(R.drawable.stroke));
                timeStart.setEnabled(false);
                timeStart.setBackground(getResources().getDrawable(R.drawable.stroke3));

            }
        });

        timePause = (ImageButton) findViewById(R.id.btnPause);
        timePause.setEnabled(false);
        timePause.setBackground(getResources().getDrawable(R.drawable.stroke3));
        timePause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pause == false) {
                    referenceTimeOffset += System.currentTimeMillis() - referenceTime;
                    handler.removeCallbacks(runnable);
                    pause = true;
                    switchMode.setEnabled(true);
                    timeStart.setEnabled(false);
                    timeStart.setBackground(getResources().getDrawable(R.drawable.stroke3));
                    timeReset.setEnabled(true);
                    timeReset.setBackground(getResources().getDrawable(R.drawable.stroke));
                    if (switchMode.isChecked()){
                        save.setEnabled(true);
                        save.setBackground(getResources().getDrawable(R.drawable.stroke));
                    }
                    else{
                        save.setEnabled(false);
                        save.setBackground(getResources().getDrawable(R.drawable.stroke3));
                    }

                }
                else {
                    referenceTime = System.currentTimeMillis();
                    handler.postDelayed(runnable, 0);
                    pause = false;
                    // Start Coffee Mode if second > 30
                    if (switchMode.isChecked() || (seconds > 30 && !switchMode.isChecked())) {
                        switchMode.setChecked(true);
                        showTimeText.setTextColor(Color.parseColor("#fffd6b"));
                        if (refTime <= 0)
                            refTime = seconds + minutes * 60;
                    }
                    switchMode.setEnabled(false);
                    timeStart.setEnabled(false);
                    timeStart.setBackground(getResources().getDrawable(R.drawable.stroke3));
                    timeReset.setEnabled(true);
                    timeReset.setBackground(getResources().getDrawable(R.drawable.stroke));
                    save.setEnabled(false);
                    save.setBackground(getResources().getDrawable(R.drawable.stroke3));
                }
            }});


        timeReset = (ImageButton) findViewById(R.id.btnReset);
        timeReset.setEnabled(false);
        timeReset.setBackground(getResources().getDrawable(R.drawable.stroke3));
        timeReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                referenceTimeOffset = 0;
                handler.removeCallbacks(runnable);
                seconds = 0;
                minutes = 0;
                inVol = 0;
                outVol = 0;
                temp = 0;
                if (!lChart.isEmpty())
                    clearChartData();
                switchMode.setEnabled(true);
                stateBar.setProgress(0);
                stateText.setText("State");
                pause = false;
                switchMode.setChecked(false);
                showInVol.setText(String.format("%.1f", inVol));
                showOutVol.setText(String.format("%.1f", outVol));
                showTemp.setText(String.format("%.1f", temp));
                showTimeText.setText(String.format("%02d", minutes) + " : " + String.format("%02d", seconds));
                showTimeText.setTextColor(Color.parseColor("#fffd6b"));
                timePause.setEnabled(false);
                timeReset.setEnabled(false);
                timePause.setBackground(getResources().getDrawable(R.drawable.stroke3));
                timeReset.setBackground(getResources().getDrawable(R.drawable.stroke3));
                timeStart.setEnabled(true);
                timeStart.setBackground(getResources().getDrawable(R.drawable.stroke));
                save.setEnabled(false);
                save.setBackground(getResources().getDrawable(R.drawable.stroke3));
                coffeeList.setVisibility(View.INVISIBLE);
                lChart.setVisibility(View.VISIBLE);
                tasteStat.setVisibility(View.INVISIBLE);
                dataStat.setVisibility(View.VISIBLE);

            }
        });

        userHelp = (ImageButton) findViewById(R.id.btnHelp);
        userGuide = (LinearLayout) findViewById(R.id.guide) ;
        userHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (guide == false) {
                    guide = true;
                    lChart.setVisibility(View.INVISIBLE);
                    userGuide.setVisibility(View.VISIBLE);
                }
                else if (guide == true) {
                    guide = false;
                    userGuide.setVisibility(View.INVISIBLE);
                    lChart.setVisibility(View.VISIBLE);
                }
            }
        });



    }

    //==============================================================

    private void requestPermissions(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            for(int i=0; i< grantResults.length ; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    new AlertDialog.Builder(this)
                            .setMessage("error_no_permission")
                            .setPositiveButton("btn_continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBluetoothHelper.checkBluetoothEnable();
        //mBluetoothHelper.startReceiverService();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void launchDeviceDialog(){
        Toast.makeText(this, "launchDeviceDialog", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    mBluetoothHelper.connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_CANCELED) {
                    // User did not enable Bluetooth or an error occurred
                    updateStatus(ERROR_BT_NOT_ENABLE);
                    Toast.makeText(getApplicationContext(), "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                    //finish();
                }else if(requestCode == Activity.RESULT_OK){
                    updateStatus(STATE_NONE);
                }
                break;
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            setConsole("C: MESSAGE_STATE_CHANGE : STATE_CONNECTED");
                            if(mBluetoothHelper.connectedDevice != null) {
                                //showDeviceName();

                                setConsole(mBluetoothHelper.connectedDevice.get(mBluetoothHelper.connectedDevice.size()-1).getDeviceName());
                               // setRssiValue(mBluetoothHelper.connectedDevice.getDeviceRSSI());
                            }
                            break;
                        case STATE_DISCOVERED:
                            setConsole("C: MESSAGE_STATE_CHANGE : STATE_DISCOVERED");
                            showDeviceName();
                            //System.out.println("=================================================================== test1");
                            if (mBluetoothHelper.connectedDevice != null) {
                                mBluetoothHelper.sendMessage("", mBluetoothHelper.mBluetoothLeService.mBluetoothDeviceAddress.get(mBluetoothHelper.connectedDevice.size() - 1));
                              //  System.out.println("=================================================================== test2");
                            }
                            //mBluetoothHelper.sendMessage("test message 1");
                            break;
                        case STATE_CONNECTING:
                            setConsole("C: MESSAGE_STATE_CHANGE : STATE_CONNECTING");
                            break;
                        case STATE_LISTEN:
                            setConsole("C: MESSAGE_STATE_CHANGE : STATE_LISTEN");
                            break;
                        case STATE_NONE:
                            setConsole("C: MESSAGE_STATE_CHANGE : STATE_NONE");
                           // onAirshipDisconnected();
                            connected1.setText("Disonnected");
                            connected1.setTextColor(Color.parseColor("#de4e1e"));
                            setConsole("N/A");
                           // setRssiValue(0);
                            break;
                    }
                    updateStatus(msg.arg1);
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    String readMessage = msg.getData().getString(BUNDLE_MSG_READ);
                   // setConsole("C: MESSAGE_READ:" + readMessage);

                    System.out.println("MESSAGE IN<=================" + readMessage);
                    if (readMessage.charAt(0) == 'A')
                        aMsg = readMessage;
                    else if (readMessage.charAt(0) == 'B')
                        bMsg = readMessage;
                    if (aMsg != "" && bMsg != "") {
                        System.out.println("MESSAGE ANALYSE =======" + aMsg.substring(1) + bMsg.substring(1));
                        analyzeData( aMsg.substring(1) + bMsg.substring(1));
                        aMsg = "";
                        bMsg = "";
                    }

                   // analyzeData(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;

                case STATE_CONNECTION_LOST:
                    updateStatus(STATE_CONNECTION_LOST);
                    //onAirshipDisconnected();
                    connected1.setText("Disonnected");
                    connected1.setTextColor(Color.parseColor("#de4e1e"));
                    setConsole("C: N/A");
                    //setRssiValue(0);
                    break;

            }
        }
    };

    protected DecimalFormat df = new DecimalFormat("#.#");

    /*
    private void onAirshipDisconnected() {
        Toast.makeText(this, "disconnected", Toast.LENGTH_SHORT).show();
    }*/

    private String tempMessage = "Loading";
    private int tempState = REMOTE_LOADING;

    public void updateStatus(int statusCode){
        //Log.e("[STATE CODE]", "state code============================================" + statusCode);
        if(superRemoteState != -1 && statusCode != superRemoteState)
            return;
        lastRemoteState = currentRemoteState;
        currentRemoteState = statusCode;

        switch (statusCode){
            case STATE_NONE:
            case STATE_LISTEN:
                tempMessage = "not_connected";
                tempState = REMOTE_WARNING;
                break;
            case STATE_CONNECTING:
                tempMessage = "connecting";
                tempState = REMOTE_LOADING;
                break;
            case STATE_CONNECTED:
            case STATE_DISCOVERED:
                break;
            /*
            case STATE_CONNECTION_LOST:
                tempMessage = "connection_lost";
                tempState = REMOTE_WARNING;
                break;
             */
            case ERROR_BT_NOT_ENABLE:
                tempMessage = "bluetooth_not_enabled";
                System.out.println("bluetooth_not_enabled");
                tempState = REMOTE_WARNING;
                break;
            case ERROR_BT_SIGNAL_WEAK:
                tempMessage = "signal_weak";
                tempState = REMOTE_WARNING;
                break;
            default:
                tempMessage = "Loading";
                tempState = REMOTE_LOADING;
        }

        //System.out.println("state["+statusCode+"]:"+ message +" ;===> color code" + state );
        final String finalMessage = tempMessage;
        final int finalState = tempState;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, finalMessage, Toast.LENGTH_SHORT).show();
                /* change color only
                if(finalState == REMOTE_NORMAL)
                    LL_statusBar.setBackgroundColor(getResources().getColor(R.color.colorGroup2_02));
                else if(finalState == REMOTE_LOADING)
                    LL_statusBar.setBackgroundColor(getResources().getColor(R.color.colorGroup2_06));
                else if(finalState == REMOTE_WARNING)
                    LL_statusBar.setBackgroundColor(getResources().getColor(R.color.colorGroup2_04));
                */
            }
        });
    }

    /* //////////////////////////////////////////////// show the strength of connectivity
    private void setRssiValue(final int value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(value <= -100)
                    tv_signal.setTextColor(getResources().getColor(R.color.colorGroup2_03));
                else
                    tv_signal.setTextColor(getResources().getColor(R.color.GRAY_4));
                tv_signal.setText(String.valueOf(value));
            }
        });
    }
    */
    private void setConsole(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /*
    private void showCustomDialog(){
        (TextView)findViewById(R.id.d_title)
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("-----------[RemoteControlActivity]------onDestroy()---------------------");
    }

    //@Override
    //public void respondData(String output) {    }

    /*
    boolean rssiIsWeak = false;
    @Override
    public void onRssiChange(int rssi) {
        setRssiValue(rssi);
        if(rssi <= -100) {
            superRemoteState = ERROR_BT_SIGNAL_WEAK;
            updateStatus(ERROR_BT_SIGNAL_WEAK);
            rssiIsWeak = true;
        }else{
            superRemoteState = -1;
            if(rssiIsWeak) {
                updateStatus(lastRemoteState);
                rssiIsWeak = false;
            }
        }
    }
    */

    //================================================= Device1 Connection Status
        /*if (isConnected) {

        }*/

        /*
        //Device2 Connection Status
        connectedDevice2 = (TextView) findViewById(R.id.textDevice2);
        // connectedDevice2.setText(mDeviceName);
        connectedDevice2.setText("BLE1");
        connected2 = (TextView) findViewById(R.id.textConnect2);
        connected2.setText("Connected");
        connected2.setTextColor(Color.parseColor("#82FA58"));
        */
}