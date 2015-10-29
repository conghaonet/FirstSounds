package org.hh.ts.vehicle;

import org.hao.ts.vehicle.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppPrefUtil {
	public static int getRunTimesOfApp(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(pref.getString(context.getString(R.string.PREF_APP_RUNTIMES_KEY), "0"));
	}
	public static void setRunTimesOfApp(Context context, Editor editor, int runTimes) {
		editor.putString(context.getString(R.string.PREF_APP_RUNTIMES_KEY), String.valueOf(runTimes));
	}

	public static String getAdBannerId(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_AD_UNIT_ID_KEY), context.getString(R.string.ad_banner_id));
	}
	public static void setAdBannerId(Context context, Editor editor, String adBannerId) {
		editor.putString(context.getString(R.string.PREF_AD_UNIT_ID_KEY), adBannerId);
	}
	
	public static int getApkVersionCode(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(pref.getString(context.getString(R.string.PREF_APK_VERSION_CODE_KEY), "0"));
	}
	public static void setApkVersionCode(Context context, Editor editor, int versionCode) {
		editor.putString(context.getString(R.string.PREF_APK_VERSION_CODE_KEY), String.valueOf(versionCode));
	}

	public static String getAdsXmlLastModified(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_ADS_XML_LASTMODIFIED_KEY), null);
	}
	public static void setAdsXmlLastModified(Context context, Editor editor, String lastModified) {
		editor.putString(context.getString(R.string.PREF_ADS_XML_LASTMODIFIED_KEY), lastModified);
	}
	
	public static String getAppsupdateXmlLastModified(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_APPSUPDATE_XML_LASTMODIFIED_KEY), null);
	}
	public static void setAppsupdateXmlLastModified(Context context, Editor editor, String lastModified) {
		editor.putString(context.getString(R.string.PREF_APPSUPDATE_XML_LASTMODIFIED_KEY), lastModified);
	}

	public static int getApkNewVersionCode(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(pref.getString(context.getString(R.string.PREF_APK_NEWVERSION_CODE_KEY), "0"));
	}
	public static void setApkNewVersionCode(Context context, Editor editor, int versionCode) {
		editor.putString(context.getString(R.string.PREF_APK_NEWVERSION_CODE_KEY), String.valueOf(versionCode));
	}

	public static String getApkNewVersionPackage(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_APK_NEWVERSION_PACKAGE_KEY), null);
	}
	public static void setApkNewVersionPackage(Context context, Editor editor, String strPackage) {
		editor.putString(context.getString(R.string.PREF_APK_NEWVERSION_PACKAGE_KEY), strPackage);
	}

	public static String getApkNewVersionUri(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_APK_NEWVERSION_URL_KEY), null);
	}
	public static void setApkNewVersionUri(Context context, Editor editor, String uri) {
		editor.putString(context.getString(R.string.PREF_APK_NEWVERSION_URL_KEY), uri);
	}

	public static long getApkNewVersionContentLength(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return Long.parseLong(pref.getString(context.getString(R.string.PREF_APK_NEWVERSION_CONTENTLENGTH_KEY), "-2"));
	}
	public static void setApkNewVersionContentLength(Context context, Editor editor, long contentLength) {
		editor.putString(context.getString(R.string.PREF_APK_NEWVERSION_CONTENTLENGTH_KEY), String.valueOf(contentLength));
	}

	public static String getApkNewVersionLastModified(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_APK_NEWVERSION_LASTMODIFIED_KEY), null);
	}
	public static void setApkNewVersionLastModified(Context context, Editor editor, String lastModified) {
		editor.putString(context.getString(R.string.PREF_APK_NEWVERSION_LASTMODIFIED_KEY), lastModified);
	}
	public static boolean hasNewApkVersion(Context context, SharedPreferences pref) {
		return getApkNewVersionCode(context, pref) > getApkVersionCode(context, pref);
	}
	public static boolean isRatedApp(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(context.getString(R.string.PREF_APP_ISRATED_KEY), false);
	}
	public static void setRatedApp(Context context, Editor editor, boolean isRated) {
		editor.putBoolean(context.getString(R.string.PREF_APP_ISRATED_KEY), isRated);
	}
	public static boolean isUpBrightness(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(context.getString(R.string.PREF_BRIGHTNESS_KEY), true);
	}
	public static boolean isAllAnimation(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(context.getString(R.string.PREF_ANIMATION_KEY), true);
	}
}
