package org.hh.ts.vehicle;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import org.hao.ts.vehicle.R;
import org.hh.ts.vehicle.services.ApkUpdateReceiver;
import org.hh.ts.vehicle.services.DownloadApkReceiver;
import org.hh.ts.vehicle.services.DownloadApkService;
import org.hh.ts.vehicle.services.MyAdsReceiver;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements MainActivityCallBack {
	private static final String TAG = MainActivity.class.getName();
	private static final int HANDLER_MSG_NEWVERSION_HAS_SHOW_DIALOG = 101;
	private static final int HANDLER_MSG_AD_CHANGED = 102;
	private static final int HANDLER_MSG_APK_DOWNLOAD = 103;
	public ImageView fullImageView;
	public RelativeLayout relativeLayoutBlocksRoot;
	private AdView adView;
	private LinearLayout adLayout;
	private MyApp myApp;
	private FragmentManager fragmentManager;
	private ProgressDialog apkDownloadingDialog;
	private ApkUpdateReceiver apkUpdateReceiver;
	private DownloadApkReceiver downloadApkReceiver;
	private MyAdsReceiver myAdsReceiver;
	private MyHandler mHandler;
	private ImageButton btnApkUpdate;
	private int statusBarHeight;
	private long apkDownloadedSize;
	private long apkTotalSize;
	private long exitTime = 0;
	private boolean gridFragClickable;
	private boolean isExit;
	private static int RANDOM_SEED_VALUE;
	private Random mRandom = new Random();
	private static final int ANIMATIONS_ARRAY[][] = {
		{R.anim.fade_in,R.anim.fade_out},
		{R.anim.push_left_in,R.anim.push_left_out},
		{R.anim.push_right_in,R.anim.push_right_out},
		{R.anim.push_top_in,R.anim.push_top_out},
		{R.anim.push_bottom_in,R.anim.push_bottom_out},
		{R.anim.push_left_in,R.anim.push_right_out},
		{R.anim.push_right_in,R.anim.push_left_out},
		{R.anim.push_top_in,R.anim.push_bottom_out},
		{R.anim.push_bottom_in,R.anim.push_top_out},
		{R.anim.scale_in,R.anim.scale_out},
		{R.anim.push_lefttop_in,R.anim.push_lefttop_out},
		{R.anim.push_leftbottom_in,R.anim.push_leftbottom_out},
		{R.anim.push_righttop_in,R.anim.push_righttop_out},
		{R.anim.push_rightbottom_in,R.anim.push_rightbottom_out},
		{R.anim.push_lefttop_in,R.anim.push_rightbottom_out},
		{R.anim.push_leftbottom_in,R.anim.push_righttop_out},
		{R.anim.push_righttop_in,R.anim.push_leftbottom_out},
		{R.anim.push_rightbottom_in,R.anim.push_lefttop_out},
		{R.anim.rotate_center_minus_in,R.anim.rotate_center_minus_out},
		{R.anim.rotate_center_plus_in,R.anim.rotate_center_plus_out},
		{R.anim.rotate_lefttop_plus_in,R.anim.rotate_lefttop_plus_out},
		{R.anim.rotate_lefttop_minus_in,R.anim.rotate_lefttop_minus_out},
//		{R.anim.rotate_lefttop_plus_in,R.anim.rotate_lefttop_minus_out},
//		{R.anim.rotate_lefttop_minus_in,R.anim.rotate_lefttop_plus_out},
		{R.anim.rotate_leftbottom_plus_in,R.anim.rotate_leftbottom_plus_out},
		{R.anim.rotate_leftbottom_minus_in,R.anim.rotate_leftbottom_minus_out},
//		{R.anim.rotate_leftbottom_plus_in,R.anim.rotate_leftbottom_minus_out},
//		{R.anim.rotate_leftbottom_minus_in,R.anim.rotate_leftbottom_plus_out},
		{R.anim.rotate_rightbottom_plus_in,R.anim.rotate_rightbottom_plus_out},
		{R.anim.rotate_rightbottom_minus_in,R.anim.rotate_rightbottom_minus_out},
//		{R.anim.rotate_rightbottom_plus_in,R.anim.rotate_rightbottom_minus_out},
//		{R.anim.rotate_rightbottom_minus_in,R.anim.rotate_rightbottom_plus_out},
		{R.anim.rotate_righttop_plus_in,R.anim.rotate_righttop_plus_out},
		{R.anim.rotate_righttop_minus_in,R.anim.rotate_righttop_minus_out}
//		{R.anim.rotate_righttop_plus_in,R.anim.rotate_righttop_minus_out},
//		{R.anim.rotate_righttop_minus_in,R.anim.rotate_righttop_plus_out}
		};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		getSupportActionBar().hide();
		this.myApp = (MyApp)getApplicationContext();
		adLayout = (LinearLayout)findViewById(R.id.ad_layout);
//		TextView versionTxt = (TextView)findViewById(R.id.version_txt);
//		versionTxt.setText("v"+this.myApp.getThisVersionName());
		btnApkUpdate = (ImageButton)findViewById(R.id.btn_update);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -10);
		animation.setDuration(300);
		animation.setRepeatMode(Animation.REVERSE);
		animation.setRepeatCount(Animation.INFINITE);
		btnApkUpdate.setAnimation(animation);
		animation.startNow();
		if(AppPrefUtil.hasNewApkVersion(this, null)) openAnotherApp();
		else btnApkUpdate.setVisibility(View.GONE);
		if(AppPrefUtil.isAllAnimation(this, null)) MainActivity.RANDOM_SEED_VALUE = ANIMATIONS_ARRAY.length;
		else MainActivity.RANDOM_SEED_VALUE = 1;
		if(AppPrefUtil.isUpBrightness(this, null)) upBrightness();
		setBanner();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (savedInstanceState == null) {
			fragmentManager = getSupportFragmentManager();
		}
		this.mHandler = new MyHandler(this);
		myAdsReceiver = new MyAdsReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				MainActivity.this.mHandler.sendMessage(MainActivity.this.mHandler.obtainMessage(MainActivity.HANDLER_MSG_AD_CHANGED));
			}
		};
		apkUpdateReceiver = new ApkUpdateReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				MainActivity.this.mHandler.sendMessage(MainActivity.this.mHandler.obtainMessage(MainActivity.HANDLER_MSG_NEWVERSION_HAS_SHOW_DIALOG));
			}
		};
		downloadApkReceiver = new DownloadApkReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				MainActivity.this.apkDownloadedSize = (Long)intent.getExtras().get(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE);
				MainActivity.this.apkTotalSize = (Long)intent.getExtras().get(DownloadApkService.INTENT_EXTRA_LONG_TOTAL_SIZE);
				MainActivity.this.mHandler.sendMessage(MainActivity.this.mHandler.obtainMessage(MainActivity.HANDLER_MSG_APK_DOWNLOAD));
			}
		};
		IntentFilter filterAdChanged = new IntentFilter();
		filterAdChanged.addAction(this.getPackageName()+MyAdsReceiver.ACTION_AD_CHANGED);
		registerReceiver(myAdsReceiver, filterAdChanged);
		IntentFilter filterApkUpdate = new IntentFilter();
		filterApkUpdate.addAction(this.getPackageName()+ApkUpdateReceiver.ACTION_APPSUPDATE);
		registerReceiver(apkUpdateReceiver, filterApkUpdate);
		IntentFilter filterApkDownload = new IntentFilter();
		filterApkDownload.addAction(this.getPackageName()+DownloadApkReceiver.ACTION_DOWNLOAD_APK);
		registerReceiver(downloadApkReceiver, filterApkDownload);
	}
	static class MyHandler extends Handler {
        WeakReference<MainActivity> mActivity;
        MyHandler(MainActivity activity) {
        	mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	MainActivity theActivity = mActivity.get();
        	if(theActivity == null) return;
        	switch (msg.what) {
			case MainActivity.HANDLER_MSG_NEWVERSION_HAS_SHOW_DIALOG:
				theActivity.showUpdateDialog(null);
				if(theActivity.btnApkUpdate.getVisibility() != View.VISIBLE) {
					theActivity.btnApkUpdate.setVisibility(View.VISIBLE);
				}
				break;
			case MainActivity.HANDLER_MSG_AD_CHANGED:
				theActivity.setBanner();
				break;
			case MainActivity.HANDLER_MSG_APK_DOWNLOAD:
				if(theActivity.apkDownloadingDialog == null) {
					theActivity.startDownloadApk();
				} else {
					if(theActivity.apkDownloadingDialog.isIndeterminate() && theActivity.apkTotalSize > 0) {
						theActivity.apkDownloadingDialog.setIndeterminate(false);
						theActivity.apkDownloadingDialog.setMax((int)(theActivity.apkTotalSize/1024));
					}
					theActivity.apkDownloadingDialog.setProgress((int)(theActivity.apkDownloadedSize/1024));
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
	private void openAnotherApp() {
		try {
			String strNewApkPackage = AppPrefUtil.getApkNewVersionPackage(this, null);
			if(strNewApkPackage == null || strNewApkPackage.trim().equalsIgnoreCase("") || this.getPackageName().equals(strNewApkPackage)) {
				return;
			}
			PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageInfo(strNewApkPackage, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);
			ResolveInfo ri = apps.iterator().next();
			if (ri != null ) {
				String className = ri.activityInfo.name;
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName cn = new ComponentName(strNewApkPackage, className);
				intent.setComponent(cn);
				startActivity(intent);
				this.finish();
			}
		} catch(NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void showUpdateDialog(View v) {
		if(fragmentManager.findFragmentByTag(ImgFrag.class.getSimpleName()) != null) {
			this.fragmentManager.popBackStack();
			this.gridFragClickable = true;
		}
		if(this.apkDownloadingDialog != null) {
			startDownloadApk();
		} else {
			AlertDialog.Builder dialogApkupdate = new AlertDialog.Builder(this);
			dialogApkupdate.setCancelable(true);
			dialogApkupdate.setTitle(R.string.DIALOG_APKUPDATE_TITLE);
			dialogApkupdate.setMessage(R.string.DIALOG_APKUPDATE_MSG);
			dialogApkupdate.setPositiveButton(
					R.string.DIALOG_BUTTON_UPDATE,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startDownloadApk();
						}
					}
				);
			dialogApkupdate.setNegativeButton(
					R.string.DIALOG_BUTTON_LATER,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}
				);
			dialogApkupdate.show();
		}
	}
	private void startDownloadApk() {
		if(this.apkDownloadingDialog == null) {
			this.apkDownloadingDialog = new ProgressDialog(this);
			this.apkDownloadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.apkDownloadingDialog.setTitle(R.string.PROGRESS_APK_DOWNLONGING_TITLE);
			this.apkDownloadingDialog.setMessage(getString(R.string.PROGRESS_APK_DOWNLONGING_MSG));
			this.apkDownloadingDialog.setIcon(R.drawable.ic_launcher);
//			this.apkDownloadingDialog.setProgress(0);
//			this.apkDownloadingDialog.setIndeterminate(true);
			this.apkDownloadingDialog.setCancelable(true);
			if(this.apkTotalSize > 0) {
				this.apkDownloadingDialog.setIndeterminate(false);
				this.apkDownloadingDialog.setMax((int)(this.apkTotalSize/1024));
				this.apkDownloadingDialog.setProgress((int)(this.apkDownloadedSize/1024));
			} else {
				this.apkDownloadingDialog.setIndeterminate(true);
				Intent apkDownloadIntent = new Intent(MainActivity.this, DownloadApkService.class);
				startService(apkDownloadIntent);
			}
		}
		this.apkDownloadingDialog.show();
		this.mHandler.sendMessage(this.mHandler.obtainMessage(MainActivity.HANDLER_MSG_APK_DOWNLOAD));
	}
	private void unregisterMyReceivers() {
		try {
			if(this.apkUpdateReceiver != null) unregisterReceiver(this.apkUpdateReceiver);
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.myAdsReceiver != null) unregisterReceiver(this.myAdsReceiver);
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.downloadApkReceiver != null) unregisterReceiver(this.downloadApkReceiver);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus && statusBarHeight <= 0) {
			Rect frame = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			statusBarHeight = frame.top;
			GridFrag gridFrag = new GridFrag();
			FragmentTransaction trans = fragmentManager.beginTransaction();
			trans.setCustomAnimations(ANIMATIONS_ARRAY[0][0], ANIMATIONS_ARRAY[0][1]);
			trans.replace(R.id.fragment_container, gridFrag, GridFrag.class.getSimpleName());
			trans.commitAllowingStateLoss();
			this.gridFragClickable = true;
		}
	}
	@Override
	public void onBackPressed() {
		if(fragmentManager.findFragmentByTag(ImgFrag.class.getSimpleName()) != null) {
			super.onBackPressed();
			this.gridFragClickable = true;
		} else {
			if((System.currentTimeMillis() - exitTime) > 1000){
				exitTime = System.currentTimeMillis();
				Toast.makeText(this, R.string.exit_toast, Toast.LENGTH_SHORT).show();
			} else {
				this.isExit = true;
				finish();
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) adView.resume();
	}
	@Override
	public void onStart() {
       super.onStart();
       FlurryAgent.onStartSession(this, this.getString(R.string.flurry_api_key));
	}
	@Override
	public void onPause() {
		super.onPause();
		if(adView != null) adView.pause();
	}
	@Override
	public void onStop() {
      super.onStop();
      FlurryAgent.onEndSession(this);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterMyReceivers();
		if(this.adView != null) this.adView.destroy();
		if(this.isExit) {
			MyHttpClient.shutdown();
			this.myApp.getMemCache().evictAll();
			System.exit(0);
		}
	}
	@Override
	public int getAvailableScreenHeight() {
		return this.getResources().getDisplayMetrics().heightPixels - this.statusBarHeight - this.adLayout.getHeight();
	}
	@Override
	public int getAdLayoutHeight() {
		return this.adLayout.getHeight();
	}
	@Override
	public void openImgFrag(String categoryName) {
		if(!this.gridFragClickable) return;
		if(fragmentManager.findFragmentByTag(ImgFrag.class.getSimpleName()) != null) return;
		this.gridFragClickable = false;
		ImgFrag imgFrag = new ImgFrag();
		imgFrag.setCategoryName(categoryName);
		FragmentTransaction trans = fragmentManager.beginTransaction();
		int animationIndex = this.mRandom.nextInt(MainActivity.RANDOM_SEED_VALUE);
		trans.setCustomAnimations(ANIMATIONS_ARRAY[animationIndex][0],ANIMATIONS_ARRAY[animationIndex][1],ANIMATIONS_ARRAY[animationIndex][0],ANIMATIONS_ARRAY[animationIndex][1]);
		trans.replace(R.id.fragment_container, imgFrag, ImgFrag.class.getSimpleName());
		trans.addToBackStack(null);
		trans.commitAllowingStateLoss();
	}
	public void closeImgFrag(View v) {
		if(fragmentManager.findFragmentByTag(ImgFrag.class.getSimpleName()) == null) return;
		fragmentManager.popBackStack();
		this.gridFragClickable = true;
	}
	public void openSettings(View v) {
		Intent intent = new Intent();
		intent.setClass(this, SettingsActivity.class);
		startActivity(intent);
		this.finish();
	}
	private void upBrightness() {
		final float defaultBrightness = 0.8f;
		float brightness = 0;
		try {
			brightness = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		if(brightness/255f > defaultBrightness) brightness = brightness/255f;
		else brightness = defaultBrightness;
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness;
		getWindow().setAttributes(lp);
	}

	private void setBanner() {
		if(!AppTools.isLoadAds()) return;
		if(adView != null) adLayout.removeView(adView);
		try {
			adView = new AdView(this);
			adView.setAdSize(myApp.getAdSizeForAdmob());
			adView.setAdUnitId(AppPrefUtil.getAdBannerId(this, null));
			adLayout.addView(adView);
			AdRequest.Builder builder = new AdRequest.Builder();
//			builder.addTestDevice("96EDE742C567059B15AEE8871B8A9B21");//nexus5
			adView.loadAd(builder.build());
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
			if(MainActivity.this == null || MainActivity.this.isFinishing()) return;
			if(MainActivity.this.apkDownloadingDialog != null && MainActivity.this.apkDownloadingDialog.isShowing()) {
				MainActivity.this.apkDownloadingDialog.dismiss();
			}
			Uri uriApkFile = Uri.fromFile(myApp.getDownloadedFile());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uriApkFile, "application/vnd.android.package-archive");
			startActivity(intent);
		}
	}

}
