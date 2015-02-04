package com.crabfibber.fccamalbum.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.controller.PreviewActivity;
import com.crabfibber.fccamalbum.view.CustomGridItemView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 *带相机的gridView
 * @author fc
 *
 */
public class CamItemAdapter extends BaseAdapter {

	private List<String> list;
	private LayoutInflater inflater;
	private LruCache<String, Bitmap> cache;
	private static final String TAG = "CamItemAdapter";
	private static Context context;
//	private GridView gridView;
	private Handler handler;
	public static final int UPDATE_UI=0x00F1;
	
	private List<Integer> waitList;
	private BitmapworkTask workTask;

	public CamItemAdapter(Context context,Handler handler, List<String> list,
			LruCache<String, Bitmap> cache) {
		super();
		this.context = context;
		this.handler=handler;
		this.list = list;
		this.cache = cache;
		waitList = new ArrayList<Integer>();
		inflater = LayoutInflater.from(context);
		workTask = new BitmapworkTask();
		workTask.execute();
	}

	//ֹͣ����
	public void finishTask() {
		workTask.cancel(true);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return null;
		} else {
			return list.get(position - 1);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// Ĭ��Ϊ1
	private static final int FIRST_ITEM = 0x01;
	private static final int OTHER_ITEM = 0x02;
	private static final int TYPE_COUNT = 3; // Ϊʲô�������֣���

	@Override
	public int getItemViewType(int position) {
		// return super.getItemViewType(position);
		if (position == 0) {
			return FIRST_ITEM;
		} else {
			return OTHER_ITEM;
		}
	}

	@Override
	public int getViewTypeCount() {
		Log.v(TAG, "getViewTypeCount has been call");
		return TYPE_COUNT;
	}

	private static Bitmap bitmap;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);
		switch (type) {
		case FIRST_ITEM: {
			BtnHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_button, null);
				holder = new BtnHolder();
				holder.mImgBtn = (ImageButton) convertView
						.findViewById(R.id.take_pic);
				convertView.setTag(holder);
			} else {
				holder = (BtnHolder) convertView.getTag();
			}
			holder.mImgBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.v(TAG, "take photo");
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);		//����
					context.startActivity(intent);
				}
			});
			break;
		}
		case OTHER_ITEM: {
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
			final String filePos = list.get(position - 1);
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
					//���Ԥ��
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
				waitList.add(position - 1);
				//�趨Ĭ����ʾ
				holder.mView.setImageResource(R.drawable.cpimage_photo_bg);
				
				// holder.mView.setImageResource(R.drawable.cpimage_photo_bg);
				// BitmapworkTask task=new BitmapworkTask();
				// task.execute(filePos,holder.mView);
				// bitmap=getImage(filePos);
				// addBitmapToLru(filePos, bitmap);
			} else {
				holder.mView.setImageBitmap(bitmap);
			}

			break;
		}
		default:
			break;
		}
		return convertView;
	}

	private static class ImgHolder {
		public CustomGridItemView mView;
		public CheckBox mCheckBox;
	}

	private static class BtnHolder {
		public ImageButton mImgBtn;
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

	/*
	 * new Thread: load image
	 */
	class BitmapworkTask extends AsyncTask<Object, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Object... params) {
			while (true) {
				if (waitList.size() != 0) {
					int tmlPos = waitList.get(0);
					String url=list.get(tmlPos);
//					Log.v(TAG, "waiting to download:" + url);
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
					
					waitList.remove(0);
				}
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			//û���أ�û��Ҫ
			super.onPostExecute(result);
/*
			int tmlPos = waitList.get(0);
			String url=list.get(tmlPos); 
			((ImageView)gridView.findViewWithTag(url)).setImageBitmap(bitmap);
*/
		}

		
		
	}

}
