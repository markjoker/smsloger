package com.iot.smsloger;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.Date;

/**
 * Created by MaJian on 2015/10/12.
 */
public class SmsObserver extends ContentObserver
{
    private static final Uri SMS_URI = Uri.parse("content://sms/");
    
    private static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
    
    private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
    
    private static final String PROTOCOL_COLUM_NAME = "protocol";
    
    private static final String SMS_ORDER = "date DESC";
    
    private static final String ADDRESS_COLUMN_NAME = "address";
    
    private static final String DATE_COLUMN_NAME = "date";
    
    private static final String BODY_COLUMN_NAME = "body";
    
    private static final String TYPE_COLUMN_NAME = "type";
    
    private static final String ID_COLUMN_NAME = "_id";
    
    private static final int SMS_MAX_AGE_MILLIS = 5000;
    
    private ContentResolver mContentResolver;
    
    private OnStateChangeListener mListener;
    
    private Context mCtx;
    
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SmsObserver(Context context, ContentResolver contentResolver, Handler handler, OnStateChangeListener 
        listener)
    {
        super(handler);
        mCtx = context;
        mContentResolver = contentResolver;
        mListener = listener;
    }
    
    @Override
    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Log.d("1021", "onChange:" + selfChange);
        Cursor cursor = null;
        cursor = mContentResolver.query(SMS_URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst())
        {
            Sms sms = processSms(cursor);
            if (mListener != null && sms != null && isNewMsg(sms))
            {
                if (sms.type == Sms.TYPE_SENT)
                {
                    mListener.onSmsSent(sms);
                }
                else if (sms.type == Sms.TYPE_RECEIVED)
                {
                    mListener.onSmsReceived(sms);
                }
            }
        }
    }
    
    private boolean isNewMsg(Sms sms)
    {
        SharedPreferences preferences = mCtx.getSharedPreferences("sms", Context.MODE_PRIVATE);
        int lastId = preferences.getInt("last_sms_id", -1);
        preferences.edit().putInt("last_sms_id",sms.id).apply();
        if (lastId == -1 && new Date().getTime() - sms.date < SMS_MAX_AGE_MILLIS)
        {
            return true;
        }
        if(lastId != -1 && sms.id > lastId)
        {
            return true;
        }
        return false;
    }
    
    private Sms processSms(Cursor cursor)
    {
        String protocal = cursor.getString(cursor.getColumnIndex(PROTOCOL_COLUM_NAME));
        cursor.close();
        Cursor smsCursor =
            mContentResolver.query(protocal == null ? SMS_SENT_URI : SMS_INBOX_URI, null, null, null, SMS_ORDER);
        if (smsCursor == null || smsCursor.getCount() <= 0 || !smsCursor.moveToNext())
        {
            return null;
        }
        String address = smsCursor.getString(smsCursor.getColumnIndex(ADDRESS_COLUMN_NAME));
        long date = smsCursor.getLong(smsCursor.getColumnIndex(DATE_COLUMN_NAME));
        String msg = smsCursor.getString(smsCursor.getColumnIndex(BODY_COLUMN_NAME));
        int type = smsCursor.getInt(smsCursor.getColumnIndex(TYPE_COLUMN_NAME));
        int id = smsCursor.getInt(smsCursor.getColumnIndex(ID_COLUMN_NAME));
        smsCursor.close();
        
        return new Sms(id, address, date, msg, type);
    }
}
