package com.iot.smsloger;

/**
 * Created by MaJian on 2015/10/12.
 */
public class Sms
{
    public static final int TYPE_UNKNOW = -1;
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SENT = 2;
    public String address;
    public long date;
    public String msg;
    public int type;
    public int id;
    public Sms(int id, String address, long date, String msg, int type)
    {
        this.id = id;
        this.address = address;
        this.date = date;
        this.msg = msg;
        this.type = type;
    }
}
