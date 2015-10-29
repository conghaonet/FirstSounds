package org.hh.ts.vehicle;

import java.lang.ref.WeakReference;

import org.hao.ts.vehicle.R;
import org.hh.ts.vehicle.services.DownloadApkReceiver;
import org.hh.ts.vehicle.services.DownloadApkService;

import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Window;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener{
	private static final int HANDLER_MSG_APK_DOWNLOAD = 1;
	private MyApp myApp;
	private ProgressDialog downloadingDialog;
	private DownloadApkReceiver downloadApkReceiver;
	private long apkDownloadedSize;
	private long apkTotalSize;
	private MyHandler mHandler;
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
        getPreferenceScreen().removePreference(findPreference(getString(R.string.PREF_INVISIBLE_CATEGORY_KEY)));
		this.myApp = (MyApp)getApplicationContext();
		
        PreferenceScreen versionPref = (PreferenceScreen)findPreference(getString(R.string.PREF_ABOUT_VERSION_KEY));
        String versionName = this.myApp.getThisVersionName();
        if(versionName != null) versionPref.setTitle(getString(R.string.PREF_ABOUT_VERSION_TITLE)+" "+versionName);
        versionPref.setOnPreferenceClickListener(this);
		
        PreferenceScreen ratePref = (PreferenceScreen)findPreference(getString(R.string.PREF_ABOUT_RATE_KEY));
        ratePref.setOnPreferenceClickListener(this);
        
        if(AppPrefUtil.hasNewApkVersion(this, null)) {
            PreferenceScreen updatePref = (PreferenceScreen)findPreference(getString(R.string.PREF_UPDATE_KEY));
            updatePref.setOnPreferenceClickListener(this);
        } else {
        	getPreferenceScreen().removePreference(findPreference(getString(R.string.PREF_UPDATE_CATEGORY_KEY)));
        }
        downloadApkReceiver = new DownloadApkReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SettingsActivity.this.apkDownloadedSize = (Long)intent.getExtras().get(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE);
				SettingsActivity.this.apkTotalSize = (Long)intent.getExtras().get(DownloadApkService.INTENT_EXTRA_LONG_TOTAL_SIZE);
				SettingsActivity.this.mHandler.sendMessage(SettingsActivity.this.mHandler.obtainMessage(SettingsActivity.HANDLER_MSG_APK_DOWNLOAD));
			}
		};
        mHandler = new MyHandler(this);
        IntentFilter filterApkDownload = new IntentFilter();
        filterApkDownload.addAction(this.getPackageName()+DownloadApkReceiver.ACTION_DOWNLOAD_APK);
        registerReceiver(downloadApkReceiver, filterApkDownload);
	}
	static class MyHandler extends Handler {
        WeakReference<SettingsActivity> mActivity;
        MyHandler(SettingsActivity activity) {
        	mActivity = new WeakReference<SettingsActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	SettingsActivity theActivity = mActivity.get();
        	if(theActivity == null) return;
			switch (msg.what) {
        	case SettingsActivity.HANDLER_MSG_APK_DOWNLOAD:
        		if(theActivity.downloadingDialog == null) {
        			theActivity.startDownloadApk();
        		} else {
            		if(theActivity.downloadingDialog.isIndeterminate() && theActivity.apkTotalSize > 0) {
            			theActivity.downloadingDialog.setIndeterminate(false);
            			theActivity.downloadingDialog.setMax((int)(theActivity.apkTotalSize/1024));
            		}
            		theActivity.downloadingDialog.setProgress((int)(theActivity.apkDownloadedSize/1024));
            		if(theActivity.apkDownloadedSize == theActivity.apkTotalSize && theActivity.apkTotalSize > 0) {
            			theActivity.new InstallNewApk().execute();
            		}
        		}
        		break;   
			default:
				break;
			}
        }
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(getString(R.string.PREF_ABOUT_VERSION_KEY))) {
			doClickVersion();
		} else if(preference.getKey().equals(getString(R.string.PREF_ABOUT_RATE_KEY))) {
			doClickRate();
		} else if(preference.getKey().equals(getString(R.string.PREF_UPDATE_KEY))) {
			showUpdateDialog();
		}
		return true;
	}
	private void showUpdateDialog() {
		if(this.downloadingDialog != null) {
			startDownloadApk();
		} else {
			AlertDialog.Builder updateDialog = new AlertDialog.Builder(this);
			updateDialog.setTitle(R.string.DIALOG_APKUPDATE_TITLE);
			updateDialog.setMessage(R.string.DIALOG_APKUPDATE_MSG);
			updateDialog.setPositiveButton(
					R.string.DIALOG_BUTTON_UPDATE,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startDownloadApk();
						}
					}
				);
			updateDialog.setNegativeButton(
					R.string.DIALOG_BUTTON_LATER,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}
				);
			updateDialog.show();
		}
	}
	private void startDownloadApk() {
		if(this.downloadingDialog == null) {
			this.downloadingDialog = new ProgressDialog(this);
			this.downloadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.downloadingDialog.setTitle(R.string.PROGRESS_APK_DOWNLONGING_TITLE);
			this.downloadingDialog.setMessage(getString(R.string.PROGRESS_APK_DOWNLONGING_MSG));
			this.downloadingDialog.setIcon(R.drawable.ic_launcher);
			this.downloadingDialog.setCancelable(true);
			if(this.apkTotalSize > 0) {
				this.downloadingDialog.setIndeterminate(false);
				this.downloadingDialog.setMax((int)(this.apkTotalSize/1024));
				this.downloadingDialog.setProgress((int)(this.apkDownloadedSize/1024));
			} else {
				this.downloadingDialog.setIndeterminate(true);
//				this.downloadingDialog.setProgress(0);
				Intent apkDownloadIntent = new Intent(this, DownloadApkService.class);
				startService(apkDownloadIntent);
			}
		}
		this.downloadingDialog.show();
		this.mHandler.sendMessage(this.mHandler.obtainMessage(SettingsActivity.HANDLER_MSG_APK_DOWNLOAD));
	}

	private void doClickRate() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		AppPrefUtil.setRatedApp(this, editor, true);
		editor.commit();
        Uri rateUri = Uri.parse(AppConstants.RATE_APP_URL+getPackageName());
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, rateUri);
		try {
			Context googlePlayContext = SettingsActivity.this.createPackageContext(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, Context.CONTEXT_INCLUDE_CODE+Context.CONTEXT_IGNORE_SECURITY);
			Class mAssetBrowserActivity = googlePlayContext.getClassLoader().loadClass(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE + ".AssetBrowserActivity");
			rateIntent.setClassName(googlePlayContext, mAssetBrowserActivity.getName());
		} catch (Exception e) {
		}
		startActivity(rateIntent);
	}
	private void doClickVersion() {
		Uri uri = Uri.parse(AppConstants.MORE_GAMES_URL);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			Context googlePlayContext = this.createPackageContext(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, Context.CONTEXT_INCLUDE_CODE+Context.CONTEXT_IGNORE_SECURITY);
			Class mAssetBrowserActivity = googlePlayContext.getClassLoader().loadClass(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE + ".AssetBrowserActivity");
			intent.setClassName(googlePlayContext, mAssetBrowserActivity.getName());
		} catch (Exception e) {
		}
		startActivity(intent);
	}
	@Override
    public void onBackPressed() {
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
		this.finish();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterMyReceivers();
		System.gc();
	}
	private void unregisterMyReceivers() {
		try {
			if(this.downloadApkReceiver !=null) unregisterReceiver(this.downloadApkReceiver);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	class InstallNewApk extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				java.lang.Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
	    protected void onPostExecute(Void voida) {
			if(SettingsActivity.this == null || SettingsActivity.this.isFinishing()) return;
			if(SettingsActivity.this.downloadingDialog != null && SettingsActivity.this.downloadingDialog.isShowing()) {
				SettingsActivity.this.downloadingDialog.dismiss();
			}
			Uri uriApkFile = Uri.fromFile(myApp.getDownloadedFile());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uriApkFile, "application/vnd.android.package-archive");
			startActivity(intent);
		}
	}
}
