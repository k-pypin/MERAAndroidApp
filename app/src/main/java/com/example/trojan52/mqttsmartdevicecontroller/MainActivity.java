package com.example.trojan52.mqttsmartdevicecontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private ConnectionData cData;
    private MqttAndroidClient client;
    private IMqttToken subToken;
    private HashMap<String, ArrayList<AvailableDeviceInfo>> availableDevicesByGroup;
    private MenuItem addMenuItem;
    private ProgressBar circularProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences prefs = PreferenceManager
                                  .getDefaultSharedPreferences(getBaseContext());
        circularProgress = (ProgressBar) findViewById(R.id.progressBar);
        circularProgress.setVisibility(View.VISIBLE);

        cData = new ConnectionData(
                prefs,
                getApplicationContext().getString(R.string.connect_host_key),
                getApplicationContext().getString(R.string.connect_port_key),
                getApplicationContext().getString(R.string.connect_user_key),
                getApplicationContext().getString(R.string.connect_pass_key)
        );
        availableDevicesByGroup = new HashMap<String, ArrayList<AvailableDeviceInfo>>();
        if(cData.hasEmptyValues()) {
            Toast t =  Toast.makeText(MainActivity.this,"Заполните данные для подключения.", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0 , 0);
            t.show();
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else {
            initConnection();
        }

    }

    @Override
    protected void onRestart() {
        addMenuItem.setVisible(false);
        circularProgress.setVisibility(View.VISIBLE);
        //if(cData.valuesUpdated()) {
        cData.updateValues();
        if (cData.hasEmptyValues()) {
            Toast t =  Toast.makeText(MainActivity.this,"Заполните данные для подключения.", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0 , 0);
            t.show();
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else {
            initConnection();
        }
        //}
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        addMenuItem = menu.findItem(R.id.action_add);
        addMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                Intent intent = new Intent(getApplicationContext(), AddDevice.class);
                intent.putExtra("availableDevices", availableDevicesByGroup);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent intent1 = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void initConnection() {
        String clientId = MqttClient.generateClientId();
        client =  new MqttAndroidClient(this.getApplicationContext(),
                "tcp://"+ cData.getHost() + ":" + cData.getPort(), clientId);
        try {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(cData.getUserName());
                options.setPassword(cData.getPassword().toCharArray());
                IMqttToken token = client.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        System.out.println("CONNECTED");
                        String topic = cData.getUserName() + "/feeds/+";
                        int qos = 1;
                        try {
                            subToken = client.subscribe(topic, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    OnSuccessSubscription();
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken,
                                                      Throwable exception) {
                                    Toast t =  Toast.makeText(MainActivity.this,"Не удалось подписаться.\nПроверьте параметры подключения.", Toast.LENGTH_SHORT);
                                    t.setGravity(Gravity.CENTER, 0 , 0);
                                    t.show();

                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                        Toast t =  Toast.makeText(MainActivity.this,"Ошибка подключения.\nПроверьте параметры подключения.", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0 , 0);
                        t.show();

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

    }

    private void OnSuccessSubscription() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("t: " + topic + " m: " + message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://" + cData.getHost()  + "/api/v2/" + cData.getUserName() + "/feeds?X-AIO-Key=" + cData.getPassword(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String resp = new String(response);
                getDevicesFromJsonString(resp);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Toast t =  Toast.makeText(MainActivity.this,"Не удалось загрузить список устройств.\nПроверьте параметры подключения.", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0 , 0);
                t.show();
            }
        });
    }

    private void getDevicesFromJsonString(String jsonString) {
        try {
            JSONArray jArr = new JSONArray(jsonString);
            for (int i = 0; i < jArr.length(); ++i) {
                JSONObject jObj = new JSONObject(jArr.get(i).toString());
                String group = jObj.getJSONObject("group").getString("name");
                AvailableDeviceInfo devInfo = new AvailableDeviceInfo(
                                                  jObj.getString("name"),
                                                  jObj.getString("key"));
                if(availableDevicesByGroup.containsKey(group)) {
                    availableDevicesByGroup.get(group).add(devInfo);
                } else {
                    availableDevicesByGroup.put(group, new ArrayList<AvailableDeviceInfo>());
                    availableDevicesByGroup.get(group).add(devInfo);
                }
            }
            addMenuItem.setVisible(true);
            circularProgress.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateDevices() {

    }


}
