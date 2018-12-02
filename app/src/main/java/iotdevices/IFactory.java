package iotdevices;

import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONObject;

public interface IFactory {
    ADevice create(JSONObject obj, LinearLayout lay);
}
