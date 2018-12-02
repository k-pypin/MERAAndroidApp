package iotdevices;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.trojan52.mqttsmartdevicecontroller.MainActivity;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class SwitchDevice extends ADevice {
    private SwitchCompat switchWidget;
    private CardView card;
    public SwitchDevice(JSONObject obj, final LinearLayout layout){
        super(obj);
        setLayout(layout);
        card = new CardView(layout.getContext());
        switchWidget = new SwitchCompat(layout.getContext());
        switchWidget.setText(getName());
        switchWidget.setMinHeight(100);
        card.addView(switchWidget);
        switchWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("PUBLISH");
                MqttMessage message;
                if(switchWidget.isChecked()) {
                    message = new MqttMessage("ON".getBytes());
                    setValue("ON");
                } else {
                    message = new MqttMessage("OFF".getBytes());
                    setValue("OFF");
                }
                try {
                    getMqttClient().publish(getFeedBaseUrl() + getFeed(), message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void setID(int id) {
        switchWidget.setId(id);
    }

    @Override
    public int getID() {
        return switchWidget.getId();
    }


    @Override
    public void setValue(String val) {
        super.setValue(val);
        if(val.equals("ON")) {
            switchWidget.setChecked(true);
        } else {
            switchWidget.setChecked(false);
        }
    }

    @Override
    public View getView() {
        return switchWidget;
    }

    @Override
    public void addToLayout() {
        getLayout().addView(card);
    }

    @Override
    public String getType() {
        return "Переключаемое";
    }
}
