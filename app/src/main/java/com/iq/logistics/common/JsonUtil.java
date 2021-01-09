package com.iq.logistics.common;
import com.iq.logistics.model.PackagesInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    public static String toNumbersOnlyJSon(PackagesInfo packageInfo) {
        JSONObject jsonObj = new JSONObject();

        try {
            // Here we convert Java Object to JSON
            jsonObj.put("LOCKER", packageInfo.getLocker()); // Set the first name/pair
            jsonObj.put("TRACKING", packageInfo.getTracking());
            jsonObj.put("DESCRIPTION", packageInfo.getDescription());

        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }

        return jsonObj.toString();
    }

    public static String toPackageJSon(PackagesInfo packageinfo) {
        JSONObject obj = null;
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < packageinfo.getBase64img().size(); i++) {
            obj = new JSONObject();
            try {
                // Here we convert Java Object to JSON
                String title = String.format("PICTURE%d", i+1);
                obj.put(title, packageinfo.getBase64img().get(i));
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }

            jsonArray.put(obj);
        }

        JSONObject finalobj = new JSONObject();
        try {
            finalobj.put("TRACKING", packageinfo.getTracking());
            finalobj.put("LOCKER", packageinfo.getLocker()); // Set the first name/pair
            finalobj.put("DESCRIPTION", packageinfo.getDescription());
            finalobj.put("BASE64STR", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return finalobj.toString();
    }
}
