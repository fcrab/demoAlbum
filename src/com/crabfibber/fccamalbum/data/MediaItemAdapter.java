package com.crabfibber.fccamalbum.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.controller.PreviewActivity;
import com.crabfibber.fccamalbum.model.AlbumModel;
import com.crabfibber.fccamalbum.utils.AlbumUtils;
import com.crabfibber.fccamalbum.view.CustomGridItemView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.widget.Toast;

/**
 * 不带相机的相册GridView Adapter
 * @author fc
 *
 */
public class MediaItemAdapter extends BaseAdapter implements Handler.Callback{
	private List<String> list;
	private LayoutInflater inflater;
	private LruCache<String, Bitmap> cache;
	private static final String TAG = "MediaItemAdapter";
	private static Context context;
//	private GridView gridView;
	private Handler handler;
	private Handler mainHandler;
	public static final int UPDATE_UI=0x00F1;
	public static final int SELECT_IMAGE=0x00F2;
	public static final int UPDATE_SELECT_RESULT=0x00F3;   //更新选择结果
	private static final int ERROR_OCCUR=0xFFFF;           //图片出错
	private static final int BLACKET_IS_FULL=0xFFFE;       //已选满
	private List<Integer> waitList;
	private BitmapworkTask workTask;
	private Map<Integer,Boolean> checkMap;

	public MediaItemAdapter(Context context,Handler handler, List<String> list,
			LruCache<String, Bitmap> cache) {
		super();
		this.context = context;
//		this.handler=handler;
		this.mainHandler=handler;
		this.handler=new Handler(context.getMainLooper(), this);		
		this.list = list;
		this.cache = cache;
		waitList = new ArrayList<Integer>();
		inflater = LayoutInflater.from(context);
        initCheckMap();
		beginTask();
	}
	
	public void resetData(List<String> list){
		workTask.cancel(true);
        this.list=list;
		initCheckMap();
		beginTask();
	}
	
	public void resetWaitingList(List<Integer> list){		
//		if(workTask.getStatus()==Status.FINISHED)
	    initCheckMap();
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
		final boolean isFileExist=new File(filePos).exists();
//		Log.v(TAG,"position:"+String.valueOf(position));
		final int currentPos=position;
		if(!isFileExist){     //如果图片不存在，不可选
		    holder.mCheckBox.setClickable(false);
		}else{
	        holder.mCheckBox.setChecked(checkMap.get(position));
	        holder.mCheckBox.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Log.v(TAG, "checkOnClick:"+String.valueOf(((CheckBox)v).isChecked()));
                    boolean isChecked=((CheckBox)v).isChecked();
                    if(isChecked){
                        if(AlbumModel.resultPath.size()>=AlbumModel.photoNumber){       //已经选满了 
                            ((CheckBox)v).setChecked(false);                        
                            handler.sendEmptyMessage(BLACKET_IS_FULL);
                        }else{          //选中
                            AlbumModel.resultPath.add(filePos);
                            checkMap.put(currentPos, isChecked);
                        }
                    }else{              //取消选择
                        AlbumModel.resultPath.remove(filePos);
                        checkMap.put(currentPos, isChecked);
                    }
                    //通知界面更新
                    mainHandler.sendEmptyMessage(UPDATE_SELECT_RESULT);
                }
            });
		}
		//图片点击事件
		holder.mView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(TAG,"mViewClick");
				//预览
				if(isFileExist){
	                Intent intent=new Intent(context, PreviewActivity.class);                   
	                intent.putExtra("photoUri", filePos);
	                context.startActivity(intent);				    
				}else{
				    Log.v(TAG,"无法找到该图片文件");
				    handler.sendEmptyMessage(ERROR_OCCUR);
				}
			}
		});
		// load image
		bitmap = null;
		holder.mView.setTag(filePos);
		if(isFileExist){
	        synchronized (cache) {
	            bitmap = getBitmapFromLru(filePos);
	        }
	        if (bitmap == null) {
	            waitList.add(position);
	            //如果无法找到图片，设置为背景图片
	            holder.mView.setImageResource(R.drawable.cpimage_photo_bg);
	            
	            // holder.mView.setImageResource(R.drawable.cpimage_photo_bg);
	            // BitmapworkTask task=new BitmapworkTask();
	            // task.execute(filePos,holder.mView);
	            // bitmap=getImage(filePos);
	            // addBitmapToLru(filePos, bitmap);
	        } else {
	            holder.mView.setImageBitmap(bitmap);
	        }		    
		}else{
            //如果无法找到图片，设置为背景图片
            holder.mView.setImageResource(R.drawable.cpimage_photo_bg);		    
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
		
		//旋转
		int degree=AlbumUtils.readPictureDegree(path);
		
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
//        Log.v(getClass().toString(), "the Rotate degree is : "+String.valueOf(degree));       
        if(bitmap!=null){
            bitmap=Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);            
        }
		
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
		//开始加载图片
		workTask = new BitmapworkTask();			
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			workTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}else{
			workTask.execute();			
		}
		
	}
	
	//结束加载
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
		    int currentThread=AlbumModel.threadCounter++;
		    Log.v(TAG,"new Thread:"+String.valueOf(currentThread));
			while (true) {
				//被终止
				if(isCancelled()){
					Log.v(TAG,"the task is cancelled:"+currentThread);
					break;
				}
				
				int tmlPos=-1;
				synchronized (waitList) {
					try {
						if (waitList!=null&&waitList.size() != 0) {
							tmlPos = waitList.get(0);
							waitList.remove(0);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(tmlPos!=-1){
				    if(tmlPos>=list.size()){
				        Log.v(TAG,"position is OutOfIndex");
				        break;
				    }
	                String url=list.get(tmlPos);                
	                final Bitmap bitmap = getImage(url);
	                if(bitmap!=null){
	                    addBitmapToLru(url, bitmap);
	                    Message msg=new Message();
	                    msg.what=UPDATE_UI;
	                    Bundle data=new Bundle();
	                    data.putString("tag", url);
	                    msg.obj=bitmap;
	                    msg.setData(data);
	                    mainHandler.sendMessage(msg);                       
	                }				    
				}else{
				    try {
                        Thread.sleep(1000);
                        if(waitList==null||waitList.size()==0){
                            Log.v(TAG,"the task is finished:"+currentThread);
                            break;
                        }
                    } catch (Exception e) {
                        if(e instanceof InterruptedException){
                            //由于外部call了cancel，因此抛出了该Exception。可以在这里搞点动作
                        }else{
                            e.printStackTrace();                            
                        }
                    } 
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
/*
			int tmlPos = waitList.get(0);
			String url=list.get(tmlPos); 
			((ImageView)gridView.findViewWithTag(url)).setImageBitmap(bitmap);
*/
		}
		
	}

    //Adapter的Handler响应
	@Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case ERROR_OCCUR:
                Toast.makeText(context, "无法找到该图片", Toast.LENGTH_LONG).show();
                break;
            case BLACKET_IS_FULL:
                Toast.makeText(context, "已选足够图片，无法继续添加", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
	    
	    
        return false;
    }	
	
	//初始化选中的内容
	private void initCheckMap(){
	    if(checkMap==null){
	        checkMap=new HashMap<Integer, Boolean>();	        
	    }
	    checkMap.clear();
	    
	    for(int counti=0;counti<list.size();counti++){
	        checkMap.put(counti, false);
	        if(AlbumModel.resultPath!=null&&AlbumModel.resultPath.size()!=0){
	            if(AlbumModel.resultPath.contains(list.get(counti))){
	                checkMap.put(counti, true);
	            }
	        }
	    }
	}
	
		
}
