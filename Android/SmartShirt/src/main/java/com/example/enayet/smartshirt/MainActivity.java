/**
 * This Application provides a visual way to user of the Smart-Shirt.
 * Developed by: Team-E, Muhammad Enayetur Rahman
 * Aug to Dec, 2017
 * End Date: Dec 12 2017
 * Uses Library
 * -to draw heatmap in Android: AndroidHeatMap: https://github.com/HeartlandSoftware/AndroidHeatMap
 * -For double sided seekbar: https://github.com/anothem/android-range-seek-bar
 */

package com.example.enayet.smartshirt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

import ca.hss.heatmaplib.HeatMap;

import static android.graphics.Color.BLACK;

public class MainActivity extends Activity {
    private static final String TAG = "bluetooth2";
    private static final String TAG_BT = "Enayet";

    TextView tempSensor1_TV;
    TextView tempSensor2_TV;
    TextView tempSensor3_TV;
    TextView tempAvg_TV;
    Handler hMessage;
    HeatMap heatMap;
    RangeSeekBar<Integer> rangeSeekBar;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder ourStringBuilder = new StringBuilder();
    private ImageView mImageView;

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //    private static String address = "00:06:66:DA:D5:8A";
    private static String address = "00:06:66:DA:D5:8A";

    // For Notification The id of the channel.
    private static final String NOTIFICATION_CHANNEL_ID = "my_channel_01";

    Integer minGlobalValue = -100;
    Integer maxGlobalValue = 500;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageview_ID);
        mImageView.setImageResource(R.drawable.tshirt2refayet);
        // Heatmap start
        heatMap = (HeatMap) findViewById(R.id.heatmap);
        heatMap.setMinimum(0.0);
        heatMap.setMaximum(100.0);

        //make the colour gradient from Red to Green
        Map<Float, Integer> colors = new ArrayMap<>();
        for (int i = 0; i < 21; i++) {
            float stop = ((float) i) / 20.0f;
            int color = doGradient(i * 5, 0, 100, 0xff00ff00, 0xffff0000);
            colors.put(stop, color);
        }

        heatMap.setColorStops(colors);

        //make the minimum opacity completely transparent
        heatMap.setMinimumOpactity(0);
        //make the maximum opacity
        heatMap.setMaximumOpactity(255);
        //set the radius to 600 pixels.
        heatMap.setRadius(1000);
        heatMap.forceRefresh();

        tempSensor1_TV = (TextView) findViewById(R.id.tmp1TV_ID);        // for display the received data from the Arduino
        tempSensor2_TV = (TextView) findViewById(R.id.tmp2TV_ID);        // for display the received data from the Arduino
        tempSensor3_TV = (TextView) findViewById(R.id.tmp3TV_ID);        // for display the received data from the Arduino
        tempAvg_TV = (TextView) findViewById(R.id.tmpAvgTV_ID);        // for display the received data from the Arduino
        rangeSeekBar = (RangeSeekBar<Integer>) findViewById(R.id.seekbar_ID);

        // Set the RangeSeekBar settings
        rangeSeekBar.setRangeValues(50, 120);
        rangeSeekBar.setSelectedMinValue(65);
        rangeSeekBar.setSelectedMaxValue(90);
        rangeSeekBar.setTextAboveThumbsColor(BLACK);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                minGlobalValue = minValue;
                maxGlobalValue = maxValue;
            }

        });


        hMessage = new Handler() {

            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                    // if receive massage
                        double sensor1;
                        double sensor2;
                        double sensor3;
                        double average;
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);            // create string from bytes array
                        ourStringBuilder.append(strIncom);                                    // append string
                        int endOfLineIndex = ourStringBuilder.indexOf("\r\n");                // determine the end-of-line

                        if (endOfLineIndex > 0) {
                            String printStringFromArduino = ourStringBuilder.toString();
                            Log.d(TAG_BT, printStringFromArduino);
                            ourStringBuilder.delete(0, ourStringBuilder.length());

                            if (printStringFromArduino != null) {
                                try {
                                    JSONObject jsonObj = new JSONObject(printStringFromArduino);
                                    sensor1 = jsonObj.getDouble("val0");
                                    sensor2 = jsonObj.getDouble("val1");
                                    sensor3 = jsonObj.getDouble("val2");
                                    average = jsonObj.getDouble("val3");

                                    tempSensor1_TV.setText(sensor1 + "");
                                    tempSensor2_TV.setText(sensor2 + "");
                                    tempSensor3_TV.setText(sensor3 + "");
                                    tempAvg_TV.setText(average + "");

                                    heatMap.clearData();
                                    HeatMap.DataPoint point = new HeatMap.DataPoint(0.50f, 0.40f, sensor1); // Bottom
                                    heatMap.addData(point);
                                    HeatMap.DataPoint point2 = new HeatMap.DataPoint(0.27f, 0.16f, sensor2); // Left
                                    heatMap.addData(point2);
                                    HeatMap.DataPoint point3 = new HeatMap.DataPoint(0.76f, 0.16f, sensor3); // Right
                                    heatMap.addData(point3);
                                    heatMap.forceRefresh();

                                    // Checking user's max and min requirements
                                    if (average < minGlobalValue || average > maxGlobalValue) {
                                        Log.d("Hello", "Min: " + minGlobalValue.toString() + "Max: " + maxGlobalValue.toString());
                                        //Showing Notification and sound
                                        showNotificationAndSound();
                                        //Sending Email
                                        emailSending();
                                    }

                                } catch (final JSONException e) {
                                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                                }
                            }
                        }
                        break;
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();        // get Bluetooth adapter
        checkBTState();
    }

    // doing color gradient
    private static int doGradient(double value, double min, double max, int min_color, int max_color) {
        if (value >= max) {
            return max_color;
        }
        if (value <= min) {
            return min_color;
        }
        float[] hsvmin = new float[3];
        float[] hsvmax = new float[3];
        float frac = (float) ((value - min) / (max - min));
        Color.RGBToHSV(Color.red(min_color), Color.green(min_color), Color.blue(min_color), hsvmin);
        Color.RGBToHSV(Color.red(max_color), Color.green(max_color), Color.blue(max_color), hsvmax);
        float[] retval = new float[3];
        for (int i = 0; i < 3; i++) {
            retval[i] = interpolate(hsvmin[i], hsvmax[i], frac);
        }
        return Color.HSVToColor(retval);
    }

    private static float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

    private void showNotificationAndSound() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.MAGENTA);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Temperature is Abnormal")
                .setContentTitle("Smart-Shirt Title")
                .setContentText("Emergency! Please take necessary steps!")
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());

        // Play Sound
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void emailSending() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        i.putExtra(Intent.EXTRA_TEXT, "body of email");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.d(TAG_BT, "There is no email Client!");
        }

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    hMessage.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();        // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }
    }
}