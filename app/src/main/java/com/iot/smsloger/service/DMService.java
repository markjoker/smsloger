package com.iot.smsloger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.iot.smsloger.event.SmsEvent;
import com.iot.smsloger.utils.Constant;
import com.tuenti.smsradar.Sms;
import com.tuenti.smsradar.SmsListener;
import com.tuenti.smsradar.SmsRadar;

import de.greenrobot.event.EventBus;

public class DMService extends Service
{
    private static final int KEEP_ALIVE_INTERVAL = 60 * 1000;
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        SmsRadar.initializeSmsRadarService(this, new SmsListener()
        {
            @Override
            public void onSmsSent(Sms sms)
            {
                Toast.makeText(DMService.this, sms.getMsg(), Toast.LENGTH_SHORT).show();
                
            }
        
            @Override
            public void onSmsReceived(Sms sms)
            {
                Toast.makeText(DMService.this, sms.getMsg(), Toast.LENGTH_SHORT).show();
                long time = Long.parseLong(sms.getDate());
                if(Constant.IS_LISTEN_SMS)
                {
                    EventBus.getDefault().post(new SmsEvent(sms.getAddress(), time, sms.getMsg()));
                    LogService.startActionLog(DMService.this, time, sms.getMsg());
                }
            }
        });
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        startKeepAlives();
        return START_STICKY;
    }
    
    
    private void startKeepAlives()
    {
        Intent i = new Intent();
        i.setClass(this, DMService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + KEEP_ALIVE_INTERVAL,
            KEEP_ALIVE_INTERVAL,
            pi);
    }
    
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Intent i = new Intent(this, DMService.class);
        startService(i);
        SmsRadar.stopSmsRadarService(this);
    }
}
