package com.SRA;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;


public class RunActivity extends Activity {
	
	double totalmiles = 5.0;
	double expectedSpeed = 2.0;
	String lovedSongPath="";
	String hatedSongPath="";
	MediaPlayer lovedSong = null;
	MediaPlayer hatedSong = null;
	private int is_playing_loved_song = 0;
	
	// Location related variables
	private LocationManager locationManager; 
    private LocationListener locationListener;
    private boolean has_bind_to_locListener = false;
    double mySpeed, maxSpeed;
    double runMiles = 0.0;
    private Location lastLocation = null;
    private long lastTime;

    Button doneButton;
	TextView infoMiles, infoSpeed, infoCurSpeed, infoLocation;
	private ProgressBar rectangleProgressBar;
	
	String strCurSpeed;
	Location checkCurLocation = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        
        infoMiles = (TextView)this.findViewById(R.id.runAndTotalMiles);
        infoSpeed = (TextView)this.findViewById(R.id.speedInfo);
        infoCurSpeed = (TextView)this.findViewById(R.id.curSpeed);
        infoLocation = (TextView)this.findViewById(R.id.locationInfo);
        
        rectangleProgressBar = (ProgressBar)findViewById(R.id.rectangleProgressBar);
        doneButton = (Button)this.findViewById(R.id.runDone);
        
        rectangleProgressBar.setIndeterminate(false);
        rectangleProgressBar.setVisibility(View.VISIBLE);  
          
        rectangleProgressBar.setMax(100);  
        rectangleProgressBar.setProgress(0);
        
        Intent it = getIntent();
        totalmiles = it.getDoubleExtra("totalmiles", 5.0);
        expectedSpeed = it.getDoubleExtra("expectedspeed", 2.0);
        
