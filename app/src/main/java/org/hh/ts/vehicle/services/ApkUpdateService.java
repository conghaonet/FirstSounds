package org.hh.ts.vehicle.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.hao.ts.vehicle.R;
import org.hh.ts.vehicle.AppPrefUtil;
import org.hh.ts.vehicle.MyApp;
import org.hh.ts.vehicle.MyHttpClient;
import org.xmlpull.v1.XmlPullParser;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Xml;

public class ApkUpdateService  extends IntentService {
	private static final String TAG = ApkUpdateService.class.getName();
	private static final String APPUPDATE_XML_FILENAME="appupdate.xml";
	private MyApp myApp;
	
	public ApkUpdateService() {
		super(TAG);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		boolean isSuccessful = false;
		if(this.myApp == null) this.myApp = (MyApp)this.getApplicationContext();
//		if(AppPrefUtil.getRunTimesOfApp(this, null) <= 3) return;
		isSuccessful = downloadAppUpdateXml();
		if(isSuccessful) {
			isSuccessful = checkUpdate();
		}
		if(!isSuccessful) {
			MyApp.tableUnfinishedServices.put(this.getClass().getName(), this.getClass());
		}
	}
	private boolean downloadAppUpdateXml() {
		boolean isSuccessful = false;
		long onlineContentLength = -1;
		String onlineLastModified = null;
		File appupdateFile = new File(myApp.getAppFilesPath(true)+APPUPDATE_XML_FILENAME);
		String localLastModified = AppPrefUtil.getAppsupdateXmlLastModified(this, null);
		if(ResourceServerConstants.ENABLED_SERVER.equalsIgnoreCase(ResourceServerConstants.SERVER_VPS_ALIYUN)) {
			HttpGet httpGet = new HttpGet(ResourceServerConstants.VpsAliyun.APPUPDATE_XML_URL);
			byte[] buffer = new byte[2048];
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			try {
				HttpResponse response = MyHttpClient.getInstance().execute(httpGet);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					onlineContentLength = response.getEntity().getContentLength();
					try {
						onlineLastModified = response.getFirstHeader("Last-Modified").getValue();
					} catch(Exception e) {}
					if(appupdateFile.length() != onlineContentLength || localLastModified==null || onlineLastModified==null || !localLastModified.equalsIgnoreCase(onlineLastModified)) {
						bis = new BufferedInputStream(response.getEntity().getContent());
						bos = new BufferedOutputStream(new FileOutputStream(appupdateFile));
						int len = -1;
						while ((len = bis.read(buffer)) != -1) {
							bos.write(buffer, 0, len);
						}
						bos.flush();
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
						Editor editor = pref.edit();
						AppPrefUtil.setAppsupdateXmlLastModified(this, editor, onlineLastModified);
						editor.commit();
					}
					isSuccessful = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(httpGet != null && !httpGet.isAborted()) httpGet.abort();
				if(bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		} else if(ResourceServerConstants.ENABLED_SERVER.equalsIgnoreCase(ResourceServerConstants.SERVER_OSS_ALIYUN)) {			
		}
		return isSuccessful;
	}
	private boolean checkUpdate() {
		boolean isSuccessful = false;
		BufferedInputStream bis = null;
		try {
			File appsupdateFile = new File(this.myApp.getAppFilesPath(true)+APPUPDATE_XML_FILENAME);
			if(!appsupdateFile.exists() || appsupdateFile.length()<=0) return isSuccessful;
			bis = new BufferedInputStream(new FileInputStream(appsupdateFile));
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(bis, "UTF-8");
			boolean blnCategoryMatched =false;
			int onlineNewVersionCode = 0;
			String onlineNewVersionUrl = null;
			String onlineNewVersionPackage = null;
			int eventType = parser.getEventType();
			while(eventType!=XmlPullParser.END_DOCUMENT) {
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if(AppUpdateXml.NODE_CATEGORY.equals(parser.getName())) {
						if(parser.getAttributeValue("", AppUpdateXml.ATTR_NAME).equals(this.getString(R.string.appupdate_xml_category_name))) {
							blnCategoryMatched = true;
						}
					} else if(blnCategoryMatched) {
						if(AppUpdateXml.NODE_APP.equals(parser.getName())) {
							if(this.getString(R.string.appupdate_xml_package_code).equals(parser.getAttributeValue("", AppUpdateXml.ATTR_NAME))) {
								onlineNewVersionCode = Integer.parseInt(parser.getAttributeValue("", AppUpdateXml.ATTR_VERSION_CODE));
								onlineNewVersionUrl = parser.getAttributeValue("", AppUpdateXml.ATTR_URL);
								onlineNewVersionPackage = parser.getAttributeValue("", AppUpdateXml.ATTR_APP_PACKAGE);
							}
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				if(onlineNewVersionUrl != null) break;
				eventType = parser.next();
			}
			if(onlineNewVersionCode > 0 && onlineNewVersionUrl != null) {
				boolean isFirstFindNewVersion = false;
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				if(AppPrefUtil.getApkNewVersionCode(this, pref) != onlineNewVersionCode || AppPrefUtil.getApkNewVersionUri(this, pref) == null || !AppPrefUtil.getApkNewVersionUri(this, pref).equals(onlineNewVersionUrl)) {
					isFirstFindNewVersion = true;
					Editor editor = pref.edit();
					AppPrefUtil.setApkNewVersionCode(this, editor, onlineNewVersionCode);
					AppPrefUtil.setApkNewVersionUri(this, editor, onlineNewVersionUrl);
					AppPrefUtil.setApkNewVersionPackage(this, editor, onlineNewVersionPackage);
					editor.commit();
				}
				if(AppPrefUtil.hasNewApkVersion(this, null)) {
					if(isFirstFindNewVersion) {
						Intent tempIntent = new Intent(this.getPackageName() + ApkUpdateReceiver.ACTION_APPSUPDATE);
						sendBroadcast(tempIntent);
					}
				} else {
					File file = myApp.getDownloadedFile();
					if(file != null && file.exists()) file.delete();
				}
			}
			isSuccessful = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccessful;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private static final class AppUpdateXml {
		private static final String NODE_CATEGORY="category";
		private static final String NODE_APP="app";
		private static final String ATTR_NAME="name";
		private static final String ATTR_APP_PACKAGE="app_package";
		private static final String ATTR_VERSION_CODE="version_code";
		private static final String ATTR_URL="url";
	}
}
