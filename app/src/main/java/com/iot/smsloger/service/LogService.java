package com.iot.smsloger.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogService extends IntentService
{
    private static final String ACTION_LOG_FILE = "com.iot.smsloger.action.LOG_FILE";
    
    private static final String TIME = "com.iot.smsloger.extra.TIME";
    
    private static final String MESSAGE = "com.iot.smsloger.extra.MESSAGE";
    
    private SimpleDateFormat mSdf;
    
    public static void startActionLog(Context context, long time, String msg)
    {
        Intent intent = new Intent(context, LogService.class);
        intent.setAction(ACTION_LOG_FILE);
        intent.putExtra(TIME, time);
        intent.putExtra(MESSAGE, msg);
        context.startService(intent);
    }
    
    public LogService()
    {
        super("LogService");
        mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();
            if (ACTION_LOG_FILE.equals(action))
            {
                final long param1 = intent.getLongExtra(TIME, 0);
                final String param2 = intent.getStringExtra(MESSAGE);
                handleActionLog(param1, param2);
            }
        }
    }
    
    private void handleActionLog(long time, String msg)
    {
        File folder = new File(Environment.getExternalStorageDirectory(), "DCIM");
        if (!folder.exists() || folder.isFile())
        {
            folder.mkdirs();
        }
        File logFile = new File(folder, "sms.txt");
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            out.println(mSdf.format(new Date(time)) + "#" + msg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }
}