        lovedSongPath = it.getStringExtra("lovedSong");
        hatedSongPath = it.getStringExtra("hatedSong");
        
       
        // Done
        doneButton.setOnClickListener(new View.OnClickListener() 
        	{
				public void onClick(View v) 
				{
					Intent it=new Intent();
					it.setClass(RunActivity.this, MainActivity.class);
					finish();
					startActivity(it);
				}
        	});
    };

    @Override
    protected void onStart()
    {
    	super.onStart();
    	
    	if( lovedSongPath != null && !lovedSongPath.equals(""))
        	lovedSong = MediaPlayer.create(this,Uri.parse(lovedSongPath)); 
        
        if( hatedSongPath != null && !hatedSongPath.equals(""))
        	hatedSong=MediaPlayer.create(this,Uri.parse(hatedSongPath));
        
        // Location Related codes
        mySpeed = maxSpeed = 0;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); 
        locationListener = new SpeedLocationListener();
        
        // Start listening to GPS update
        startListening();
        
        lastTime = 0;
        
        // Start a thread checking if the device is still or not
        checkIsRunning();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_run, menu);
        return true;
    }
    
    @Override
    protected void onPause() {
        stopListening();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startListening();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
    	if( lovedSong != null ) 
    	{
    		lovedSong.stop();
    		lovedSong.release();
    	}
    	if( hatedSong != null ) 
    	{
    		hatedSong.stop();
    		hatedSong.release();
    	}
    	
    	stopListening();
    	
    	this.finish();
        super.onDestroy();
    }

    /**********************************************************************
     * helpers functions 
     **********************************************************************/
    private void startListening() {
    	
    	 if( has_bind_to_locListener ) return;
    	 
    	 if (locationManager == null ) return;
    	 
    	 final Criteria criteria = new Criteria();
         
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setSpeedRequired(true);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setCostAllowed(true);
         criteria.setPowerRequirement(Criteria.POWER_LOW);
         
         final String bestProvider = locationManager.getBestProvider(criteria, true);
         
         if ( bestProvider != null && bestProvider.length() > 0 )
         {
             locationManager.requestLocationUpdates(bestProvider, 0,
                                 0, locationListener);
             lastLocation = locationManager.getLastKnownLocation(bestProvider);
             
         }
         else
         {
                 final List<String> providers = locationManager.getProviders(true);
                 
                 for (final String provider : providers)
                 {
                     locationManager.requestLocationUpdates(provider, 0,
                                 0, locationListener);
                     lastLocation = locationManager.getLastKnownLocation(provider);                	 
                 }
         }
 
         has_bind_to_locListener = true;
         
         setMySpeed();
    }

    private void checkIsRunning()
    {
    	if (locationManager == null ) return;
    	
    	checkCurLocation = null;
    	
    	final Criteria criteria = new Criteria();
        
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
    	
    	final String bestProvider = locationManager.getBestProvider(criteria, true);
        
        if ( bestProvider != null && bestProvider.length() > 0 )
        {
        	checkCurLocation = locationManager.getLastKnownLocation(bestProvider);
            
        }
        else
        {
                final List<String> providers = locationManager.getProviders(true);
                
                for (final String provider : providers)
                {
                	checkCurLocation = locationManager.getLastKnownLocation(provider);                	 
                }
        }
    	
        infoCurSpeed.post(new Runnable() 
		{  
            public void run() 
            {
            	if( checkCurLocation.distanceTo(lastLocation) < 0.00000005 )
            	{
            		mySpeed = 0.0;
            		strCurSpeed = "\nCurrent Speed:\n" + Double.toString(mySpeed) + " miles/hour";
            		infoCurSpeed.setText(strCurSpeed);
            		pauseLovedSong();
            		playHatedSong();
	        		try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
	        	}
            }  
        });
    }
    
    private void stopListening() {
        if (locationManager != null && has_bind_to_locListener)
        {
            locationManager.removeUpdates(locationListener);
            has_bind_to_locListener = false;
        }
    }
    private void playLovedSong(){
    	if( lovedSong == null )
    		return;
    	
    	if( is_playing_loved_song != 1)
    	{
    		lovedSong.setLooping(true);
        	lovedSong.start();
    	}
    	is_playing_loved_song = 1;    	
    }
    
    private void pauseLovedSong(){
     	if( lovedSong == null )
    		return;
     	
     	if( is_playing_loved_song == 1 )
     	{
     		lovedSong.pause();
     		is_playing_loved_song = 0;
     	}     	
    }
    
    private void playHatedSong(){
    	if( hatedSong == null )
    		return;
    	
    	if( is_playing_loved_song != -1)
    	{
    		hatedSong.setLooping(true);
    		hatedSong.start();
    	}
    	is_playing_loved_song = -1;
    }
    
    private void pauseHatedSong(){
    	if( hatedSong == null )
    		return;    	
    	
    	if( is_playing_loved_song == -1 )
    	{
    		hatedSong.pause();
    		is_playing_loved_song = 0;
    	}
    }
    
    private void setMySpeed()
    {
    	// Convert to miles/h
    	double _curSpeed, _maxSpeed;
    	
    	_curSpeed = 2.23693629 * mySpeed;
    	_maxSpeed = 2.23693629 * maxSpeed;
    	StringBuilder myAddress = new StringBuilder("Address:\n");
    	
    	
    	// Geocode latitude and longtitude to address name
    	Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

        try 
        {
		   List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
		  
		   if( addresses != null ) 
		   {
			    Address geocodedAddress = addresses.get(0);
			    for(int i = 0; i < geocodedAddress.getMaxAddressLineIndex(); i++) 
			    {
			    	myAddress.append(geocodedAddress.getAddressLine(i)).append(" ");
			    }
		   }
		 } catch (IOException e) {}
    	
    	
        // construct string to display
    	
    	// Set info miles
    	String strRunAndTotalMiles = "Run Miles/Goal Miles:" + runMiles + "/" + totalmiles + "\nProgress:" + runMiles/totalmiles*100.0 + "%\n";
        infoMiles.setText(strRunAndTotalMiles);
    	 
        // Set progress
        if( runMiles >= totalmiles)
        	rectangleProgressBar.setProgress(100);
        else
        	rectangleProgressBar.setProgress((int) (runMiles/totalmiles*100.0));
        
        // Set speed infomation
        String strSpeedInfo = "\n\nExpected Speed:" + Double.toString(expectedSpeed) + " miles/hour\nMax Speed:" + _maxSpeed + " miles/hour";
        infoSpeed.setText(strSpeedInfo);
        
        // check speed exceeds expected speed or not and set current speed info
        strCurSpeed = "\nCurrent Speed:\n" + Double.toString(_curSpeed) + " miles/hour";
        
        if( _curSpeed < expectedSpeed )
        {
        	infoCurSpeed.setBackgroundColor(TRIM_MEMORY_RUNNING_LOW);
        	infoCurSpeed.setTextColor(Color.MAGENTA);
        	if( is_playing_loved_song != -1 ) 
        	{
        		infoCurSpeed.post(new Runnable() 
        		{  
	                public void run() 
	                {
	                	for( int i = 4; i >= 0; i--)
			        	{
			        		String tmpInfoStr = strCurSpeed;
			        		if( i > 0 )
			        			tmpInfoStr += "\n In " + (i + 1) + " seconds the music will be changed to the one you hated!";
			        		else
			        			tmpInfoStr += "\n The music changing to the one you hated!";
			        		infoCurSpeed.setText(tmpInfoStr);
			            	try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								//e.printStackTrace();
							}
			        	}
		        		pauseLovedSong();
		        		playHatedSong(); 
	                }  
                });
        	}
        	else
        	{
        		infoCurSpeed.setText(strCurSpeed);
        	}
        }
        else
        {
        	if( is_playing_loved_song != 1 )
        	{
        		pauseHatedSong();
        		playLovedSong();
        	}
        	infoCurSpeed.setBackgroundColor(Color.LTGRAY);
        	infoCurSpeed.setTextColor(Color.GREEN);
        	infoCurSpeed.setText(strCurSpeed);
        }    	           
        
        
        // Set location info
        String strLocation = "\nLatitude:" + lastLocation.getLatitude() + "\nLongtitude:" + lastLocation.getLongitude() 
        					+ "\n" + myAddress.toString();
        infoLocation.setText(strLocation);        
    }
 
    // Class for LocationListener
    private class SpeedLocationListener implements LocationListener 
    { 
    	//@Override
    	public void onLocationChanged(Location location) 
    	{ 
    		if( location != null ) 
    		{ 
    			long time = location.getTime();
    			float d = 0;
    			if( lastLocation != null )
				{
					d = location.distanceTo(lastLocation);
				}
    			
    			runMiles += 0.00062 * d;
    			
    			if( location.hasSpeed() )
    			{
    				mySpeed = location.getSpeed();
    			}
    			else
    			{
    				if( lastLocation != null && lastTime != 0 )
    				{
    					mySpeed = d / (time - lastTime) / 1000.0;
    				}
    			}
    			
    			lastTime = time;
				lastLocation = location;
				
				if( mySpeed > maxSpeed ) maxSpeed = mySpeed;
				
				setMySpeed();
            } 
        } 

    	//@Override
        public void onProviderDisabled(String provider) { 
                // TODO Auto-generated method stub 

        } 

    	//@Override
        public void onProviderEnabled(String provider) { 
                // TODO Auto-generated method stub 

        } 

    	//@Override
        public void onStatusChanged(String provider, int status, Bundle extras) { 
                // TODO Auto-generated method stub 

        } 
    }  
}
