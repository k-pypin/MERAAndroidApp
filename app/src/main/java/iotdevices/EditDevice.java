package iotdevices;

import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class EditDevice extends ADevice {
    private EditText textEdit;
    private ImageButton submitBtn;
    private CardView card;
    private LinearLayout hLay;
    public EditDevice(JSONObject jObj, LinearLayout layout) {
        super(jObj);
        setLayout(layout);
        textEdit = new EditText(layout.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        textEdit.setLayoutParams(lp);
        textEdit.setMinHeight(100);
        textEdit.setHint(getName());
        textEdit.setHintTextColor(Color.GRAY);
        submitBtn = new ImageButton(layout.getContext());
        submitBtn.setBackgroundColor(Color.parseColor("#00BCD4"));
        submitBtn.setImageResource(android.R.drawable.ic_menu_send);
        submitBtn.setMinimumHeight(100);
        hLay = new LinearLayout(layout.getContext());
        hLay.setMinimumHeight(100);
        hLay.addView(textEdit);
        hLay.addView(submitBtn);
        card = new CardView(layout.getContext());
        card.addView(hLay);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MqttMessage message = new MqttMessage(textEdit.getText().toString().getBytes());
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
        submitBtn.setId(id);
    }

    @Override
    public int getID() {
        return submitBtn.getId();
    }

    @Override
    public View getView() {
        return submitBtn;
    }

    @Override
    public void addToLayout() {
        getLayout().addView(card);
    }

    @Override
    public String getType() {
        return "Редактируемое";
    }
}
