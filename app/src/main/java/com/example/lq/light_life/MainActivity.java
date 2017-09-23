package com.example.lq.light_life;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int UPDATE_TEXT = 1;

    TextView add_time,trends,username,trendsData;

    String a,b,c,title,content,category;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE_TEXT :
                    add_time.setText(a);
                    trends.setText(b);
                    username.setText(c);
                    trendsData.setText("动态：\n"+category+"\n"+title+"\n"+content);
                    break;
                default :
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendRequest = (Button) findViewById(R.id.send_request);
        add_time = (TextView) findViewById(R.id.add_time);
        add_time.setTextIsSelectable(true);
        trends = (TextView) findViewById(R.id.trends);
        trends.setTextIsSelectable(true);
        username = (TextView) findViewById(R.id.username);
        username.setTextIsSelectable(true);
        trendsData = (TextView) findViewById(R.id.trendsData);
        trendsData.setTextIsSelectable(true);
        sendRequest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_request) {
            HttpUtil httpUtil = new HttpUtil();
            httpUtil.sendOKHttpRequest("http://192.168.1.110:5000/api/users", new okhttp3.Callback(){
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //得到服务器返回的具体内容
                    String responseData = response.body().string();
                    parseJSONWithJSONObject(responseData);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    //这里对异常情况进行处理
                    //Toast.makeText(MainActivity.this, "网络异常！", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void parseJSONWithJSONObject(String jsonData) {
        try {
            HttpUtil httpUtil = new HttpUtil();
            JSONObject obj = new JSONObject(jsonData);
            JSONArray jsonArray = obj.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                a = "添加时间："+jsonObject.getString("add_time");
                httpUtil.sendOKHttpRequest(jsonObject.getString("trends"),new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String trendsData = response.body().string();
                        try {
                            JSONObject obj = new JSONObject(trendsData);
                            JSONArray jsonArray = obj.getJSONArray("trends");
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                category = jsonObject.getString("category");
                                title = jsonObject.getString("title");
                                content = jsonObject.getString("content");
                                Log.d("MainActivity", "类别：" + category);
                                Log.d("MainActivity", "标题：" + title);
                                Log.d("MainActivity", "内容：" + content);
                                b = category+"\n"+title+"\n"+content;
                                Log.d("MainActivity", "动态：" + b);
                                Message message = new Message();
                                message.what = UPDATE_TEXT;
                                handler.sendMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
                c = "用户名："+jsonObject.getString("username");
                Log.d("MainActivity", "添加时间：" + a);
                Log.d("MainActivity", "动态url：" + b);
                Log.d("MainActivity", "用户名：" + c);
                Message message = new Message();
                message.what = UPDATE_TEXT;
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
