package com.crabfibber.fccamalbum.controller;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.view.CustomPreviewImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * photo_preview
 * @author fc
 *
 *2015/1/4 预览
 *2015/1/6 缩放
 *
 */

public class PreviewActivity extends Activity {
	private static final String TAG="PreviewActivity";
//	private ImageView preview;
	private CustomPreviewImageView customPreview;
	private Bitmap bitmap;
	private String path;
	
	private int winWidth=0;
	private int winHeight=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_photo_preview);
		//获取传过来的数据
		path=getIntent().getStringExtra("photoUri");
		Log.v(TAG,path);
		
		customPreview=(CustomPreviewImageView)findViewById(R.id.custom_preview_iamge);
		
		
//		preview=(ImageView)findViewById(R.id.preview_image);
		
		//获取整个手机可见区域数据
		WindowManager manager=getWindowManager();
//		winWidth=manager.getDefaultDisplay().getWidth();		//api被弃用 use getSize()
//		winHeight=manager.getDefaultDisplay().getHeight();		
//		Log.v(TAG,"Win width:"+String.valueOf(winWidth));
//		Log.v(TAG,"Win height:"+String.valueOf(winHeight));
		Point sizeReceiver=new Point();
		manager.getDefaultDisplay().getSize(sizeReceiver);
		Log.v(TAG,"Win X(height):"+String.valueOf(sizeReceiver.x));
		Log.v(TAG,"Win Y(height):"+String.valueOf(sizeReceiver.y));
//		customPreview.setScreenSize(sizeReceiver.x, sizeReceiver.y);
		
	}

	@Override
	protected void onResume() {
		super.onResume();

	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.v(TAG,"imageView height:"+String.valueOf(customPreview.getHeight()));
		Log.v(TAG,"imageView width:"+String.valueOf(customPreview.getWidth()));		
		customPreview.setScreenSize(customPreview.getWidth(), customPreview.getHeight());
		//这样子还是不太好。因为会有延迟(好像也看不太出来)
		int width=customPreview.getWidth();
		int height=customPreview.getHeight();
		if(width!=0&&height!=0){
			bitmap=getImage(width, height);
//			preview.setImageBitmap(bitmap);
			customPreview.setImageBitmap(bitmap);
		}else{
			Toast.makeText(PreviewActivity.this, "图片容器参数有误", Toast.LENGTH_LONG);
		}
	}
	
	private Bitmap getImage(int width,int height) {
		BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
		factoryOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, factoryOptions);
		int imgWidth = factoryOptions.outWidth;
		int imgHeight = factoryOptions.outHeight;
		int scaleFactor = Math.max(imgWidth / width, imgHeight / height);
//		Log.v(TAG, "scaleFactor:" + String.valueOf(scaleFactor));
		factoryOptions.inJustDecodeBounds = false;
		factoryOptions.inSampleSize = scaleFactor;
		factoryOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(path, factoryOptions);

		return bitmap;
	}

	
}
