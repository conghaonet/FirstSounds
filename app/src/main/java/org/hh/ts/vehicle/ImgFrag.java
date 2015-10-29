package org.hh.ts.vehicle;

import org.hao.ts.vehicle.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImgFrag extends Fragment {
	private static final String TAG = ImgFrag.class.getName();
	private MyApp myApp;
	private String categoryName;
	private MediaPlayer myMediaPlayer;
	private MainActivityCallBack mCallBack;
	private AsyncFullImageLoader asyncFullImageLoader;
	
	/**
	 * 当该Fragment被添加,显示到Activity时调用该方法
	 * 在此判断显示到的Activity是否已经实现了接口
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, TAG+"--onAttach");
		if (!(activity instanceof MainActivityCallBack)) {
			throw new IllegalStateException("ImgFrag所在的Activity必须实现MainActivityCallBack接口");
		}
		mCallBack = (MainActivityCallBack)activity;
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TAG+"--onCreate");
        this.myApp = (MyApp)this.getActivity().getApplicationContext();
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, TAG+"--onCreateView");
		View rootView = inflater.inflate(R.layout.img_fragment, container, false);
		LinearLayout invisibleAdLayout = (LinearLayout)rootView.findViewById(R.id.img_fragment_invisible_adlayout);
		invisibleAdLayout.setMinimumHeight(mCallBack.getAdLayoutHeight());
		final ImageView imgFull = (ImageView)rootView.findViewById(R.id.img_fragment_imageview);
		if(asyncFullImageLoader == null) asyncFullImageLoader = new AsyncFullImageLoader(myApp);
		Bitmap subBitmap = asyncFullImageLoader.loadFullImageBitmap(categoryName,
				new AsyncFullImageLoader.ImageCallback() {
                    public void imageLoaded(Bitmap bitmap) {
        				if(bitmap != null) {
        					imgFull.setImageBitmap(bitmap);
        				}
                    }
		});
		if(subBitmap != null) {
			imgFull.setImageBitmap(subBitmap);
		}
		try {
			myMediaPlayer = MediaPlayer.create(this.getActivity(), myApp.getMusicFromCategory(categoryName));
//			AssetFileDescriptor fileDescriptor = asserter.openFd(myApp.getMusicFromCategory(categoryName));
//			myMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(), fileDescriptor.getLength());
//			myMediaPlayer.prepare();
			myMediaPlayer.setLooping(true);
			myMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, TAG+"--onActivityCreated");
	}
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, TAG+"--onStart");
	}
	@Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, TAG+"--onResume");
    }
	@Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, TAG+"--onPause");
    }
	@Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, TAG+"--onStop");
        if(myMediaPlayer != null) {
        	try {
            	myMediaPlayer.stop();
            	myMediaPlayer.release();
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        }
    }
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, TAG+"--onDestroyView");
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, TAG+"--onDestroy");
	}
	/**
	 * 当该FragmentA从它所属的Activity中被删除时调用该方法
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, TAG+"--onDetach");
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
}
