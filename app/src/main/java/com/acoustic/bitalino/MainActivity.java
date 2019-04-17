package com.acoustic.bitalino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;

import java.io.IOException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static boolean INSTANT_UPLOAD = false;
    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int SAMPLE_RATE = 1000;
    private static String WEB_API_URL = "https://teststres.herokuapp.com";

    private TextView tvLog;
    private Switch switchRecording;
    private Button btnStartTest;
    private EditText txtClientId;

    private Boolean testInitiated = false;

    WebService webService = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(WEB_API_URL)
            .build()
            .create(WebService.class);

    private String recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLog = findViewById(R.id.log);

        pingWebService();

        switchRecording = findViewById(R.id.recording);
        switchRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchRecording.isChecked() && !testInitiated) {
                    new RecordAsyncTask().execute();

                    btnStartTest.setEnabled(true);
                } else if (!switchRecording.isChecked()) {
                    btnStartTest.setEnabled(false);
                    INSTANT_UPLOAD = false;
                }
            }
        });

        txtClientId = findViewById(R.id.client_id);

        btnStartTest = findViewById(R.id.start_test);
        btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webService.startTest(new TestInfo(txtClientId.getText().toString(), recordId, TestType.IMAGE.name())).enqueue(new Callback<SimpleMessage>() {
                    @Override
                    public void onResponse(Call<SimpleMessage> call, Response<SimpleMessage> response) {
                        if (response.isSuccessful()) {
                            btnStartTest.setEnabled(false);
                            INSTANT_UPLOAD = true;

                            appendLog("\nStarted test!");

                        } else {
                            appendLog("\nStart test failed, check client id!");
                            Log.e(TAG, "Start test failed. Response: " + response.message());

                            Toast.makeText(getApplicationContext(), "Could not start test!\nTry Again...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SimpleMessage> call, Throwable t) {
                        appendLog("\nStart test failed." + t.getMessage());
                        Log.e(TAG, "Start test failed", t);

                        Toast.makeText(getApplicationContext(), "Could not start test!\nTry Again...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void appendLog(final String message) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.append(message);
            }
        });
    }

    private void pingWebService() {
        webService.ping().enqueue(new Callback<SimpleMessage>() {
            @Override
            public void onResponse(Call<SimpleMessage> call, Response<SimpleMessage> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Ping web service successful." + response.body().getMessage());
                } else {
                    Log.e(TAG, "Ping web service failed! Response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SimpleMessage> call, Throwable t) {
                tvLog.append("\n".concat("Could not ping web service!"));
                Log.e(TAG, "Could not ping web service!", t);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class RecordAsyncTask extends AsyncTask<Void, String, Void> {
        private TextView tvLog = findViewById(R.id.log);
        private BluetoothDevice dev = null;
        private BluetoothSocket sock = null;
        private BITalinoDevice bitalino;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Let's get the remote Bluetooth device
                final String remoteDevice = "20:18:06:13:02:49";

                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                dev = btAdapter.getRemoteDevice(remoteDevice);

                /*
                 * Establish Bluetooth connection
                 *
                 * Because discovery is a heavyweight procedure for the Bluetooth adapter,
                 * this method should always be called before attempting to connect to a
                 * remote device with connect(). Discovery is not managed by the Activity,
                 * but is run as a system service, so an application should always call
                 * cancel discovery even if it did not directly request a discovery, just to
                 * be sure. If Bluetooth state is not STATE_ON, this API will return false.
                 *
                 * see
                 * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
                 * .html#cancelDiscovery()
                 */
                Log.d(TAG, "Stopping Bluetooth discovery.");
                btAdapter.cancelDiscovery();

                sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                sock.connect();
                testInitiated = true;

                bitalino = new BITalinoDevice(SAMPLE_RATE, new int[]{0, 1, 2, 3, 4, 5});
                publishProgressMessage("Connecting to BITalino [" + remoteDevice + "]..");
                bitalino.open(sock.getInputStream(), sock.getOutputStream());
                publishProgressMessage("Connected.");

                // get BITalino version
                publishProgressMessage("Version: " + bitalino.version());

                // start acquisition on predefined analog channels
                bitalino.start();

                startReading();

                // trigger digital outputs
                // int[] digital = { 1, 1, 1, 1 };
                // device.trigger(digital);
            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
                publishProgressMessage("An error occured: " + e.getMessage());

                switchRecording.setChecked(false);
                testInitiated = false;
            }

            return null;
        }

        private void startReading() {
            BitalinoDeviceInfo deviceInfo = new BitalinoDeviceInfo(SAMPLE_RATE);

            try {
                Response<ServerResponse<CreateRecordResponse>> response = webService.createRecord(deviceInfo).execute();

                if (response.isSuccessful()) {
                    recordId = response.body().getData().getId();
                    publishProgressMessage("Created record, id: " + recordId);

                    readSamples(recordId);
                } else {
                    Toast.makeText(getApplicationContext(), "Could not create record!\nTry Again...", Toast.LENGTH_SHORT).show();

                    Log.e(TAG, "Could not create record! Response:" + response.message());
                    cancel(true);
                }
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Could not create record!\nTry Again...", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Could not create record!", e);
                cancel(true);
            }
        }

        private void readSamples(String recordId) {
            publishProgressMessage("Started reading.");

            // read until task is stopped
            while (switchRecording.isChecked()) {
                final int numberOfSamplesToRead = 1000;
                BITalinoFrame[] frames;
                try {
                    frames = bitalino.read(numberOfSamplesToRead);
                } catch (BITalinoException e) {
                    publishProgressMessage("Exception reading from device!");
                    break;
                }

                //TODO: BULK UPLOAD

                if (INSTANT_UPLOAD) {
                    // prepare reading for upload
                    BITalinoReading reading = new BITalinoReading();
                    reading.setTimestamp(System.currentTimeMillis());
                    reading.setFrames(frames);

                    // upload reading
                    webService.uploadReading(recordId, reading).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                publishProgressMessage("Could not upload partial reading!");
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            publishProgressMessage("Could not upload partial reading!");
                        }
                    });
                }
            }

            publishProgressMessage("Stopped reading.");

            try {
                Response<SimpleMessage> response = webService.stopRecord(recordId).execute();

                if (response.isSuccessful()) {
                    publishProgressMessage(response.body().getMessage());
                    cancel(true);
                } else {
                    //TODO
                    publishProgressMessage("Could not finalize record!");
                }

            } catch (IOException e) {
                //TODO
                publishProgressMessage("Could not finalize record!");
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tvLog.append("\n".concat(values[0]));
        }

        @Override
        protected void onCancelled() {
            INSTANT_UPLOAD = false;
            // stop acquisition and close bluetooth connection
            try {
                bitalino.stop();

                publishProgressMessage("BITalino is stopped");

                sock.close();

                testInitiated = false;
                publishProgressMessage("And we're done! :-)");
            } catch (Exception e) {
                Log.e(TAG, "Error while stopping BITalino.", e);
            }
        }

        void publishProgressMessage(final String message) {
            new Thread() {
                @Override
                public void run() {
                    publishProgress(message);
                }
            }.start();
        }
    }

}