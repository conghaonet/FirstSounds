package org.hh.ts.vehicle.services;

public class ResourceServerConstants {
	public static final String SERVER_OSS_ALIYUN = "OSS_ALIYUN";
	public static final String SERVER_VPS_ALIYUN = "VPS_ALIYUN";
	public static final String ENABLED_SERVER = SERVER_VPS_ALIYUN;
	
	public static class VpsAliyun {
		private static final String HOST = "http://oss.aliyuncs.com";
		public static final String ADS_XML_URL = HOST + "/hao-adsxml/ads_touchsound.xml";
		public static final String APPUPDATE_BASE_URL = HOST + "/hao-appupdate";
		public static final String APPUPDATE_XML_URL = APPUPDATE_BASE_URL + "/appupdate_touchsound.xml";
	}
}
