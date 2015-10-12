package com.iot.smsloger;

import com.iot.smsloger.Sms;

/**
 * Created by MaJian on 2015/10/12.
 */
public interface OnStateChangeListener
{
    public void onSmsSent(Sms sms);
    public void onSmsReceived(Sms sms);
}
