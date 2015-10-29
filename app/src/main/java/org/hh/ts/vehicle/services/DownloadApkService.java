package org.hh.ts.vehicle.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import org.hh.ts.vehicle.AppPrefUtil;
import org.hh.ts.vehicle.MyApp;
import org.hh.ts.vehicle.MyHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class DownloadApkService extends IntentService {
	private static final String TAG = DownloadApkService.class.getName();
	public static final String INTENT_EXTRA_LONG_DOWNLOADED_SIZE="INTENT_EXTRA_LONG_DOWNLOADED_SIZE";
	public static final String INTENT_EXTRA_LONG_TOTAL_SIZE="INTENT_EXTRA_LONG_TOTAL_SIZE";
	private MyApp myApp;
	public DownloadApkService() {
		super(TAG);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		boolean isSuccessful = false;
		if(this.myApp == null) this.myApp = (MyApp)this.getApplicationContext();
		try {
			if(ResourceServerConstants.ENABLED_SERVER.equalsIgnoreCase(ResourceServerConstants.SERVER_VPS_ALIYUN)) {
				isSuccessful = downloadApKByVpsAliyun();
			} else if(ResourceServerConstants.ENABLED_SERVER.equalsIgnoreCase(ResourceServerConstants.SERVER_OSS_ALIYUN)) {
				
			}
			if(!isSuccessful) MyApp.tableUnfinishedServices.put(this.getClass().getName(), this.getClass());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private boolean downloadApKByVpsAliyun() {
		boolean isSuccessful = false;
		String downloadUrl = ResourceServerConstants.VpsAliyun.APPUPDATE_BASE_URL + AppPrefUtil.getApkNewVersionUri(this, null);
		File apkFile = myApp.getDownloadedFile();
		long onlineContentLength = -1;
		String onlineLastModified = null;
		long expectedContentLength = AppPrefUtil.getApkNewVersionContentLength(this, null);
		String localLastModified = AppPrefUtil.getApkNewVersionLastModified(this, null);
		byte[] buffer = new byte[8192];
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		RandomAccessFile raf = null;
		HttpGet httpGet = new HttpGet(downloadUrl);
		try {
			HttpResponse response = MyHttpClient.getInstance().execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				onlineContentLength = response.getEntity().getContentLength();
				Header onlineLastModifiedHeader = response.getFirstHeader("Last-Modified");
				if(onlineLastModifiedHeader != null) onlineLastModified = onlineLastModifiedHeader.getValue();
				boolean isContinueDownload = false;
				if(onlineContentLength == expectedContentLength && apkFile.length() > 0
						&& onlineLastModified != null && localLastModified != null && onlineLastModified.equalsIgnoreCase(localLastModified)) {
					//to do the breakpoint continual transfer
					if(!httpGet.isAborted()) httpGet.abort();
					httpGet = new HttpGet(downloadUrl);
					httpGet.addHeader("Range", "bytes=" + apkFile.length() + "-");
					response = MyHttpClient.getInstance().execute(httpGet);
					Intent receiverIntent = new Intent(this.getPackageName()+DownloadApkReceiver.ACTION_DOWNLOAD_APK);
					receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_TOTAL_SIZE, onlineContentLength);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
						bis = new BufferedInputStream(response.getEntity().getContent());
						raf = new RandomAccessFile(apkFile, "rw");
						raf.seek(apkFile.length());
						receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE, apkFile.length());
						sendBroadcast(receiverIntent);
						long startTime = System.currentTimeMillis();
						long currentTime;
						int len = -1;
						while ((len = bis.read(buffer)) != -1) {
							raf.write(buffer, 0, len);
							currentTime = System.currentTimeMillis();
							if((currentTime - startTime) >= 1000 && apkFile.length() != onlineContentLength) {
								receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE, apkFile.length());
								sendBroadcast(receiverIntent);
								startTime = currentTime;
							}
						}
						raf.close();
						raf = null;
						bis.close();
						bis = null;
					}
					if(apkFile.length() == onlineContentLength) {
						isContinueDownload = true;
						isSuccessful = true;
						java.lang.Thread.sleep(1000);
						receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE, onlineContentLength);
						sendBroadcast(receiverIntent);
					}
				}
				if(!isContinueDownload) {
					//restart download apk file
					if(httpGet != null && !httpGet.isAborted()) httpGet.abort();
					httpGet = new HttpGet(downloadUrl);
					response = MyHttpClient.getInstance().execute(httpGet);
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
					Editor editor = pref.edit();
					AppPrefUtil.setApkNewVersionContentLength(this, editor, onlineContentLength);
					AppPrefUtil.setApkNewVersionLastModified(this, editor, onlineLastModified);
					editor.commit();
					
					if(apkFile.exists()) apkFile.delete();
					bis = new BufferedInputStream(response.getEntity().getContent());
					bos = new BufferedOutputStream(new FileOutputStream(apkFile));
					Intent receiverIntent = new Intent(this.getPackageName()+DownloadApkReceiver.ACTION_DOWNLOAD_APK);
					receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE, 0l);
					receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_TOTAL_SIZE, onlineContentLength);
					sendBroadcast(receiverIntent);
					long startTime = System.currentTimeMillis();
					long currentTime;
					int len = -1;
					while ((len = bis.read(buffer)) != -1) {
						bos.write(buffer, 0, len);
						currentTime = System.currentTimeMillis();
						if((currentTime - startTime) >= 1000 && apkFile.length() != onlineContentLength) {
							receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE, apkFile.length());
							sendBroadcast(receiverIntent);
							startTime = currentTime;
						}
					}
					bos.flush();
					bos.close();
					bos = null;
					bis.close();
					bis = null;
					java.lang.Thread.sleep(1000);
					receiverIntent.putExtra(DownloadApkService.INTENT_EXTRA_LONG_DOWNLOADED_SIZE, onlineContentLength);
					sendBroadcast(receiverIntent);
					isSuccessful = true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(httpGet != null && !httpGet.isAborted()) {
				httpGet.abort();
			}
			if(bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(raf != null) {
				try {
					raf.close();
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
}
