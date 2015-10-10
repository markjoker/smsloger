package com.iot.smsloger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.iot.smsloger.event.SmsEvent;
import com.iot.smsloger.service.LogService;
import com.iot.smsloger.utils.Constant;

import de.greenrobot.event.EventBus;

public class SmsReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d("1021","onReceive:" + action);
        if(Constant.IS_LISTEN_SMS)
        {
            return;
        }
        if (Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action) || Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msgBody;
            String msgFrom;
            long timeStamp;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msgFrom = msgs[i].getOriginatingAddress();
                        timeStamp = msgs[i].getTimestampMillis();
                        msgBody = msgs[i].getMessageBody();
                        Log.d("1021",msgFrom + "," + msgBody + ","+ timeStamp);
                        EventBus.getDefault().post(new SmsEvent(msgFrom, timeStamp, msgBody));
                        LogService.startActionLog(context, timeStamp, msgBody);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
