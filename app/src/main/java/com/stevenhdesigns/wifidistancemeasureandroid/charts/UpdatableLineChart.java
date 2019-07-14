package com.stevenhdesigns.wifidistancemeasureandroid.charts;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class UpdatableLineChart extends LineChart {
    Timer timer;
    float updateTimeInterval = 1.0f;
    float threshold = 1000.0f;
    long startMillis = System.currentTimeMillis();
    float lastTime = 0.0f;
    String label;
    private Activity activity;
    float prevX = -999f;

    public UpdatableLineChart(Context context) {
        super(context);
    }

    public UpdatableLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UpdatableLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setUpdateTimeInterval(float updateTimeInterval) {
        this.updateTimeInterval = updateTimeInterval;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public float getLastTime() {
        return lastTime;
    }

    public void setLastTime(float lastTime) {
        this.lastTime = lastTime;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    void timerAction() {
        if (Build.FINGERPRINT.contains("generic")) { // IF simulator
            lastTime = Math.round((new Random()).nextFloat() * 5) - 5;
        }
        addData(lastTime);
    }

    void removeOldDataFor(int datasetIndex) {
        ILineDataSet dset = getData().getDataSetByIndex(datasetIndex);
        Entry e = dset.getEntryForIndex(0);
        if (e != null && e.getX() < (System.currentTimeMillis() - startMillis - threshold)) {
            dset.removeEntry(dset.getEntryForIndex(0));
        }
    }

    public void start(Activity activity) {
        this.activity = activity;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerAction();
            }
        }, 0, 10);
    }

    public void addData(final float v) {
        final int dataSetIndex = 0;
        final float diff = System.currentTimeMillis() - startMillis;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // @BUG: because this is run on the main thread, the order of execution seems to
                    // go out of order at times which causes some issues.
                    if (diff >= prevX) {
                        prevX = diff;

                        getData().addEntry(new Entry(diff, v), dataSetIndex);
                        getXAxis().setAxisMinimum(diff - threshold);
                        removeOldDataFor(dataSetIndex);
                        getData().notifyDataChanged();
                        notifyDataSetChanged();
                        invalidate();
                    }
                } catch (Exception e) {
                    // pass
                }
            }
        });
    }
}
