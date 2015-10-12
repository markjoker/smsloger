# smsloger
监听短信接收功能，在APP填写验证码时，通过自动填充验证码，能够提升用户体验。也在通过手机号自动注册等功能上有比较好的作用。下面介绍两种监听接收短信的方法。

##一, 通过广播读取接收短信
###1. 在AndroidManifest文件中注册receiver
```XML
<receiver android:name=".SmsReceiver">   
     <intent-filter>
         <action android:name="android.provider.Telephony.SMS_RECEIVED" />
     </intent-filter>
 </receiver>
 ```
最好在注册监听的同时设置优先级，保证广播的接收
```Xml
<intent-filter android:priority="2147483647">
``` 
###2. 在AndroidManifest文件中声明权限
```XML
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS"/>
```

###3. 实现SmsReceiver中OnReceive方法
接收到SMS_RECEIVED_ACTION广播事件后，从Intent中解析出短信的相关信息。
```Java
public void onReceive(Context context, Intent intent)
{
	String action = intent.getAction();
	
	if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action))
	{
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String msgBody;
		String msgFrom;
		long timeStamp;
		if (bundle != null)
		{
			Object[] pdus = (Object[])bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++)
			{
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				msgFrom = msgs[i].getOriginatingAddress();
				timeStamp = msgs[i].getTimestampMillis();
				msgBody = msgs[i].getMessageBody();
				Log.d("1021", msgFrom + "," + msgBody + "," + timeStamp);
			}
		}
	}
}
```
##二、通过ContentResolver监听sms数据库变化
###1. 在AndroidManifest文件中声明Service
```XML
<service
	android:name=".service.SmsGuardService"
	android:enabled="true"
	android:exported="false" >
</service>
```
###2. 在AndroidManifest文件中声明权限
```XML
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS"/>
```

###3. 在SMSGuardService中实现监听
**注册监听**
通过ContentResolver注册监听sms数据库变化
```Java
@Override
public void onCreate()
{
	super.onCreate();
	mContentResolver = getContentResolver();
	mSmsObserver = new SmsObserver(this, mContentResolver, new Handler(), this);
	Uri smsUri = Uri.parse("content://sms");
	mContentResolver.registerContentObserver(smsUri, true, mSmsObserver);
}
```
**停止监听**
```Java
@Override
public void onDestroy()
{
	super.onDestroy();
	mContentResolver.unregisterContentObserver(mSmsObserver);
}
```
###4. 实现SmsObserver
**继承ContentObserver，重写OnChange方法**
通过重写onChange方法，在sms数据库发生变化时会调用此方法，然后通过获取最新一条短信记录，通过判断判断短信的类型调用监听的sent或者received方法。
```Java
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
```
注意，数据库变化可能存在重复修复，所以可能会在接到或者发送短信时多次调用onChange方法，需要用isNewMsg判断一下是否短信为同一条短信。
