package iotdevices;

import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONObject;

public class DeviceFactory implements IFactory {
    @Override
    public ADevice create(JSONObject jObj, LinearLayout layout) {
        try {
            if (jObj.has("type")) {
                switch (jObj.getString("type")) {
                    case "Переключаемое":
                        return new SwitchDevice(jObj, layout);
                    case "Редактируемое":
                        return new EditDevice(jObj,layout);
                    case "Информационное":
                        return new InfoDevice(jObj, layout);
                    default:
                        return null;
                }



            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
