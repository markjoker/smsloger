package com.iot.smsloger.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.iot.smsloger.Sms;
import com.iot.smsloger.SmsObserver;
import com.iot.smsloger.event.SmsEvent;
import com.iot.smsloger.OnStateChangeListener;

import de.greenrobot.event.EventBus;

public class SmsGuardService extends Service implements OnStateChangeListener
{
    private ContentResolver mContentResolver;
    
    private SmsObserver mSmsObserver;
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        mContentResolver = getContentResolver();
        mSmsObserver = new SmsObserver(this, mContentResolver, new Handler(), this);
        Uri smsUri = Uri.parse("content://sms");
        mContentResolver.registerContentObserver(smsUri, true, mSmsObserver);
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mContentResolver.unregisterContentObserver(mSmsObserver);
    }
    
    private void notifyDataChange(Sms sms)
    {
        EventBus.getDefault().post(new SmsEvent(sms.address, sms.date, sms.msg));
        LogService.startActionLog(this, sms.date, sms.msg);
    }
    
   
    @Override
    public void onSmsSent(Sms sms)
    {
        Toast.makeText(this, "Sent:" + sms.msg, Toast.LENGTH_SHORT).show();
        notifyDataChange(sms);
    }
    
    @Override
    public void onSmsReceived(Sms sms)
    {
        Toast.makeText(this, "Received:" + sms.msg, Toast.LENGTH_SHORT).show();
        notifyDataChange(sms);
    }
}
