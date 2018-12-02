package com.example.trojan52.mqttsmartdevicecontroller;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class AddDevice extends AppCompatActivity {
    HashMap<String, ArrayList<AvailableDeviceInfo>> devices;
    Spinner spnType;
    Spinner spnFeed;
    EditText devName;
    String selectedGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        Intent intent = getIntent();
        devices = (HashMap<String, ArrayList<AvailableDeviceInfo>>) intent.getSerializableExtra("availableDevices");
        spnType = (Spinner) findViewById(R.id.spnDeviceType);
        spnFeed  = (Spinner) findViewById(R.id.spnDeviceFeed);
        devName = (EditText) findViewById(R.id.editDeviceName);

        ArrayAdapter<String> adapterFeed = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getDevicesList());
        adapterFeed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this,
                R.array.deviceType_array, android.R.layout.simple_spinner_item);

        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnFeed.setAdapter(adapterFeed);
        spnFeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String feed = spnFeed.getSelectedItem().toString();
                ((EditText)findViewById(R.id.editDeviceName)).setText(getDeviceName(feed));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnType.setAdapter(adapterType);

        ((Button)findViewById(R.id.btnAddDevice)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spnFeed.getSelectedItem().toString().isEmpty() || spnType.getSelectedItem().toString().isEmpty())
                {
                    Toast t =  Toast.makeText(getApplicationContext(),"Не все поля заполнены.", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0 , 0);
                    t.show();
                } else {
                    addDeviceToFile(spnFeed.getSelectedItem().toString(), selectedGroup, devName.getText().toString(), spnType.getSelectedItem().toString());
                }
            }
        });

    }

    private void addDeviceToFile(String feed, String group, String name, String type) {
        File devFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + getApplicationContext().getString(R.string.devices_filename));
        JSONObject addObj = new JSONObject();
        try {
            addObj.put("feed", feed);
            addObj.put("group", group);
            addObj.put("name", name);
            addObj.put("type", type);
            if(devFile.isFile()) {
                FileInputStream inputStream = new FileInputStream(devFile);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }
                JSONObject jObj = new JSONObject(stringBuilder.toString());
                if(jObj.has("devices")) {
                    JSONArray devArr = jObj.getJSONArray("devices");
                    devArr.put(addObj);
                    jObj.put("devices", devArr);
                    devFile.delete();
                    devFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(devFile);
                    outputStream.write(jObj.toString().getBytes());
                    outputStream.close();
                    Toast t =  Toast.makeText(getApplicationContext(),"Устройство добавлено", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0 , 0);
                    t.show();
                } else {
                    JSONArray devArr = new JSONArray();
                    devArr.put(addObj);
                    jObj.put("devices", devArr);
                    devFile.delete();
                    devFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(devFile);
                    outputStream.write(jObj.toString().getBytes());
                    outputStream.close();
                    Toast t =  Toast.makeText(getApplicationContext(),"Устройство добавлено", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0 , 0);
                    t.show();
                }
            } else {
                JSONObject jObj = new JSONObject();
                JSONArray devArr = new JSONArray();
                devArr.put(addObj);
                jObj.put("devices", devArr);
                devFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(devFile);
                outputStream.write(jObj.toString().getBytes());
                outputStream.close();
                Toast t =  Toast.makeText(getApplicationContext(),"Устройство добавлено", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0 , 0);
                t.show();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getDeviceName(String feed) {
        for (String key : devices.keySet()) {
            ArrayList<AvailableDeviceInfo> vector= devices.get(key);
            for(AvailableDeviceInfo x : vector) {
                if(x.getFeed().equals(feed)) {
                    selectedGroup = key;
                    return x.getName();
                }
            }
        }
        return "";
    }

    private List<String> getDevicesList() {
        List<String> list = new ArrayList<>();
        for (String key : devices.keySet()) {
            ArrayList<AvailableDeviceInfo> vector= devices.get(key);
            for(AvailableDeviceInfo x : vector) {
                list.add(x.getFeed());
            }
        }
        return list;
    }
}
