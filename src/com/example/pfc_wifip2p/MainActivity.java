package com.example.pfc_wifip2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pfc_wifip2p.DeviceListFragment.DeviceActionListener;

public class MainActivity extends Activity implements ChannelListener, DeviceActionListener{
	
	
	private WifiP2pManager mManager;
	private Channel mChannel;
	private BroadcastReceiver mReceiver;
	private final IntentFilter mIntentFilter = new IntentFilter();
	private Context CONTEXT=this;
	private WifiP2pDevice device = new WifiP2pDevice();
	private WifiP2pConfig config = new WifiP2pConfig();
	public static final String TAG = "wifidirectdemo";

	
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;
	
	 //to use a toast to show message on screen 
    public void showMessage(String str){
	Toast.makeText(CONTEXT, str,Toast.LENGTH_SHORT).show();
    }
	
    //isWifiP2pEnabled the isWifiP2pEnabled to set
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Create the necessary manager and Channel
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    mChannel = mManager.initialize(this, getMainLooper(), null);
		
	    //Register for the events we need to capture
	
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
	    
	}
	
	//show device info on screen
    public void updateThisDevice(WifiP2pDevice device) {
        TextView view = (TextView)findViewById(R.id.mystatus);
        view.setText("My Name: "+device.deviceName+"\nMy Address: "+device.deviceAddress+"\nMy Status: "+getDeviceStatus(device.status));
        return;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() { // register the receiver on resume
        super.onResume();
	    mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {  // unregister on pause
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    
    
    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.devicelist);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.devicedetail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }
    
    public void searchButton(View view){
    	searchPeer();
    	return;
        }
    
	
    // Search Peers
	public void searchPeer(){
		
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
		    @Override
		    public void onSuccess() {
		        return;
		    }

		    @Override
		    public void onFailure(int reasonCode) {
		    	
		    	showMessage("Search Failed: "+ reasonCode);
		   //     Toast.makeText(CONTEXT, "Search Failed: "+reasonCode,Toast.LENGTH_SHORT).show();
                return;
		    }
		});
	}
	
	
    
 // prevent channel lose. if lost, try to create it again. 
    @Override
    public void onChannelDisconnected() {
        if (mManager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
        return;	
    }
    
    
    
    
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.devicedetail);
        fragment.showDetails(device);

    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        
        return true;
    }

	@Override
	public void cancelDisconnect() {
		 if (mManager != null) {
	            final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
	                    .findFragmentById(R.id.devicedetail);
	            if (fragment.device == null
	                    || fragment.device.status == WifiP2pDevice.CONNECTED) {
	                disconnect();
	            } else if (fragment.device.status == WifiP2pDevice.AVAILABLE
	                    || fragment.device.status == WifiP2pDevice.INVITED) {

	                mManager.cancelConnect(mChannel, new ActionListener() {

	                    @Override
	                    public void onSuccess() {
	                        showMessage("Aborting connection");
	                        return;
	                    }

	                    @Override
	                    public void onFailure(int reasonCode) {
	                        showMessage("Connect abort request failed. Reason Code: " + reasonCode);
	                    }
	                });
	            }
	        }
	        return;
		
	}

	@Override
	public void connect(WifiP2pConfig config) {
		// TODO Auto-generated method stub
		mManager.connect(mChannel, config, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// WifiDirectBroadcastReceiver will notify us
				
			}
			
			@Override
			public void onFailure(int reason) {
				showMessage ("Connect failed: "+reason);
				
			}
		});
    	return;
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
		final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.devicedetail);
        fragment.resetViews();
        mManager.removeGroup(mChannel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
            	 showMessage("Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
               // fragment.getView().setVisibility(View.GONE);
                showMessage("Disconnected.");
            }

        });
		
	}
	
	//map the status code with words
    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    
    
    
    
    

}
