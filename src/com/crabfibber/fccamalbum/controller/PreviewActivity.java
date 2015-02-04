package com.crabfibber.fccamalbum.controller;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.model.AlbumModel;
import com.crabfibber.fccamalbum.utils.AlbumUtils;
import com.crabfibber.fccamalbum.view.CustomPreviewImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * photo_preview
 * @author fc
 *
 *2015/1/4 预览
 *2015/1/6 缩放
 *2015/1/22 点击选择
 *
 */

public class PreviewActivity extends Activity implements OnClickListener {
	private static final String TAG="PreviewActivity";
//	private ImageView preview;
	private CustomPreviewImageView customPreview;
	private Bitmap bitmap;
	private String path;
	
	private CheckBox selectBox;        //选择框
	
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
		selectBox=(CheckBox)findViewById(R.id.photo_selected);
		selectBox.setOnClickListener(this);
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
		
        //初始化
        if(AlbumModel.resultPath.contains(path)){
            selectBox.setChecked(true);
        }else{
            selectBox.setChecked(false);
        }
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.v(TAG,"imageView height:"+String.valueOf(customPreview.getHeight()));
		Log.v(TAG,"imageView width:"+String.valueOf(customPreview.getWidth()));		
		customPreview.setScreenSize(customPreview.getWidth(), customPreview.getHeight());

		int width=customPreview.getWidth();
		int height=customPreview.getHeight();
		if(width!=0&&height!=0){
			bitmap=getImage(width, height);      //获取图像
//			preview.setImageBitmap(bitmap);
			customPreview.setImageBitmap(bitmap);
		}else{
			Toast.makeText(PreviewActivity.this, "图片容器参数有误", Toast.LENGTH_LONG).show();
		}
	}
	
	private Bitmap getImage(int width,int height) {
		BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
		factoryOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, factoryOptions);
		int imgWidth = factoryOptions.outWidth;
		int imgHeight = factoryOptions.outHeight;
		int scaleFactor = Math.max(imgWidth / width, imgHeight / height);
		//旋转
		
//		Log.v(TAG, "scaleFactor:" + String.valueOf(scaleFactor));
		factoryOptions.inJustDecodeBounds = false;        
		factoryOptions.inSampleSize = scaleFactor;
		factoryOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(path, factoryOptions);

	    //旋转
        int degree=AlbumUtils.readPictureDegree(path);
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);        
        bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		
		return bitmap;
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_selected:
                if(((CheckBox)view).isChecked()){
                    AlbumModel.resultPath.add(path);
                }else{
                    if(AlbumModel.resultPath.contains(path)){
                        AlbumModel.resultPath.remove(path);
                    }
                }
                break;
            default:
                break;
        }
    }
	
}
