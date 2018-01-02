package com.example.administrator.weatherpro;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity{
    HttpURLConnection httpConn = null;
    InputStream din =null;

    Button find = null;
    EditText value = null;
    TextView tv_show = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气查询Json解析");
        find = (Button)findViewById(R.id.find);
        value = (EditText)findViewById(R.id.value);
        value.setText("广州");//初始化，给个初值，方便测试
       tv_show = (TextView)findViewById(R.id.tv_show);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_show.setText("");//清空数据
                Toast.makeText(MainActivity.this, "正在查询天气信息", Toast.LENGTH_SHORT).show();
                GetJson gd = new GetJson(value.getText().toString());//调用线程类创建的对象
                gd.start();//运行线程对象


            }
        });

    }



    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 123:
                        showData((String)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private  void showData(String jData){
        tv_show.setText(jData);
        //这里我直接显示json数据，没解析。解析的方法，请参考教材或网上相应的代码
        try {
            JSONObject jobj = new JSONObject(jData);
            JSONObject weather = jobj.getJSONObject("data");
            StringBuffer wbf = new StringBuffer();
            wbf.append("天气提示："+weather.getString("ganmao")+"\n");
            wbf.append("当前温度："+weather.getString("wendu")+"\n");
            JSONArray jary = weather.getJSONArray("forecast");
            for(int i=0;i<jary.length();i++){
                JSONObject pobj = (JSONObject)jary.opt(i);
                wbf.append("日期："+pobj.getString("date")+"\n");
                wbf.append("最高温："+pobj.getString("high")+"\n");
                wbf.append("最低温度："+pobj.getString("low")+"\n");
                wbf.append("风向："+pobj.getString("fengxiang")+"\n");
                String fengli=pobj.getString("fengli");
                int ep=fengli.indexOf("]]>");
                fengli=fengli.substring(9,ep);
                wbf.append("风力："+fengli+"\n");
            }
            tv_show.setText(wbf.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    class GetJson extends Thread{

        private String urlstr =  "http://wthrcdn.etouch.cn/weather_mini?city=";
        public GetJson(String cityname){
            try{
                urlstr = urlstr+URLEncoder.encode(cityname,"UTF-8");

            }catch (Exception ee){

            }
        }
        @Override
        public void run() {
            try {
                URL url = new URL(urlstr);
                httpConn = (HttpURLConnection)url.openConnection();
                httpConn.setRequestMethod("GET");
                din = httpConn.getInputStream();
                InputStreamReader in = new InputStreamReader(din);
                BufferedReader buffer = new BufferedReader(in);
                StringBuffer sbf = new StringBuffer();
                String line = null;
                while( (line=buffer.readLine())!=null) {
                    sbf.append(line);
                }
                Message msg = new Message();
                msg.obj = sbf.toString();
                msg.what = 123;
                handler.sendMessage(msg);
                Looper.prepare(); //在线程中调用Toast，要使用此方法，这里纯粹演示用:)
                Toast.makeText(MainActivity.this,"获取数据成功",Toast.LENGTH_LONG).show();
                Looper.loop(); //在线程中调用Toast，要使用此方法



            }catch (Exception ee){
                Looper.prepare(); //在线程中调用Toast，要使用此方法
                Toast.makeText(MainActivity.this,"获取数据失败，网络连接失败或输入有误",Toast.LENGTH_LONG).show();
                Looper.loop(); //在线程中调用Toast，要使用此方法
                ee.printStackTrace();
            }finally {
                try{
                    httpConn.disconnect();
                    din.close();

                }catch (Exception ee){
                    ee.printStackTrace();
                }
            }
        }
    }

}
