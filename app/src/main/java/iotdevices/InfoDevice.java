package iotdevices;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

public class InfoDevice extends ADevice {
    private TextView textName;
    private TextView textValue;
    private CardView card;
    private LinearLayout hLay;
    public InfoDevice(JSONObject jObj, LinearLayout layout) {
        super(jObj);
        setLayout(layout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        textName = new TextView(layout.getContext());
        textName.setMinHeight(100);
        textName.setLayoutParams(lp);
        textName.setTextColor(Color.BLACK);
        textValue = new TextView(layout.getContext());
        textValue.setMinHeight(100);
        textValue.setTextColor(Color.BLACK);
        textName.setText(getName());
        textValue.setText(getValue());
        hLay = new LinearLayout(layout.getContext());
        hLay.setMinimumHeight(100);
        hLay.addView(textName);
        hLay.addView(textValue);
        card = new CardView(layout.getContext());
        card.addView(hLay);
    }

    @Override
    public void setID(int id) {
        hLay.setId(id);
    }

    @Override
    public int getID() {
        return hLay.getId();
    }

    @Override
    public void setValue(String val) {
        super.setValue(val);
        textValue.setText(val);
    }

    @Override
    public View getView() {
        return hLay;
    }

    @Override
    public void addToLayout() {
        getLayout().addView(card);
    }

    @Override
    public String getType() {
        return "Иноформационное";
    }
}
