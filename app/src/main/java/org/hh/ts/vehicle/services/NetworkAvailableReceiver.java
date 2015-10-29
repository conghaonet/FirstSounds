package org.hh.ts.vehicle.services;

import java.util.Enumeration;

import org.hh.ts.vehicle.MyApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkAvailableReceiver extends BroadcastReceiver {
	private String lastNetworkName;
	private static final String TAG = NetworkAvailableReceiver.class.getName();
	public static final String ACTION_AVAILABLE = NetworkAvailableReceiver.class.getSimpleName()+"_ACTION_AVAILABLE";
	private MyApp myApp;
	public NetworkAvailableReceiver() {
		MyApp.tableUnfinishedServices.put(MyAdsService.class.getName(), MyAdsService.class);
		MyApp.tableUnfinishedServices.put(ApkUpdateService.class.getName(), ApkUpdateService.class);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
        	if(myApp == null) myApp = (MyApp)context.getApplicationContext();
            String currentNetworkName = myApp.getConnectivityNetworkName();
            if(currentNetworkName != null && !currentNetworkName.equals(lastNetworkName)) {
               	Enumeration<String> enumKeys = MyApp.tableUnfinishedServices.keys();
               	while(enumKeys.hasMoreElements()) {
               		String key = enumKeys.nextElement();
               		Class tempClass = MyApp.tableUnfinishedServices.remove(key);
               		context.startService(new Intent(context, tempClass));
               	}
            }
            lastNetworkName = currentNetworkName;
        }
	}
}
