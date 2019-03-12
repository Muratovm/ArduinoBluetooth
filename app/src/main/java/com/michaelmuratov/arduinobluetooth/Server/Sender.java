package com.michaelmuratov.arduinobluetooth.Server;

import android.app.Activity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sender {

    private DateFormat df;
    private long currentDateTime;
    private Date currentDate;
    public boolean connected = true;

    String server_name = "http://142.1.200.140:10023/uploadData/";

    Activity activity;
    public Sender(Activity activity){
        this.activity = activity;
        currentDateTime = System.currentTimeMillis();
        currentDate = new Date(currentDateTime);
        df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss:SSSS");
    }

    public void send(String action_string) throws  IOException{
        HttpURLConnection conn = null;
        try{
            URL url = new URL(server_name);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);


            action_string = action_string.replace("[","");
            action_string = action_string.replace("]","");
            byte[] outputBytes = action_string.getBytes("UTF-8");

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.write(outputBytes);
            os.flush();
            os.close();

            InputStream in = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);

            String res = "";
            int inputStreamData = inputStreamReader.read();
            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                res += current;
            }
            final String finalRes = res;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, finalRes,Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void send(JSONObject action) throws IOException {
        String action_string = action.toString();
        send(action_string);
    }

    public JSONObject format_message(String... values) throws JSONException {
        currentDateTime = System.currentTimeMillis();
        currentDate = new Date(currentDateTime);
        JSONObject action = new JSONObject();
        JSONObject attributes = new JSONObject();
        for(int i =0; i < values.length; i+=2) {
            attributes.put(values[i], values[i + 1]);
        }
        action.put(df.format(currentDate),attributes);
        return action;
    }

    public JSONObject single_format(String value) throws JSONException {
        currentDateTime = System.currentTimeMillis();
        currentDate = new Date(currentDateTime);
        JSONObject action = new JSONObject();
        action.put(df.format(currentDate),value);
        return action;
    }
}
