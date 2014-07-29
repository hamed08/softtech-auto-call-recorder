package com.softtech.apps.callrecorder;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;



// This class to excute record incomming call when receiver receive an incomming call
public class CR_RecordService extends Service{

	public static final String LISTEN_ENABLED = "ListenEnabled";
	public static final String FILE_DIRECTORY = "softtech";
	private MediaRecorder recorder = new MediaRecorder();
	private String phoneNumber = null;
	private int audioQuality;
	public static final int STATE_INCOMING_NUMBER = 0;
	public static final int STATE_CALL_START = 1;
	public static final int STATE_CALL_END = 2;
	
	private NotificationManager manger;
	private String myFileName;
	private Boolean is_offhook = false;
	
	
	String tag = "AUTO_ANSWER_PHONE_CALL";
	
	public CR_RecordService() {
		// TODO Auto-generated constructor stub
		Log.d(tag,"Da start service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		int commandType = intent.getIntExtra("commandType", STATE_INCOMING_NUMBER);
		if (commandType == STATE_INCOMING_NUMBER)
		{
			if (phoneNumber == null)
				phoneNumber = intent.getStringExtra("phoneNumber");
		}
		else if (commandType == STATE_CALL_START)
		{
			if (phoneNumber == null)
				phoneNumber = intent.getStringExtra("phoneNumber");
				audioQuality = intent.getIntExtra("audioQuality", 1);
			
			Log.d(tag,"Nhan start command - Bat dau nhan command");
			try {
				// reset lai tat ca nhung thu tro ve nac dinh
				//terminateAndEraseFile();
				recorder.reset();
				recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				
				// Config audio quality here
				AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	            am.setMode(AudioManager.MODE_IN_CALL); 
				Log.d("RECEIVER","Audio quality = "+audioQuality);
//				switch(audioQuality)
//				{
//					case 1: // Low, 8kHz, 16Bit, Mono
//						recorder.setAudioChannels(1);
//						recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
//						recorder.setAudioSamplingRate(8000);
//						break;
//					case 2: // Moderate, 22kHz, 16Bit, Mono
//						recorder.setAudioChannels(1);
//						recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
//						recorder.setAudioSamplingRate(22050);
//						break;
//					case 3: // Hight, 44kHz, 16Bit, stereo
//						recorder.setAudioChannels(2);
//						recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
//						recorder.setAudioSamplingRate(44100);
//						break;
//					default:
//						recorder.setAudioChannels(1);
//						recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
//						recorder.setAudioSamplingRate(8000);
//						break;
//				}
				// 1 (mono)
			    // 2 (stereo)
			    
				myFileName = getFilename();
				recorder.setOutputFile(myFileName);
				
				
				Log.d("RECEIVER","Audio quality = "+audioQuality);
				Log.d(tag,"Duong dan file = "+myFileName);
			}
			catch (IllegalStateException e) {
				Log.e("Call recorder IllegalStateException: ", "");
				terminateAndEraseFile();
			}
			catch (Exception e) {
				Log.e("Call recorder Exception: ", "");
				terminateAndEraseFile();
			}
			
			OnErrorListener errorListener = new OnErrorListener() {

				public void onError(MediaRecorder arg0, int arg1, int arg2) {
					Log.e("Call recorder OnErrorListener: ", arg1 + "," + arg2);
					arg0.stop();
					arg0.reset();
					arg0.release();
					arg0 = null;
					terminateAndEraseFile();
				}
				
			};
			recorder.setOnErrorListener(errorListener);
			OnInfoListener infoListener = new OnInfoListener() {

				public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
					Log.e("Call recorder OnInfoListener: ", arg1 + "," + arg2);
					arg0.stop();
					arg0.reset();
					arg0.release();
					arg0 = null;
					terminateAndEraseFile();
				}
				
			};
			recorder.setOnInfoListener(infoListener);
			
			
			
			
			try {
//				recorder.prepare();
//				recorder.start();
				
				startRecord();
				
				Toast toast = Toast.makeText(this, this.getString(R.string.reciever_start_call), Toast.LENGTH_SHORT);
		    	toast.show();
		    	
		    	manger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		    	Notification notification = new Notification(R.drawable.ic_launcher, this.getString(R.string.notification_ticker), System.currentTimeMillis());
		    	notification.flags = Notification.FLAG_NO_CLEAR;
		    	
		    	Intent intent2 = new Intent(this, MainActivity.class);
		    	intent2.putExtra("RecordStatus", true);

		        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent2, 0);
		        notification.setLatestEventInfo(this, this.getString(R.string.notification_title), this.getString(R.string.notification_text), contentIntent);
		        //manger.notify(0, notification);
		        
		        startForeground(1337, notification);
		        Log.d(tag,"bat dau ghi am");
		    	
			} catch (IllegalStateException e) {
				Log.e("Call recorder IllegalStateException: ", "");
				terminateAndEraseFile();
				e.printStackTrace();
			} catch (Exception e) {
				Log.e("Call recorder Exception: ", "");
				terminateAndEraseFile();
				e.printStackTrace();
			}
			
			
		}
		else if (commandType == STATE_CALL_END)
		{
			Log.d(tag,"Nhan command ket thuc");
			try {
				
//				recorder.stop();
//				recorder.reset();
//				recorder.release();
//				recorder = null;
				if(is_offhook==true){
					stopRecord();
				}
				Toast toast = Toast.makeText(this, this.getString(R.string.reciever_end_call), Toast.LENGTH_SHORT);
		    	toast.show();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			if (manger != null)
				manger.cancel(0);
			stopForeground(true);
			this.stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void startRecord()
	{
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recorder.start();
		is_offhook = true;
	}
	
	private void stopRecord()
	{
		recorder.stop();
		recorder.reset();
		recorder.release();
		recorder = null;
		System.gc();
		is_offhook = false;
	}
	
	/**
	 * in case it is impossible to record
	 * Check device can surport record
	 */
	private void terminateAndEraseFile()
	{
		try {
//			recorder.stop();
//			recorder.reset();
//			recorder.release();
//			recorder = null;
			
			stopRecord();
			
			Toast toast = Toast.makeText(this, this.getString(R.string.reciever_end_call), Toast.LENGTH_SHORT);
	    	toast.show();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		File file = new File(myFileName);
		
		if (file.exists()) {
			file.delete();
			
		}
		Toast toast = Toast.makeText(this, this.getString(R.string.record_impossible), Toast.LENGTH_LONG);
    	toast.show();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private String getFilename() {
		//String filepath = getFilesDir().getAbsolutePath();
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, FILE_DIRECTORY);

		if (!file.exists()) {
			file.mkdirs();
		}
		
		String myDate = new String();
		myDate = (String) DateFormat.format("yyyyMMddkkmmss", new Date());

		return (file.getAbsolutePath() + "allcalls/" + myDate + "-" + phoneNumber + ".mp3");
	}

}