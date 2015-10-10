package com.iot.smsloger.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.iot.smsloger.R;
import com.iot.smsloger.event.SmsEvent;
import com.iot.smsloger.service.DMService;
import com.iot.smsloger.utils.Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by MaJian on 2015/10/8.
 */
public class MainActivity extends Activity
{
    private ListView mConsole;
    
    private SimpleDateFormat mSdf;
    
    private SharedPreferences mSharedPreferences;
    
    private List<Map<String, String>> mData;
    
    private SimpleAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        mSharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mConsole = (ListView)findViewById(R.id.lv_console);
        mData = new ArrayList<>();
        mAdapter =
            new SimpleAdapter(this, mData, R.layout.layout_item, new String[]{"content"}, new int[]{R.id.tv_content});
        mConsole.setAdapter(mAdapter);
        initData();
        mAdapter.notifyDataSetChanged();
        startService(new Intent(this, DMService.class));
    }
    
    private void initData()
    {
        File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM", "sms.txt");
        mData.clear();
        if (logFile.exists())
        {
            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader(new FileReader(logFile));
                String line = null;
                Map<String, String> map = null;
                while ((line = reader.readLine()) != null)
                {
                    map = new HashMap<>();
                    map.put("content", line);
                    mData.add(map);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !Constant.IS_LISTEN_SMS)
        {
            changeSmsSetting();
        }
        else
        {
            findViewById(R.id.tv_set_default).setVisibility(View.GONE);
        }
        
    }
    
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void changeSmsSetting()
    {
        final String storeSmsPkg = mSharedPreferences.getString("storeSmsPkg", null);
        final String myPackageName = getPackageName();
        Button setBtn = (Button)findViewById(R.id.tv_set_default);
        setBtn.setVisibility(View.VISIBLE);
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
        if (!defaultSmsPackage.equals(myPackageName))
        {
            mSharedPreferences.edit().putString("storeSmsPkg", defaultSmsPackage).commit();
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
            setBtn.setText("设置App为默认短信应用");
            
            // Set up a button that allows the user to change the default SMS app
            setBtn.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                    startActivity(intent);
                }
            });
        }
        else
        {
            setBtn.setText("恢复默认短信应用");
            setBtn.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, storeSmsPkg);
                    startActivity(intent);
                }
            });
        }
    }
    
    public void onEvent(SmsEvent event)
    {
        Map<String, String> map = new HashMap<>();
        map.put("content", mSdf.format(new Date(event.timeStamp)) + "#" + event.message);
        mData.add(map);
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
