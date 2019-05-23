package com.stevenhdesigns.wifidistancemeasureandroid.services;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class UdpEncoderService {
    public UdpEncoderServiceDelegate delegate;
    private MyDatagramReceiver myDatagramReceiver = null;

    public void setupUDP() throws IOException {


        myDatagramReceiver = new MyDatagramReceiver();
        myDatagramReceiver.start();

    }

    private void newUdpPacket(String d) {
        double v = Double.parseDouble(d);
        delegate.udpEncoderValue(v);
        delegate.udpEncoder(mapEncoderToDistance(v));
    }

    private double mapEncoderToDistance(double value) {
        double[] distances = new double[]{
                0.0,   // encoder ->   0cm
                23.0,  // encoder ->  25cm
                48.0,  // encoder ->  50cm
                73.0,  // encoder ->  75cm
                99.0,  // encoder -> 100cm
                125.0, // encoder -> 125cm
                152.0, // encoder -> 150cm
                180.0, // encoder -> 175cm
                208.0, // encoder -> 200cm
                238.0, // encoder -> 225cm
                268.0, // encoder -> 250cm
                300.0, // encoder -> 275cm
                331.0, // encoder -> 300cm
                365.0, // encoder -> 325cm
                401.0, // encoder -> 350cm
                437.0, // encoder -> 375cm
        };

        double d = 0.0;

        for (int i = 0; i < distances.length; i++) {
            if (value >= distances[i] && value < distances[i+1]) {
                double theRange = (distances[i+1] - distances[i]);
                double percentageOfRange = (value - distances[i]) / theRange;
                double minCm = ((double) i * 25.0);
                d = (minCm + (25.0 * percentageOfRange)) / 100.0;
            }
        }

        return d;
    }

    class MyDatagramReceiver extends Thread {
        private boolean bKeepRunning = true;

        public void run() {
            String message;
            byte[] msg = new byte[100];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);

            try {
                DatagramSocket socket = new DatagramSocket(4201);

                while(bKeepRunning) {
                    socket.receive(packet);
                    message = new String(msg, 0, packet.getLength());
                    newUdpPacket(message);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
