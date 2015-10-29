package org.hh.ts.vehicle;

import org.hao.ts.vehicle.R;
import org.hh.ts.vehicle.services.NetworkAvailableReceiver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;

public class MyApp extends Application {
	private static final String TAG = MyApp.class.getName();
	public static Hashtable<String, Class> tableUnfinishedServices = new Hashtable<String, Class>();
	public int gridColumns;
	public int gridRows;
	private List<CategoryEntity> listCategory;
	public Map<String, List<Integer>> picsMap = new HashMap<String, List<Integer>>();
	public Map<String, List<Integer>> musicMap = new HashMap<String, List<Integer>>();
	private Map<String, Integer> mapPicsCounterOfCategory;
	private Map<String, Integer> mapMusicCounterOfCategory;
	private LruCache<String, Bitmap> mMemoryCache;
	private Object memoryCacheLock = new Object();

	@Override
	public void onCreate() {
		super.onCreate();
		this.gridColumns = this.getResources().getInteger(R.integer.grid_columns);
		this.gridRows = this.getResources().getInteger(R.integer.grid_rows);
		InitAppPreference();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		AppPrefUtil.setRunTimesOfApp(this, editor, AppPrefUtil.getRunTimesOfApp(this, pref)+1);
		editor.commit();
		IntentFilter filterNetwork = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		NetworkAvailableReceiver networkReceiver = new NetworkAvailableReceiver();
		registerReceiver(networkReceiver, filterNetwork);
	}
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MyHttpClient.shutdown();
    }
 
    @Override
    public void onTerminate() {
        super.onTerminate();
        MyHttpClient.shutdown();
    }
    public LruCache<String, Bitmap> getMemCache() {
    	if(this.mMemoryCache == null) {
    		synchronized(this.memoryCacheLock) {
    			if(this.mMemoryCache == null) {
            		final int memClass = ((ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
//            		memClass = memClass > 64 ? 64 : memClass;
            		final int cacheSize = 1024 * 1024 * memClass / 8;
            		this.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            			@Override 
            	        protected int sizeOf(String key, Bitmap bitmap) {
            				return bitmap.getRowBytes() * bitmap.getHeight();
            			}
            			/*
            			@Override
                        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            				Log.d(TAG, "=========entryRemoved evicted="+evicted);
            				if(evicted) {
            					Log.d(TAG, "=========entryRemoved key="+key);
            					if(oldValue != null && !oldValue.isRecycled()) oldValue.recycle();
            					if(newValue != null && !newValue.isRecycled()) newValue.recycle();
            				}
            			}
            			*/
            		};
    			}
    			this.memoryCacheLock.notifyAll();
    		}
    	}
    	return this.mMemoryCache;
    }
	private void InitAppPreference() {
		SharedPreferences tempPref = this.getSharedPreferences(this.getPackageName() + AppConstants.DEFAULT_SHARED_PREFERENCES_SUFFIX, MODE_WORLD_READABLE + MODE_MULTI_PROCESS);
		if(tempPref.getAll().size()==0) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			tempPref.edit().putString("first_install_time", dateFormat.format(new Date())).commit();
		}
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		int inPrefVersionCode = AppPrefUtil.getApkVersionCode(this, pref);
		try {
			int intThisVersionCode = getThisVersionCode();
			if(inPrefVersionCode != intThisVersionCode) {
				Editor editor = pref.edit();
				AppPrefUtil.setApkVersionCode(this, editor, intThisVersionCode);
				editor.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private int getThisVersionCode() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}
	public String getThisVersionName() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(),0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "1.0";
		}
	}
	public String getConnectivityNetworkName() {
		ConnectivityManager connManager= (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if(info != null && info.isAvailable()) {
			return info.getTypeName();
		} return null;
	}
	public String getAppFilesPath(boolean isEndWithFileSeparator) {
		String strPath = null;
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			strPath =  this.getExternalFilesDir(null).getPath();
		} else {
			strPath =  this.getFilesDir().getPath();
		}
		if(isEndWithFileSeparator) return strPath+File.separator;
		else return strPath;
	}
	public File getDownloadedFile() {
		return new File(getAppFilesPath(true) + AppConstants.DOWNLOADED_APK_FILE);
	}

	public boolean isTablet() {
		return (getResources().getConfiguration().screenLayout & 
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	public com.google.android.gms.ads.AdSize getAdSizeForAdmob() {
		if(isTablet()) {
			return com.google.android.gms.ads.AdSize.LEADERBOARD;
		} else {
			return com.google.android.gms.ads.AdSize.BANNER;
		}
	}
	public List<CategoryEntity> getCategoryList() {
		if(this.listCategory != null && !this.listCategory.isEmpty()) return this.listCategory;
		this.listCategory = new ArrayList<CategoryEntity>();
		XmlResourceParser parser = getResources().getXml(R.xml.category);
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlResourceParser.END_DOCUMENT) {
				switch(eventType) {
				case XmlResourceParser.START_DOCUMENT:
					break;
				case XmlResourceParser.START_TAG:
					if(parser.getName().equals("category")) {
						String categoryName = parser.getAttributeValue(null, "category_name");
						String resName = parser.getAttributeValue(null, "res_name");
						int resXmlId = this.getResources().getIdentifier(resName, R.xml.class.getSimpleName(), this.getPackageName());
						int resPicId = this.getResources().getIdentifier(resName, R.drawable.class.getSimpleName(), this.getPackageName());
						if(resXmlId > 0 && resPicId > 0) {
							CategoryEntity entity = new CategoryEntity(categoryName, resXmlId, resPicId);
							this.listCategory.add(entity);
						} else System.exit(0);
					}
					break;
				case XmlResourceParser.END_TAG:
					break;
				case XmlResourceParser.END_DOCUMENT:
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.listCategory;
	}
	public void buildResMap(String categoryName) {
		if(this.picsMap.get(categoryName) == null || this.musicMap.get(categoryName) == null) {
			int categoryIndex = this.listCategory.indexOf(new CategoryEntity(categoryName, -1, -1));
			if(categoryIndex < 0) return;
			CategoryEntity categoryEntity = this.listCategory.get(categoryIndex);
			List<Integer> picsList = new ArrayList<Integer>();
			List<Integer> musicList = new ArrayList<Integer>();
			XmlResourceParser parser = getResources().getXml(categoryEntity.getResXmlId());
			try {
				int eventType = parser.getEventType();
				while (eventType != XmlResourceParser.END_DOCUMENT) {
					switch(eventType) {
					case XmlResourceParser.START_DOCUMENT:
						break;
					case XmlResourceParser.START_TAG:
						if(parser.getName().equals("pic")) {
							String resName = parser.getAttributeValue(null, "res_name");
							int resPicId = this.getResources().getIdentifier(resName, R.drawable.class.getSimpleName(), this.getPackageName());
							if(resPicId > 0) picsList.add(resPicId);
							else System.exit(0);
						} else if(parser.getName().equals("mp3")) {
							String resName = parser.getAttributeValue(null, "res_name");
							int resMp3Id = this.getResources().getIdentifier(resName, R.raw.class.getSimpleName(), this.getPackageName());
							if(resMp3Id > 0) musicList.add(resMp3Id);
							else System.exit(0);
						}
						break;
					case XmlResourceParser.END_TAG:
						break;
					case XmlResourceParser.END_DOCUMENT:
						break;
					}
					eventType = parser.next();
				}
				if(!picsList.isEmpty()) this.picsMap.put(categoryName, picsList);
				if(!musicList.isEmpty()) this.musicMap.put(categoryName, musicList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public Integer getPicFromCategory(String categoryName) {
		int picIndex = 0;
		if(this.mapPicsCounterOfCategory == null) {
			this.mapPicsCounterOfCategory = new HashMap<String, Integer>();
		} 
		Integer lastIndex = this.mapPicsCounterOfCategory.get(categoryName);
		if(lastIndex != null) picIndex = lastIndex + 1;
		if(this.picsMap.get(categoryName) == null) buildResMap(categoryName);
		if(picIndex >= this.picsMap.get(categoryName).size()) picIndex = 0;
		this.mapPicsCounterOfCategory.put(categoryName, picIndex);
		return this.picsMap.get(categoryName).get(picIndex);
	}
	public Integer getMusicFromCategory(String categoryName) {
		int mp3Index = 0;
		if(this.mapMusicCounterOfCategory == null) {
			this.mapMusicCounterOfCategory = new HashMap<String, Integer>();
		}
		Integer lastIndex = this.mapMusicCounterOfCategory.get(categoryName);
		if(lastIndex != null) mp3Index = lastIndex + 1;
		if(this.musicMap.get(categoryName) == null) buildResMap(categoryName);
		if(mp3Index >= this.musicMap.get(categoryName).size()) mp3Index = 0;
		this.mapMusicCounterOfCategory.put(categoryName, mp3Index);
		return this.musicMap.get(categoryName).get(mp3Index);
	}
}
