package iotdevices;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.trojan52.mqttsmartdevicecontroller.MainActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public abstract class ADevice implements IDevice {
    private String Name;
    private String Group;
    private String Feed;
    private String Value;
    private MqttAndroidClient mqttclient;
    private String BaseUrl;
    private LinearLayout layout;
    public ADevice(JSONObject obj) {
        fromJsonObject(obj);
    }

    public abstract String getType();

    @Override
    public String getValue()  {
        return Value;
    }

    @Override
    public void setValue(String new_value) {
        Value = new_value;
    }


    public String getName()  {
        return Name;
    }

    public String getGroup() {
        return Group;
    }

    public String getFeed() {
        return Feed;
    }

    public void fromJsonObject(JSONObject obj) {
        try {
            Name = obj.getString("name");
            Group = obj.getString("group");
            Feed = obj.getString("feed");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public abstract void setID(int id);

    public abstract int getID();

    public JSONObject toJsonObject() {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("name", getName());
            jObj.put("group", getGroup());
            jObj.put("feed", getFeed());
            jObj.put("type", getType());
            return jObj;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setMqttClient(MqttAndroidClient client) {
        mqttclient = client;
    }

    public MqttAndroidClient getMqttClient() {
        return mqttclient;
    }

    public void setFeedBaseUrl(String url) {
        BaseUrl = url;
    }

    public String getFeedBaseUrl() {
        return BaseUrl;
    }

    public void getLastValueFromUrl(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String resp = new String(response);
                setValue(resp.split(",")[0]);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

            }
        });
    }

    public abstract View getView();

    public void setLayout(LinearLayout lay) {
        layout = lay;
    }

    public LinearLayout getLayout() {
        return layout;
    }

    public abstract void addToLayout();
}
