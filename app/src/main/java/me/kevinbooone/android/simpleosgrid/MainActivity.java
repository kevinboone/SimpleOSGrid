/*=====================================================================================

  SimpleOSGrid

  This is the main (and, so far, only) Java source file for the SimpleOSGrid app.

  Copyright (c)2023 Kevin Boone, GPLv3.0

=====================================================================================*/
package me.kevinboone.android.simpleosgrid;

import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.location.*;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import me.kevinboone.android.simpleosgrid.databinding.ActivityMainBinding;
import uk.me.jstott.jcoord.*;

public class MainActivity extends AppCompatActivity implements LocationListener 
  {
  private ActivityMainBinding binding;

  /* gpsIntervalMsec is the time interval at which we would like GPS updates.
     It could probably be longer than this, given that the output is a grid
     reference that is a 100m square -- there's little chance of moving to
     a different grid reference in ten seconds. But I think this value changes
     the time to get a _first_ location update, so it shouldn't be too long. */ 
  private final int gpsIntervalMsec = 10000;

  // Using this without a locale gets a warning from lint. But I'm not going to
  //   go to the effort of providing a locale selection -- default should be
  //   sufficient.
  private static SimpleDateFormat formatter = new SimpleDateFormat ("h:mm a");

/*=====================================================================================

  onCreate

  Set up the user interface, such as it is

=====================================================================================*/
  @Override
  protected void onCreate (Bundle savedInstanceState) 
    {
    super.onCreate (savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView (binding.getRoot());

    binding.onoff.setOnCheckedChangeListener (new OnCheckedChangeListener() 
      {
      public void onCheckedChanged (CompoundButton button, boolean checked)
        {
        if (checked)
          tryStartGPS();
        else
          stopGPS();
        }
      });
    //tryStartGPS(); TODO
    }

/*=====================================================================================

  onLocationChanged

  Called by android when a new location update is available. Just update the
  display.

=====================================================================================*/
  @Override
  public void onLocationChanged (Location location)
    {
    double lat = location.getLatitude();
    double longt = location.getLongitude();
    LatLng ll = new LatLng (lat, longt);
    OSRef osref = ll.toOSRef();
    String s = osref.toSixFigureString();
    TextView tv = (TextView) findViewById (R.id.text_view_result_id);
    tv.setText (s.substring (0, 2) + " " + 
      s.substring (2, 5) + " " +  s.substring (5, 8));
    float accuracy = location.getAccuracy();
    // TODO -- these strings should be externalized
    setStatusMessage ("GPS accuracy " + 
      String.format("%.1f", Float.valueOf(accuracy)) + "m, at " + 
	formatter.format (location.getTime()));
    }

/*=====================================================================================

  onStart

  The app has been made visible. If the Active switch is enabled, try to start
    GPS updates.

=====================================================================================*/
  @Override
  public void onStart()
    {
    super.onStart();
    if (binding.onoff.isChecked())
      tryStartGPS();
    }

/*=====================================================================================

  onStop

  The app has been made invisible. Stop GPS updates. Note that this app does not
    request background update permission, so GPS updates would not be delivered
    even if it were enabled. 

=====================================================================================*/
  @Override
  public void onStop()
    {
    super.onStop();
    stopGPS();
    }

/*=====================================================================================

  setStatusMessage 

  Helper method for setting the text in the status box.

=====================================================================================*/
  public void setStatusMessage (String s)
    {
    TextView tv = (TextView) findViewById (R.id.text_view_message_id);
    tv.setText (s);
    }

/*=====================================================================================

  stopGPS

  Stop receiving GPS updates (saves battery load)

=====================================================================================*/
  public void stopGPS()
    {
    LocationManager locationManager =
	 (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    locationManager.removeUpdates (this);
    String s = getResources().getString (R.string.gps_stopped);
    setStatusMessage (s);
    }

/*=====================================================================================

  tryStartGPS 

  Enable GPS updates, if we have permissions. Prompt for permissions if not, and
    if possible.

=====================================================================================*/
  public void tryStartGPS()
    {
    boolean canTry = false;

    // If we're using Android 6 and later, we can check at runtime whether we
    //   have location permissions. If we don't, we can request the permissions
    //   to be added at runtime. Earlier versions did not have this check, or
    //   the runtime request. For such versions, all we can do is report an error
    //   when requestLocationUpdates() fails. Of course, we have to do that
    //   anyway because, in theory, permissions could be withdrawn whilst the
    //   app is running.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
      {
      // We must not do this check with Android 6 and earlier -- the API is not
      //   present.
      if (ActivityCompat.checkSelfPermission (MainActivity.this, 
         Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
        // We checked permissions, and they're not there. So let's turn the 'Active'
        //   button off, which will force the user to retry switch-on once the
        //   permissions dialog has been shown. Note that canTry remains false
        //   here -- there's no point in trying to enable GPS because the 
        //   permission request will be in a different thread. We still will not
        //   have permissions after requestPermissions() returns, whether the user
        //   grants the permissions or not.
        binding.onoff.setChecked (false);
        requestPermissions (new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, 
          Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
      else
        {
        // We have Android > 6 and the permissions are in place 
        canTry = true;
        }
      }
    else
      {
      // We have Android <= 6, and we have no way to know whether the permissions
      //   are in place or not
      canTry = true;
      }
                        
    if (canTry)
      {
      // Either knowing, or guessing, that permissions are in place, try to
      //   enable location updates.
      LocationManager locationManager =
	(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

      try
	{
	locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, 
	  gpsIntervalMsec, 0 /* metres */, this); 
	boolean gps_enabled = 
	    locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
	if (gps_enabled)
	  {
          // GPS is enabled, but it might take a while to get updates
	  String s = getResources().getString (R.string.waiting);
	  setStatusMessage (s);
	  }
	else
	  {
          // We have location permissions, and updates were enabled, but GPS
          //   is not enabled. We need to warn the user, else he could be 
          //   waiting a long time for a grid reference 
	  String s = getResources().getString (R.string.waiting_no_gps);
	  setStatusMessage (s);
	  }
	}
      catch (SecurityException e)
	{
        // So far as I know, the only reason to get this exception is that location
        //   permission was withdrawn.
	String s = getResources().getString (R.string.no_location_permission);
	setStatusMessage (s);
	}
      }
    }
  }

