package com.stevenhdesigns.wifidistancemeasureandroid;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.stevenhdesigns.wifidistancemeasureandroid.charts.UpdatableLineChart;
import com.stevenhdesigns.wifidistancemeasureandroid.services.UdpEncoderService;
import com.stevenhdesigns.wifidistancemeasureandroid.services.UdpEncoderServiceDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Color.BLUE;

public class MainActivity extends AppCompatActivity implements UdpEncoderServiceDelegate {
    private UpdatableLineChart actualDistanceLineChart;
    private UpdatableLineChart rssiLineChart;
    private UpdatableLineChart encoderPositionLineChart;
    private Timer timer;
    private ArrayList dataList = new ArrayList<String>();
    private UdpEncoderService udpEncoderService =  new UdpEncoderService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actualDistanceLineChart = findViewById(R.id.actualDistanceLineChart);
        rssiLineChart = findViewById(R.id.rssiLineChart);
        encoderPositionLineChart = findViewById(R.id.encoderPositionLineChart);

        setupCharts();
        setupTimer();

        udpEncoderService.delegate = this;
        try {
            udpEncoderService.setupUDP();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("UDP", "failed");
        }
    }

    void setupCharts() {
        // Setup the line chart view
        setup(actualDistanceLineChart, "Actual Distance (m)");
        setup(rssiLineChart, "RSSI Value");
        setup(encoderPositionLineChart, "Encoder Position");
    }

    void setup(UpdatableLineChart chart, String label) {
        LineDataSet setData = new LineDataSet(new ArrayList<Entry>(), label);
        chart.setLabel(label);
        setData.setDrawCircles(false);
        setData.setLineWidth(2);
        setData.setAxisDependency(YAxis.AxisDependency.RIGHT);
        setData.setColor(BLUE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setData);
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.setUpdateTimeInterval(0.025f);
        chart.setThreshold(10000);
        chart.setExtraLeftOffset(-30);
        chart.setMinOffset(0);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getLegend().setDrawInside(true);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.getLegend().setDrawInside(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawLabels(false);
        chart.setDrawBorders(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.start(this);
    }

    void setupTimer() {
        dataList.add("time,uuid,encoder,distance,rssi");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerAction();
            }
        }, 1, 50);
    }

    void timerAction() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        dataList.add(String.format("%d,%s,%f,%f,%f", System.currentTimeMillis(), deviceId, encoderPositionLineChart.getLastTime(), actualDistanceLineChart.getLastTime(), rssiLineChart.getLastTime()));
    }

    void bluetoothRssi(float value) {
        rssiLineChart.setLastTime(value);
        rssiLineChart.addData(value);
    }

    @Override
    public void udpEncoder(Double distance) {
        actualDistanceLineChart.setLastTime(distance.floatValue());
        actualDistanceLineChart.addData(distance.floatValue());
    }

    @Override
    public void udpEncoderValue(Double value) {
        encoderPositionLineChart.setLastTime(value.floatValue());
        encoderPositionLineChart.addData(value.floatValue());
    }

    void onSaveDataButtonPressed() {
        // TODO: mail data
    }
}
