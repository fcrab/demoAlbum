package com.crabfibber.fccamalbum.data;

import java.util.ArrayList;
import java.util.List;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.controller.PreviewActivity;
import com.crabfibber.fccamalbum.view.CustomGridItemView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 不带相机的相册gridview Adapter
 * @author fc
 *
 */
public class MediaItemAdapter extends BaseAdapter {
	private List<String> list;
	private LayoutInflater inflater;
	private LruCache<String, Bitmap> cache;
	private static final String TAG = "MediaItemAdapter";
	private static Context context;
//	private GridView gridView;
	private Handler handler;
	public static final int UPDATE_UI=0x00F1;
	public static final int SELECT_IMAGE=0x00F2;

	private List<Integer> waitList;
	private BitmapworkTask workTask;

	public MediaItemAdapter(Context context,Handler handler, List<String> list,
			LruCache<String, Bitmap> cache) {
		super();
		this.context = context;
		this.handler=handler;
		this.list = list;
		this.cache = cache;
		waitList = new ArrayList<Integer>();
		inflater = LayoutInflater.from(context);
		beginTask();
	}
	
	public void resetData(List<String> list){
		workTask.cancel(true);
		this.list=list;
		beginTask();
	}
	
	public void resetWaitingList(List<Integer> list){		
//		if(workTask.getStatus()==Status.FINISHED)
		waitList.clear();
		waitList.addAll(list);
		beginTask();
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private static Bitmap bitmap;
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImgHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.album_item_layout, null);
			holder = new ImgHolder();
			holder.mCheckBox = (CheckBox) convertView
					.findViewById(R.id.thumb_check);
			holder.mView = (CustomGridItemView) convertView
					.findViewById(R.id.thumb_image);
			convertView.setTag(holder);
		} else {
			holder = (ImgHolder) convertView.getTag();
		}
		final String filePos = list.get(position);
		holder.mCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.v(TAG, "checkOnClick");
						
						
					}
				});
		holder.mView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(TAG,"mViewClick");
				//点击预览
				Intent intent=new Intent(context, PreviewActivity.class);					
				intent.putExtra("photoUri", filePos);
				context.startActivity(intent);
			}
		});
		// load image
		bitmap = null;
		holder.mView.setTag(filePos);
		synchronized (cache) {
			bitmap = getBitmapFromLru(filePos);
		}
		if (bitmap == null) {
			waitList.add(position);
			//设定默认显示
			holder.mView.setImageResource(R.drawable.cpimage_photo_bg);
			
			// holder.mView.setImageResource(R.drawable.cpimage_photo_bg);
			// BitmapworkTask task=new BitmapworkTask();
			// task.execute(filePos,holder.mView);
			// bitmap=getImage(filePos);
			// addBitmapToLru(filePos, bitmap);
		} else {
			holder.mView.setImageBitmap(bitmap);
		}
		
		return convertView;
	}

	
	
	private static class ImgHolder {
		public CustomGridItemView mView;
		public CheckBox mCheckBox;
	}

	// get thumb image without LRU cache
	private Bitmap getImage(String path) {
		BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
		factoryOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, factoryOptions);
		int imgWidth = factoryOptions.outWidth;
		int imgHeight = factoryOptions.outHeight;
		int scaleFactor = Math.max(imgWidth / 100, imgHeight / 100);
//		Log.v(TAG, "scaleFactor:" + String.valueOf(scaleFactor));
		factoryOptions.inJustDecodeBounds = false;
		factoryOptions.inSampleSize = scaleFactor;
		factoryOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(path, factoryOptions);

		return bitmap;
	}

	/*
	 * LRUcache read and write
	 */
	private void addBitmapToLru(String key, Bitmap bitmap) {
//		Log.v(TAG, "adding to lru : " + key);
		if (getBitmapFromLru(key) == null) {
			if (key == null) {
				Log.v(TAG, "the key is null");
			}
			if (bitmap == null) {
				Log.v(TAG, "bitmap is null");
			} else {
				cache.put(key, bitmap);
			}
		}
	}

	private Bitmap getBitmapFromLru(String key) {
		return cache.get(key);
	}
	private void beginTask(){
		//每一个只能启动一次
		workTask = new BitmapworkTask();			
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			workTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}else{
			workTask.execute();			
		}
		
	}
	
	//中止图片加载
	public void finishTask() {
		Log.v(TAG,"cancelling the workTask");
		workTask.cancel(true);
	}

	/*
	 * new Thread: load image
	 */
	class BitmapworkTask extends AsyncTask<Object, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Object... params) {
			while (true) {
				
				if(isCancelled()){
					Log.v(TAG,"the task is cancelled");
					break;
				}

				int tmlPos=0;
				synchronized (waitList) {
					try {
						waitList.wait();
						if (waitList!=null&&waitList.size() != 0) {
							tmlPos = waitList.get(0);
							waitList.remove(0);
						}
						waitList.notify();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				String url=list.get(tmlPos);
//						Log.v(TAG, "waiting to download:" + url);
				final Bitmap bitmap = getImage(url);
				if(bitmap!=null){
					addBitmapToLru(url, bitmap);
					Message msg=new Message();
					msg.what=UPDATE_UI;
					Bundle data=new Bundle();
					data.putString("tag", url);
					msg.obj=bitmap;
					msg.setData(data);
					handler.sendMessage(msg);						
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			//没返回，没必要
			super.onPostExecute(result);
/*
			int tmlPos = waitList.get(0);
			String url=list.get(tmlPos); 
			((ImageView)gridView.findViewWithTag(url)).setImageBitmap(bitmap);
*/
		}
		
	}	
	
}
