package com.cse437.sensor_basedauthenticationapp;

public class ProgressBarThread extends Thread {
    @Override
    public void run() {
        super.run();
        while (MainActivity.PinAuthenticated==false && MainActivity.ErrorRisen==false && MainActivity.StopThread==false) {
            MainActivity.handler.post(new Runnable() {
                public void run() {
                    MainActivity.pb.setProgress((int)MainActivity.TimeDiff);
                }
            });
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
