package com.stratus.activity;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Properties;

import com.stratus.R;
import com.stratus.UDP.UDPStream;
import com.stratus.mavlink.MAVlink;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;

public class MainActivity extends Activity 
{
	final Context context = this;
	MAVlink MAV = new MAVlink();
    public Properties buttonColor = new Properties();
    UDPStream stream;
    GPS gps;
    boolean override = false;
    boolean alarmRunning = false;
    MediaPlayer alert;
    
    //async tasks
    OpenConnection openConnection;
    StatusLoop statusLoop = new StatusLoop();
    Disarm disarm;
    Takeoff takeoff;
    Land land;
    ReturnToBase rtb;
    FollowMe followMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        gps = new GPS(context);
        alert = MediaPlayer.create(context, R.raw.alarm);
        
        buttonColor.put(R.id.button1, 0xFF299130);
        buttonColor.put(R.id.button2, 0xFFCC0000);
        buttonColor.put(R.id.button3, 0xFF0099FF);
        buttonColor.put(R.id.button4, 0xFFFF6600);
        buttonColor.put(R.id.button5, 0xFFE6B800);
        buttonColor.put(R.id.button6, 0xFF006666);
 
        resetAll();
    }
    
    @Override
    protected void onStop()
    {
    	super.onStop();
    	alert.stop();
    	gps.stopGPS();
    	if(!statusLoop.isCancelled())
        {
        	statusLoop.cancel(true);
        }
    	if(!openConnection.isCancelled())
    	{
    		openConnection.cancel(true);
    	}
    	if(followMe != null && !followMe.isCancelled())
    	{
    		followMe.cancel(true);
    	}
    	try {
    		stream.close();
    	} catch (Exception e) {}
    	finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add("Override");
    	menu.add("Set Altitude");
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if(item.getTitle().equals("Override"))
    	{
	    	override = true;
	    	enableButton(R.id.button1);
	    	enableButton(R.id.button2);
	    	enableButton(R.id.button3);
	    	enableButton(R.id.button4);
	    	enableButton(R.id.button5);
	    	enableButton(R.id.button6);
	    	return true;
    	}
    	else
    	{
    		View altView = getLayoutInflater().inflate(R.layout.altitude, null);
    		final NumberPicker alt = (NumberPicker) altView.findViewById(R.id.altitudePicker);
    		//prevent keyboard from popping up
    		alt.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    		alt.setMinValue(3);
    		alt.setMaxValue(20);
    		alt.setValue(MAV.getTargetAltitude());
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setView(altView);
    		builder.setTitle("Set Altitude (meters)");
    		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
    		builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MAV.setTargetAltitude(alt.getValue());
					dialog.cancel();
				}
			});
    		
    		AlertDialog altitudeDialog = builder.create();
    		altitudeDialog.show();
    		return true;
    	}
    }
    
    public void resetAll()
    {
        disableButton(R.id.button1);
        disableButton(R.id.button2);
        disableButton(R.id.button3);
        disableButton(R.id.button4);
        disableButton(R.id.button5);
        disableButton(R.id.button6);
        
        MAV.setUAVBattery("");
        MAV.setUAVGPS(-1);
        MAV.setAltitude("");
        MAV.setSpeed("");
        MAV.setMAVType("");
        MAV.setAPType("");
        MAV.setMAVRoll((float)0);
        MAV.setMAVPitch((float)0);
        MAV.setMAVYaw(0);
        MAV.setSystemId(0);
        MAV.setComponentId(0);
        MAV.setBattery((float) 100);
        
    	try {
    		stream.close();
    	} catch (Exception e) {}

        if(!statusLoop.isCancelled())
        {
        	statusLoop.cancel(true);
        }
        
        Button arm = (Button)findViewById(R.id.button6);
        arm.setText("Disarm");
        arm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendDisarmCommand();
			}
		});
        
        updateStatus();
        
        openConnection = new OpenConnection();
    	openConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public void enableButton(int buttonId)
    {
    	Button button = (Button)findViewById(buttonId);
    	button.getBackground().setColorFilter((Integer)buttonColor.get(buttonId), PorterDuff.Mode.OVERLAY);
    	button.setEnabled(true);
    }
    
    public void disableButton(int buttonId)
    {
    	Button button = (Button)findViewById(buttonId);
    	button.getBackground().setColorFilter(0xFFE2E1E, PorterDuff.Mode.OVERLAY);
    	button.setEnabled(false);    	
    }
    
    public void updateStatus()
    {
    	TextView uavBatt = (TextView)findViewById(R.id.statusDisplay1);
    	TextView uavGPS = (TextView)findViewById(R.id.statusDisplay2);
    	TextView phoneGPS = (TextView)findViewById(R.id.statusDisplay3);
    	TextView altitude = (TextView)findViewById(R.id.statusDisplay4);
    	TextView speed = (TextView)findViewById(R.id.statusDisplay5);
    	TextView roll = (TextView)findViewById(R.id.statusDisplay6);
    	TextView pitch = (TextView)findViewById(R.id.statusDisplay7);
    	TextView yaw = (TextView)findViewById(R.id.statusDisplay8);
        uavBatt.setText(MAV.getUAVBattery());
        uavGPS.setText(MAV.getUAVGPSStatus());
        phoneGPS.setText(gps.getGPSStatus());
        altitude.setText(MAV.getAltitude());
        speed.setText(MAV.getSpeed());
        roll.setText(MAV.getRoll());
        pitch.setText(MAV.getPitch());
        yaw.setText(MAV.getYaw());
        
        //low battery alarm
        if(MAV.getBattery() < 9.5 && !alarmRunning)
        {
        	alert.setLooping(true);
        	alert.start();
        	alarmRunning = true;
        	TextView batt = (TextView)findViewById(R.id.statusText1);
        	uavBatt.setTextColor(Color.RED);
        	batt.setTextColor(Color.RED);
        }
    }

    public void connectSuccess()
    {
    	enableButton(R.id.button1);
    	statusLoop = new StatusLoop();
    	statusLoop.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void takeOff(View v) 
    {
    	takeoff = new Takeoff();
    	takeoff.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public void landHere(View v)
    {
    	land = new Land();
    	land.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public void returnToBase(View v)
    {
    	rtb = new ReturnToBase();
    	rtb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public void sendDisarmCommand()
    {
    	disarm = new Disarm();
		disarm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public void followMe(View v)
    {
    	followMe = new FollowMe();
    	followMe.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void wait(View v)
    {
        if(!followMe.isCancelled())
        {
        	followMe.cancel(true);
        }
    }
    
    private class OpenConnection extends AsyncTask<Void, Void, Integer>
    {
    	protected ConnectDialog connectDialog;
    	
    	@Override
    	protected void onPreExecute()
    	{
    		connectDialog = new ConnectDialog();
        	connectDialog.setCancelable(false);
        	connectDialog.show(getFragmentManager(), "connect");
    	}
    	
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		//open socket
    		try
    		{
    			try {
    	    		stream.close();
    	    	} catch (Exception e) {}
    			stream = new UDPStream();
    			//listen for heartbeat
    			while(true)
    			{
    				int messageResult = MAV.GetMessage(stream, true);
    				if(messageResult == MAV.HEARTBEAT)
    				{
    					MAV.sendHeartbeat(stream);
    					MAV.requestDataStream(stream);
    					return 1;
    				}
    				else if(messageResult == MAV.TIMEOUT)
    				{
    					return 0;
    				}
    			}
    		}catch(SocketTimeoutException e){
    			return 0;
    		}catch(IOException e){
    			e.getMessage();
    			return 0;
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		connectDialog.dismiss();
    		if(result == 0)
    		{
        		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        		builder.setTitle("Connection Failed");
        		builder.setMessage("Make sure you are connected to Stratus Wi-Fi with Static IP: 1.2.3.55");
        		builder.setCancelable(false);
        		builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.cancel();
    					openConnection = new OpenConnection();
    			    	openConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    				}
    			});
        		AlertDialog failDialog = builder.create();
        		failDialog.show();
    		}
    		else if(result == 1)
    		{
    			Toast toast = Toast.makeText(context, "Connection Successful", Toast.LENGTH_SHORT);
    			toast.show();
        		connectSuccess();   			
    		}
    	}
    }//OpenConnection Task
    	 
    private class StatusLoop extends AsyncTask<Void, Void, Integer>
    {
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		try
    		{
    			int messageCount = 0;
    			while(true)
    			{
    				if(this.isCancelled())
    				{
    					return 1;
    				}
    				int messageResult = MAV.GetMessage(stream, false);
    				if(messageResult != MAV.TIMEOUT)
    				{
    					messageCount++;
    				}
    				else
    				{
    					break;
    				}
    				if(messageCount % 5 == 0)
    				{
    					publishProgress();
    				}
    			}
    			return 0;
    		}catch(Exception e){
    			return 0;
    		}
    	}
    	
    	@Override
    	protected void onProgressUpdate(Void... progress)
    	{
    		updateStatus();
    		if(MAV.isToastAvailable())
    		{
    			Toast toast = Toast.makeText(context, MAV.getToastText(), Toast.LENGTH_LONG);
    			toast.show();
    			MAV.setToastAvailable(false);
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setTitle("Connection Lost");
    		builder.setMessage("No longer receiving messages, please try reconnecting.");
    		builder.setCancelable(false);
    		builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					openConnection = new OpenConnection();
			    	openConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			});
    		AlertDialog lostConnDialog = builder.create();
    		lostConnDialog.show();
    	}
    }
    
    private class Takeoff extends AsyncTask<Void, Void, Integer>
    {
    	@Override 
    	protected void onPreExecute()
    	{
    		if(!override)
    		{
	    		disableButton(R.id.button1);
	    		disableButton(R.id.button2);
	    		disableButton(R.id.button3);
	    		disableButton(R.id.button4);
	    		disableButton(R.id.button5);
	    		disableButton(R.id.button6);
    		}
    	}
    	
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		try{
	    		MAV.clearMission(stream);
	    		//clear mission script
	    		//APM sends 3 acks for some reason, sleep thread for 1/2 second to wait for all 3
	    		Thread.sleep(500);
	    		long timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		MAV.setCommandSuccess(false);
	    		
	    		//alert APM to receive mission script with two items
	    		MAV.sendMissionCount(stream);
	    		
	    		//wait to receive mission request
	    		timer = System.currentTimeMillis();
	    		while(!MAV.isMissionRequest())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		MAV.setMissionRequest(false);
	    		//send home waypoint item
	    		MAV.sendHomeWaypointItem(stream);
	    		
	    		//wait to receive mission request
	    		timer = System.currentTimeMillis();
	    		while(!MAV.isMissionRequest())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		MAV.setMissionRequest(false);
	    		
	    		//send loiter waypoint
	    		MAV.sendLoiterItem(stream);

	    		//wait to receive mission ack
	    		timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		MAV.setCommandSuccess(false);
	    		
	    		//make sure motors still armed
	    		MAV.sendArmCommand(stream, true);
	    		timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 10000)
	    			{
	    				return 1;
	    			}
	    		}
	    		MAV.setCommandSuccess(false);
	    		
	    		//switch to auto mode
	    		MAV.missionStart(stream);
	    		
	    		//wait to receive command ack
	    		timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		MAV.setCommandSuccess(false);
	    		
//	    		//must raise throttle for mission to start, no ack
//	    		MAV.overrideThrottle(stream);
//	    		Thread.sleep(500);
//	    		//release throttle back to RC
//	    		MAV.releaseThrottle(stream);
	    		return 0;
    		}catch(Exception e){
    			e.getMessage();
    			return 1;
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		if(result == 0)
    		{
    			if(!override)
    			{
	    			disableButton(R.id.button1);
	        		enableButton(R.id.button2);
	        		enableButton(R.id.button3);
	        		disableButton(R.id.button4);
	        		enableButton(R.id.button5);
	        		disableButton(R.id.button6);
    			}
    			Toast toast = Toast.makeText(context, "Command received", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    		else
    		{
    			if(!override)
    			{
	    			enableButton(R.id.button1);
	        		disableButton(R.id.button2);
	        		disableButton(R.id.button3);
	        		disableButton(R.id.button4);
	        		disableButton(R.id.button5);
	        		disableButton(R.id.button6);
    			}
    			Toast toast = Toast.makeText(context, "Command not received, try again", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }

    private class Disarm extends AsyncTask<Void, Void, Integer>
    {
    	@Override
    	protected void onPreExecute()
    	{
    		if(!override)
    		{
	    		disableButton(R.id.button1);
	    		disableButton(R.id.button2);
	    		disableButton(R.id.button3);
	    		disableButton(R.id.button4);
	    		disableButton(R.id.button5);
	    		disableButton(R.id.button6);
    		}
    	}
    	
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		try{
	    		MAV.sendArmCommand(stream, false);
	    		//wait for command ack
	    		long timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		if(MAV.isCommandSuccess())
	    		{	
	    			MAV.setCommandSuccess(false);
	    			return 0;
	    		}
	  
    		}catch(Exception e){
    			e.getMessage();
    			return 1;
    		}
    		return 1;
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		if(result == 0)
    		{
    			Toast toast = Toast.makeText(context, "Command received", Toast.LENGTH_SHORT);
    			toast.show();
    			if(!override)
    			{
	    			enableButton(R.id.button1);
	    			disableButton(R.id.button2);
	    			disableButton(R.id.button3);
	    			disableButton(R.id.button4);
	    			disableButton(R.id.button5);
	    			disableButton(R.id.button6);
    			}
    		}
    		else
    		{
    			if(!override)
    			{
		    		enableButton(R.id.button1);
		    		disableButton(R.id.button2);
		    		disableButton(R.id.button3);
		    		disableButton(R.id.button4);
		    		disableButton(R.id.button5);
		    		enableButton(R.id.button6);
    			}
    			Toast toast = Toast.makeText(context, "Command not received, try again", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }

    private class Land extends AsyncTask<Void, Void, Integer>
    {
    	@Override
    	protected void onPreExecute()
    	{
    		if(!override)
    		{
	    		disableButton(R.id.button1);
	    		disableButton(R.id.button2);
	    		disableButton(R.id.button3);
	    		disableButton(R.id.button4);
	    		disableButton(R.id.button5);
	    		disableButton(R.id.button6);
    		}
    	}
    	
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		try{
	    		MAV.sendLandCommand(stream, false);
	    		//wait for command ack
	    		long timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		if(MAV.isCommandSuccess())
	    		{	
	    			MAV.setCommandSuccess(false);
	    			return 0;
	    		}
	  
    		}catch(Exception e){
    			e.getMessage();
    			return 1;
    		}
    		return 1;
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		if(result == 0)
    		{
    			Toast toast = Toast.makeText(context, "Command received", Toast.LENGTH_SHORT);
    			toast.show();
    			if(!override)
    			{
	    			enableButton(R.id.button1);
	    			disableButton(R.id.button2);
	    			disableButton(R.id.button3);
	    			disableButton(R.id.button4);
	    			disableButton(R.id.button5);
	    			enableButton(R.id.button6);
    			}
    		}
    		else
    		{
    			if(!override)
    			{
		    		disableButton(R.id.button1);
		    		enableButton(R.id.button2);
		    		enableButton(R.id.button3);
		    		disableButton(R.id.button4);
		    		enableButton(R.id.button5);
		    		disableButton(R.id.button6);
    			}
    			Toast toast = Toast.makeText(context, "Command not received, try again", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }

    private class ReturnToBase extends AsyncTask<Void, Void, Integer>
    {
    	@Override
    	protected void onPreExecute()
    	{
    		if(!override)
    		{
	    		disableButton(R.id.button1);
	    		disableButton(R.id.button2);
	    		disableButton(R.id.button3);
	    		disableButton(R.id.button4);
	    		disableButton(R.id.button5);
	    		disableButton(R.id.button6);
    		}
    	}
    	
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		try{
	    		MAV.sendLandCommand(stream, true);
	    		//wait for command ack
	    		long timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				return 1;
	    			}
	    		}
	    		if(MAV.isCommandSuccess())
	    		{	
	    			MAV.setCommandSuccess(false);
	    			return 0;
	    		}
	  
    		}catch(Exception e){
    			e.getMessage();
    			return 1;
    		}
    		return 1;
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		if(result == 0)
    		{
    			Toast toast = Toast.makeText(context, "Command received", Toast.LENGTH_SHORT);
    			toast.show();
    			if(!override)
    			{
	    			disableButton(R.id.button1);
	    			enableButton(R.id.button2);
	    			enableButton(R.id.button3);
	    			disableButton(R.id.button4);
	    			enableButton(R.id.button5);
	    			disableButton(R.id.button6);
    			}
    		}
    		else
    		{
    			if(!override)
    			{
		    		disableButton(R.id.button1);
		    		enableButton(R.id.button2);
		    		enableButton(R.id.button3);
		    		disableButton(R.id.button4);
		    		enableButton(R.id.button5);
		    		disableButton(R.id.button6);
    			}
    			Toast toast = Toast.makeText(context, "Command not received, try again", Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	}
    }
    
    private class FollowMe extends AsyncTask<Void, Void, Integer>
    {    
    	@Override
    	protected Integer doInBackground(Void...params)
    	{
    		try
    		{
				MAV.sendWaypoint(stream, gps);
	    		long timer = System.currentTimeMillis();
	    		while(!MAV.isCommandSuccess())
	    		{
	    			if(System.currentTimeMillis() - timer > 2000)
	    			{
	    				break;
	    			}
	    		}
	    		if(MAV.isCommandSuccess())
	    		{	
	    			MAV.setCommandSuccess(false);
	    			return 0;
	    		}
	    		return 1;
    		}catch(Exception e){
    			e.getMessage();
    			return 1;
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(Integer result)
    	{
    		
    		if(result == 0)
    		{
    			Toast toast = Toast.makeText(context, ((Float)gps.getLatitude()).toString() + " " + ((Float)gps.getLatitude()).toString(), Toast.LENGTH_SHORT);
    			toast.show();
    		}
    		else
    		{
	    		Toast toast = Toast.makeText(context, "Follow Me error", Toast.LENGTH_LONG);
				toast.show();
    		}
    	}
    	
    }
}