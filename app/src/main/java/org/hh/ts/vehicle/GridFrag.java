package org.hh.ts.vehicle;

import org.hao.ts.vehicle.R;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

public class GridFrag extends Fragment {
	private static final String TAG = GridFrag.class.getName();
	private MyApp myApp;
	private GridView gridView;
	private MainActivityCallBack mCallBack;
	private int imageMaxWidth;
	private List<CategoryEntity> listCategory;
	private MyGridAdapter myGridAdapter;
	private AsyncCategoryIconLoader asyncImgLoader;
	private ImageView arrImageView[];

	/**
	 * 当该Fragment被添加,显示到Activity时调用该方法
	 * 在此判断显示到的Activity是否已经实现了接口
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, TAG+"--onAttach");
		if (!(activity instanceof MainActivityCallBack)) {
			throw new IllegalStateException("GridFrag所在的Activity必须实现MainActivityCallBack接口");
		}
		mCallBack = (MainActivityCallBack)activity;
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TAG+"--onCreate");
        this.myApp = (MyApp)this.getActivity().getApplicationContext();
		this.listCategory = myApp.getCategoryList();
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, TAG+"--onCreateView");
		View rootView = inflater.inflate(R.layout.grid_fragment, container, false);
		LinearLayout bottomLayout = (LinearLayout)rootView.findViewById(R.id.grid_fragment_bottom_layout);
		bottomLayout.setMinimumHeight(mCallBack.getAdLayoutHeight());
		int availableHeight = mCallBack.getAvailableScreenHeight();
		int availableWidth = this.getResources().getDisplayMetrics().widthPixels;
//		for testing
//		availableWidth = 480; 
		if(availableHeight/myApp.gridRows > availableWidth/myApp.gridColumns) imageMaxWidth = availableWidth/myApp.gridColumns;
		else imageMaxWidth = availableHeight/myApp.gridRows;
		LinearLayout topLayout = (LinearLayout)rootView.findViewById(R.id.grid_fragment_top_layout);
		int topLayoutHeight = (availableHeight - (imageMaxWidth * myApp.gridRows)) / 2;
		if(topLayoutHeight < 0) topLayoutHeight = 0; 
		topLayout.setMinimumHeight(topLayoutHeight);
		arrImageView = new ImageView[this.listCategory.size()];
		if(asyncImgLoader == null) asyncImgLoader = new AsyncCategoryIconLoader(myApp, imageMaxWidth);
		gridView = (GridView)rootView.findViewById(R.id.grid_fragment_grid_view);
		myGridAdapter = new MyGridAdapter(this.getActivity());
		gridView.setAdapter(myGridAdapter);
		gridView.setOnItemClickListener(new MyItemClickListener());
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
		mCallBack = null;
	}

	final static class ViewHolder {
		public ImageView categoryImg;
	}
	class MyGridAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		public MyGridAdapter(Context context){
			this.mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return GridFrag.this.arrImageView.length;
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null) {
				holder=new ViewHolder();
				convertView = mInflater.inflate(R.layout.grid_item, null);
				if(arrImageView[position] == null) {
					arrImageView[position] = (ImageView)convertView.findViewById(R.id.grid_item_img);
				}
				holder.categoryImg = arrImageView[position];
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			if(position >= GridFrag.this.listCategory.size()) return convertView;
			CategoryEntity entity = GridFrag.this.listCategory.get(position);
			
			Bitmap subBitmap = asyncImgLoader.loadCategoryIconBitmap(entity,
					new AsyncCategoryIconLoader.ImageCallback() {
	                    public void imageLoaded(Bitmap bitmap) {
	        				if(bitmap != null) {
	        					setBitmap2View(position, bitmap);
	        				}
	                    }
			});
			if(subBitmap != null) {
				setBitmap2View(position, subBitmap);
			}
			return convertView;
		}
		private void setBitmap2View(int position, Bitmap bmp) {
			arrImageView[position].setImageBitmap(bmp);
		}
		
	}
	class MyItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			Toast.makeText(GridFrag.this.getActivity(), "item width=" + view.getWidth() + " item height="+view.getHeight() , Toast.LENGTH_LONG).show();
			String categoryName = GridFrag.this.listCategory.get(position).getName();
			mCallBack.openImgFrag(categoryName);
		}
	}

}
