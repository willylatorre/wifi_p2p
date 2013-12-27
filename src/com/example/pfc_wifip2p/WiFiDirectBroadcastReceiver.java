package com.example.pfc_wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.view.View;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
    		MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        	int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            	 mActivity.setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi P2P is not enabled
            	mActivity.setIsWifiP2pEnabled(false);
            	mActivity.resetData();
            }
       
        
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        	if(mManager != null){
        		mManager.requestPeers(mChannel,(PeerListListener)mActivity.getFragmentManager()
        				.findFragmentById(R.id.devicelist));
        	return;
        	}
        	
        	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection info to find group owner IP
        	final DeviceListFragment listfragment = (DeviceListFragment) mActivity.getFragmentManager().findFragmentById(R.id.devicelist);
        	listfragment.getView().setVisibility(View.GONE);
        	DeviceDetailFragment fragment = (DeviceDetailFragment) mActivity
                        .getFragmentManager().findFragmentById(R.id.devicedetail);
        	//fragment.showDetails(device);
        	mManager.requestConnectionInfo(mChannel, fragment);
            } else {
                // It's a disconnect. back to main menu
        	final DeviceListFragment listfragment = (DeviceListFragment) mActivity.getFragmentManager().findFragmentById(R.id.devicelist);
        	listfragment.getView().setVisibility(View.VISIBLE);
                final DeviceDetailFragment fragment = (DeviceDetailFragment) mActivity.getFragmentManager()
                        .findFragmentById(R.id.devicedetail);
                fragment.blockDetail();
            }
            return;
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        	mActivity.updateThisDevice((WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            return;
        }

    }
}
