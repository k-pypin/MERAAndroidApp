package com.example.trojan52.mqttsmartdevicecontroller;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

import iotdevices.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_PERMISSION = 120;
    private ConnectionData cData;
    private MqttAndroidClient client;
    private IMqttToken subToken;
    private HashMap<String, ArrayList<AvailableDeviceInfo>> availableDevicesByGroup;
    private MenuItem addMenuItem;
    private ProgressBar circularProgress;
    private ArrayList<ADevice> devices;
    private AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        reqPermissions();
        SharedPreferences prefs = PreferenceManager
                                  .getDefaultSharedPreferences(getBaseContext());
        devices = new ArrayList<>();
        circularProgress = (ProgressBar) findViewById(R.id.progressBar);
        circularProgress.setVisibility(View.VISIBLE);
        builder = new AlertDialog.Builder(this);
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
                for(ADevice x : devices) {
                    if((x.getFeedBaseUrl() + x .getFeed()).equals(topic))
                        x.setValue(message.toString());
                }
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Изменено состояние устройства")
                                .setContentText(topic + " теперь имеет значение: " + message.toString());

                Notification notification = builder.build();

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);
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
        availableDevicesByGroup.clear();
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
            UpdateDevices();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateDevices() {
        File devFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + getApplicationContext().getString(R.string.devices_filename));
        JSONObject jObj;
        DeviceFactory factory = new DeviceFactory();
        int id = 0;
        try {
            if(devFile.isFile()) {
                FileInputStream inputStream = new FileInputStream(devFile);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }
                jObj = new JSONObject(stringBuilder.toString());
                if(jObj.has("devices")) {
                    JSONArray devArr = jObj.getJSONArray("devices");
                    devices.clear();
                    for(int i = 0; i < devArr.length(); ++i) {
                        JSONObject curObj = new JSONObject(devArr.get(i).toString());
                        if(curObj.has("group") && curObj.has("feed")) {
                            if(availableDevicesByGroup.containsKey(curObj.getString("group"))) {
                                ArrayList<AvailableDeviceInfo> devs = availableDevicesByGroup.get(curObj.getString("group"));
                                boolean finded = false;
                                String feed = curObj.getString("feed");
                                for(AvailableDeviceInfo x : devs) {
                                    if(x.getFeed().equals(feed)) {
                                        finded = true;
                                        break;
                                    }
                                }
                                if(finded) {
                                    ADevice dev = factory.create(curObj, (LinearLayout)findViewById(R.id.scrollable_vlayout));
                                    if(dev != null) {
                                        dev.setMqttClient(client);
                                        dev.setFeedBaseUrl(cData.getUserName() + "/feeds/");
                                        dev.getLastValueFromUrl("http://" + cData.getHost() + "/api/v2/" + cData.getUserName() + "/feeds/" + dev.getFeed() + "/data/retain/?X-AIO-Key=" + cData.getPassword());
                                        dev.setID(id);
                                        devices.add(dev);

                                        dev.getView().setOnLongClickListener((View v)-> {
                                            builder.setMessage("Удалить устройство?")
                                                    .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int i) {
                                                            deleteDeviceById(v.getId());
                                                            UpdateDevices();
                                                        }
                                                    })
                                                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                        }
                                                    });
                                            builder.show();
                                            return false;
                                        });
                                    }
                                }
                            }
                        }
                        ++id;
                    }
                }
                addDevicesToScreen();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addDevicesToScreen() {
        LinearLayout layout = (LinearLayout)findViewById(R.id.scrollable_vlayout);
        layout.removeAllViews();
        for(ADevice x : devices) {
            x.addToLayout();
        }
    }

    private boolean deleteDeviceById(int id) {
        File devFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + getApplicationContext().getString(R.string.devices_filename));
        JSONObject jObj;
        if(devFile.isFile()) {
            try {
                FileInputStream inputStream = new FileInputStream(devFile);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                jObj = new JSONObject(stringBuilder.toString());

                if(jObj.has("devices")) {
                    JSONArray devArr = jObj.getJSONArray("devices");
                    if(id < devArr.length()) {
                        JSONArray newArr = new JSONArray();
                        for(int i = 0; i < devArr.length(); ++i) {
                            if( i != id)
                                newArr.put(devArr.getJSONObject(i));
                        }
                        jObj.put("devices", newArr);
                        devFile.delete();
                        devFile.createNewFile();
                        FileOutputStream outputStream = new FileOutputStream(devFile);
                        outputStream.write(jObj.toString().getBytes());
                        outputStream.close();
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (Exception e ) {
                e.printStackTrace();
            }

        } else {
            return false;
        }
        return false;
    }

    public void reqPermissions() {
        int ReqEX = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ReqEX != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        if(requestCode == REQ_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("OK");
        } else {
            System.out.print("NO");
        }
    }

}


