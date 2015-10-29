package org.hh.ts.vehicle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

public class AsyncCategoryIconLoader {
	private ExecutorService executorService = Executors.newFixedThreadPool(3);
	private final Handler handler = new Handler();
	private MyApp myApp;
	private int categoryIconInSampleSize;
	private int catetoryIconMaxWidth;
	
	public AsyncCategoryIconLoader(Context context, int catetoryIconMaxWidth) {
		myApp = (MyApp)context;
		this.catetoryIconMaxWidth = catetoryIconMaxWidth;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(myApp.getResources(), myApp.getCategoryList().get(0).getResPicId(), opts);
		this.categoryIconInSampleSize = opts.outWidth / catetoryIconMaxWidth;
		if(this.categoryIconInSampleSize < 1) this.categoryIconInSampleSize = 1;
	}
	private String getCategoryIconCacheKey(String categoryName) {
		return "category_icon_"+categoryName;
	}
	public Bitmap loadCategoryIconBitmap(final CategoryEntity categoryEntity, final ImageCallback callback) {
		final String categoryIconCacheKey = getCategoryIconCacheKey(categoryEntity.getName());
		Bitmap cacheSubBitmap = myApp.getMemCache().get(categoryIconCacheKey);
		if(cacheSubBitmap != null && !cacheSubBitmap.isRecycled()) return cacheSubBitmap;
		executorService.submit(new Runnable() {
			public void run() {
				try {
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = false;
					opts.inSampleSize = AsyncCategoryIconLoader.this.categoryIconInSampleSize;
					Bitmap tempBitmap = BitmapFactory.decodeResource(AsyncCategoryIconLoader.this.myApp.getResources(), categoryEntity.getResPicId(), opts);
					if(catetoryIconMaxWidth != tempBitmap.getWidth()) {
						Bitmap scaledBtimap = Bitmap.createScaledBitmap(tempBitmap, catetoryIconMaxWidth, catetoryIconMaxWidth, true);
						tempBitmap.recycle();
						tempBitmap = scaledBtimap;
						scaledBtimap = null;
					}
					final Bitmap bitmap = tempBitmap;
					if(bitmap !=null) {
						addToCache(categoryIconCacheKey, bitmap);
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
