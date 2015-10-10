package com.iot.smsloger.event;

/**
 * Created by MaJian on 2015/10/9.
 */
public class SmsEvent
{
    public String formNumber;
    public long timeStamp;
    public String message;
    
    public SmsEvent(String number, long time, String msg)
    {
        formNumber = number;
        timeStamp = time;
        message = msg;
    }
}
