package com.example.trojan52.mqttsmartdevicecontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {
    private ConnectionData cData;
    private Button btnSub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        SharedPreferences prefs = PreferenceManager
                                  .getDefaultSharedPreferences(getBaseContext());
        cData = new ConnectionData(
                prefs,
                getApplicationContext().getString(R.string.connect_host_key),
                getApplicationContext().getString(R.string.connect_port_key),
                getApplicationContext().getString(R.string.connect_user_key),
                getApplicationContext().getString(R.string.connect_pass_key)
        );
        btnSub = (Button)findViewById(R.id.connect);





        if(cData.hasEmptyValues()) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else {
//            String clientId = MqttClient.generateClientId();
//            final MqttAndroidClient client =
//                    new MqttAndroidClient(this.getApplicationContext(), "tcp://"+ cData.getHost() + ":" + cData.getPort(),
//                            clientId);
//
//
//
//            try {
//                MqttConnectOptions options = new MqttConnectOptions();
//                options.setUserName(cData.getUserName());
//                options.setPassword(cData.getPassword().toCharArray());
//                IMqttToken token = client.connect(options);
//                token.setActionCallback(new IMqttActionListener() {
//                    @Override
//                    public void onSuccess(IMqttToken asyncActionToken) {
//                        // We are connected
//                        System.out.println("CONNECTED");
//                    }
//
//                    @Override
//                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                        // Something went wrong e.g. connection timeout or firewall problems
//                        //Log.d(TAG, "onFailure");
//                        System.out.println("NOT - CONNECTED");
//
//                    }
//                });
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }

//            btnSub.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String topic = cData.getUserName() + "feeds/kitchen.lamp-on-the-kitchen-1";
//                    int qos = 1;
//                    try {
//                        final IMqttToken subToken = client.subscribe(topic, qos);
//                        subToken.setActionCallback(new IMqttActionListener() {
//                            @Override
//                            public void onSuccess(IMqttToken asyncActionToken) {
//                                System.out.println("SUBSCRIBED");
//                            }
//
//                            @Override
//                            public void onFailure(IMqttToken asyncActionToken,
//                                                  Throwable exception) {
//                                System.out.println("NOT - SUBSCRIBED");
//
//                            }
//                        });
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
        }

    }

    @Override
    protected void onRestart() {
        cData.updateValues();
        if(cData.hasEmptyValues()) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }
}
