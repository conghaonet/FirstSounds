package org.hh.ts.vehicle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

public class AsyncFullImageLoader {
	private static final int ADAPTER_WIDTH_MIN = 480;
	private static final int ADAPTER_WIDTH_MID = 800;
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	private final Handler handler = new Handler();
	private MyApp myApp;
	private int fullImageInSampleSize;
	
	public AsyncFullImageLoader(Context context) {
		myApp = (MyApp)context;
		if(myApp.getResources().getDisplayMetrics().widthPixels <= ADAPTER_WIDTH_MIN) fullImageInSampleSize = 4;
		else if(myApp.getResources().getDisplayMetrics().widthPixels <= ADAPTER_WIDTH_MID) fullImageInSampleSize = 2;
		else fullImageInSampleSize = 1;
	}
	private String getFullImageCacheKey(String categoryName, int resId) {
		return "category_fullimg_"+categoryName+"_"+resId;
	}
	public Bitmap loadFullImageBitmap(final String categoryName, final ImageCallback callback) {
		final int fullImageResId = myApp.getPicFromCategory(categoryName);
		final String fullImageCacheKey = getFullImageCacheKey(categoryName, fullImageResId);
		Bitmap cacheBitmap = myApp.getMemCache().get(fullImageCacheKey);
		if(cacheBitmap != null && !cacheBitmap.isRecycled()) return cacheBitmap;
		executorService.submit(new Runnable() {
			public void run() {
				try {
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inPreferredConfig = Bitmap.Config.RGB_565;
					opts.inSampleSize = fullImageInSampleSize;
					opts.inJustDecodeBounds = false;
					Bitmap tempBitmap = BitmapFactory.decodeResource(myApp.getResources(), fullImageResId, opts);
					final Bitmap bitmap = tempBitmap;
					if(bitmap !=null) {
						addToCache(fullImageCacheKey, bitmap);
					}
					handler.post(new Runnable() {
						public void run() {
							callback.imageLoaded(bitmap);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		return null;
	}
	private void addToCache(String key, Bitmap bitmap) {
	    // Add to memory cache as before
		if(myApp.getMemCache() != null && myApp.getMemCache().get(key) == null) myApp.getMemCache().put(key, bitmap);
	}

	// Callback interface is open to the outside world
	public interface ImageCallback {
		// Note that this method is used to set the target object image resources
		public void imageLoaded(Bitmap imageBitmap);
	}
}
