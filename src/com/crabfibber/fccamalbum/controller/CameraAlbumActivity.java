package com.crabfibber.fccamalbum.controller;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.data.CamItemAdapter;
import com.crabfibber.fccamalbum.data.CatalogItemAdapter;
import com.crabfibber.fccamalbum.data.MediaItemAdapter;
import com.crabfibber.fccamalbum.model.AlbumModel;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.Layout;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * ���տؼ� mainActivity ���Ե�ѡ��ѡ����ת
 * 
 * @author fc
 * 
 */
public class CameraAlbumActivity extends Activity implements OnClickListener,OnScrollListener{

	private static final String TAG = "CamAlbum";

	private GridView mainView;

	private Button testBtn;

	private Button popupBtn;

	private LruCache<String, Bitmap> memCache;

	private int bottomHeight;

	private PopupWindow mPopupWin;
	
	private AlbumModel dataManager=AlbumModel.getIns();
	
	private static final int INIT_PROGRAM = 0x000A;
	private static final int SET_ADAPTER = 0x000B;
	private static final int INIT_CACHE = 0x000C;
	private static final int FINISH_TASK = 0x000D;
	private static final int ERROR_OCCUR=0xFFFF;
	private static final int CREATE_STATUS=0x0001;
	private static final int RESUME_STATUS=0x0002;
	private static final int STOP_STATUS=0x0003;
	private static int running_state=CREATE_STATUS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"onCreate");
		running_state=CREATE_STATUS;
		setContentView(R.layout.cam_ablum);
		
		mainView = (GridView) findViewById(R.id.main_grid);
		mainView.setAdapter(null);

//		testBtn = (Button) findViewById(R.id.getPhoto);
//		testBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// getImagePath();
//				handler.sendEmptyMessage(GET_PHOTO_PATH);
//			}
//		});
		
		
		// View bottomLayout=findViewById(R.id.bottom_layout);
		// bottomHeight=bottomLayout.getHeight();

		//�ײ�������
		popupBtn = (Button) findViewById(R.id.catalog_select);
		popupBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// �ǵü��ж�(Ϊ������Ч)
				initPopWindow();
			}
		});
		handler.sendEmptyMessage(INIT_CACHE);		//��ʼ������
		handler.sendEmptyMessage(INIT_PROGRAM);		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG,"onResume");
		if(running_state==STOP_STATUS){
			resetWaitingList();			
		}else{
			running_state=RESUME_STATUS;
		}
		
//		Log.v(TAG,"getFirstVisiblePosition:"+String.valueOf(mainView.getFirstVisiblePosition()));
//		Log.v(TAG,"getLastVisiblePosition:"+String.valueOf(mainView.getLastVisiblePosition()));
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
		running_state=STOP_STATUS;
		cancelLoadingTask();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG,"onDestroy");
		memCache=null;
		mainView=null;
		adapter=null;
//		memCache.evictAll();
		dataManager.dataDestroy();
	}

	//��ɰ�ť
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.finish_select).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG,String.valueOf(item.getItemId()));
		Log.v(TAG,item.getTitle().toString());
		
		switch(item.getItemId()){
		case 0:
			Log.v(TAG,"selected photos");
			//���ؽ��
			Intent intent=new Intent();
			intent.putExtra("data", "");
			setResultEx(Activity.RESULT_OK, intent);
			finish();
			break;
		default:
			
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}


	/**
	 * �ļ���ѡ��
	 * ������
	 */
	private void initPopWindow() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View listWin = inflater.inflate(R.layout.album_pop_up_list, null);
		ListView list = (ListView) listWin.findViewById(R.id.catalog_list);
		BaseAdapter catalogAdapter = new CatalogItemAdapter(this,
				AlbumModel.dcimPathList, AlbumModel.dcimPicNumCounter);
		list.setAdapter(catalogAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mPopupWin.dismiss();
//				Log.v(TAG, "arg2=" + String.valueOf(arg2));
				// Log.v(TAG,"arg3="+String.valueOf(arg3));

				Log.v(TAG,
						"full name="
								+ AlbumModel.dcimPathFullNameList.get(arg2));
				// ��ѯ����ͼƬ������adapter
				String newPath = AlbumModel.dcimPathFullNameList.get(arg2);

				Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;		//�ⲿ�洢�豸
				List<String>tmlList=getCurrentPath(externalUri,newPath);
				Message msg = new Message();
				msg.obj = tmlList;
				msg.what = SET_ADAPTER;
				handler.sendMessage(msg);
			}
		});
		LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
		bottomHeight = bottomLayout.getHeight();

//		Log.v(TAG, String.valueOf(bottomHeight));
		mPopupWin = new PopupWindow(listWin, LayoutParams.MATCH_PARENT, 750,
				true);
		// mPopupWin.setFocusable(true);
		mPopupWin.setFocusable(true);
		mPopupWin.setOutsideTouchable(true);
		mPopupWin.setBackgroundDrawable(new BitmapDrawable());
		mPopupWin.showAtLocation(listWin, Gravity.BOTTOM, 0, bottomHeight);
	}


//	private static CamItemAdapter adapter;			//������
	private static MediaItemAdapter adapter;		//

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case INIT_PROGRAM:
				Log.v(TAG,"INIT_PROGRAM");
				getImagePath();
				break;
			case SET_ADAPTER:
				Log.v(TAG,"SET_ADAPTER");
				if (adapter == null) {
					//��ʼ��
//					adapter = new CamItemAdapter(CamAblum.this, handler,
//							(List<String>) msg.obj, memCache);
					adapter = new MediaItemAdapter(CameraAlbumActivity.this, handler, (List<String>)msg.obj, memCache);
					mainView.setAdapter(adapter);
					mainView.setOnScrollListener(CameraAlbumActivity.this);		//��������
				}else{
					//���ݸ���
					adapter.resetData((List<String>)msg.obj);
					adapter.notifyDataSetChanged();
				}
				break;
			case INIT_CACHE:
				memCache = initCache();
				break;
			case FINISH_TASK:
				if (adapter != null) {
					adapter.finishTask();
				}
				break;
			case CamItemAdapter.UPDATE_UI:
				String tag = msg.getData().getString("tag");
				if (mainView!=null&&(ImageView) mainView.findViewWithTag(tag) != null) {
					((ImageView) mainView.findViewWithTag(tag))
							.setImageBitmap((Bitmap) msg.obj);
				}
				break;			
			case ERROR_OCCUR:
				Toast.makeText(CameraAlbumActivity.this, "error code= *****", Toast.LENGTH_LONG).show();;
				break;
			default:
				break;
			}

		}
	};
	
	//��ȡ�����ļ�
	private void getImagePath() {
		Log.v(TAG,"getImagePath");
		//���洢�豸�Ƿ����
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			handler.sendEmptyMessage(ERROR_OCCUR);
			return;
		}

		// �ⲿ�洢
		Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		// �ڲ��洢
//		Uri internalUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
		
		List<String> tmlList = getPath(externalUri);
		Message msg = new Message();
		msg.obj = tmlList;
		msg.what = SET_ADAPTER;
		handler.sendMessage(msg);
	}



	// ��ȡ�����ļ���ͼƬ��Ϣ
	private List<String> getCurrentPath(Uri uri,String path) {
		List<String> currentPathList = new ArrayList<String>();
		ContentResolver mResolver = CameraAlbumActivity.this.getContentResolver();
		Cursor mCursor = mResolver.query(uri, null,
				"_data like ? and ("+
				MediaStore.Images.Media.MIME_TYPE + "=? or "
				+ MediaStore.Images.Media.MIME_TYPE + "=?)",
				new String[]{"%"+path+"%","image/jpeg","image/png"},
				MediaStore.Images.Media.DATE_MODIFIED + " desc"); 
		Log.v(TAG,"path count:"+String.valueOf(mCursor.getCount()));
		
		while(mCursor.moveToNext()){
			String tmlPath = mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			currentPathList.add(tmlPath);
		}
		mCursor.close();
		return currentPathList;
	}

	private void getThumbnails(){
		ContentResolver mResolver = CameraAlbumActivity.this.getContentResolver();
		
		
		Cursor cursor=mResolver.query(Thumbnails.EXTERNAL_CONTENT_URI, null, "", null, null);
				
		//�ؼ���
//		Thumbnails._ID;					//�����ID
//		Thumbnails.IMAGE_ID;			//������ͼƬID
		
	}
	
	// ��ʼ������ȡȫ����ͼƬ�����ַ��Ϣ
	private List<String> getPath(Uri uri) {

		List<String> pathList = new ArrayList<String>();

		// �ȴ��޸��滻��CursorLoader
		ContentResolver mResolver = CameraAlbumActivity.this.getContentResolver();

		Cursor mCursor = mResolver.query(uri, null,
				MediaStore.Images.Media.MIME_TYPE + "=? or "
						+ MediaStore.Images.Media.MIME_TYPE + "=?",
				new String[] { "image/jpeg", "image/png" },
				MediaStore.Images.Media.DATE_MODIFIED + " desc"); // ����
		

		while (mCursor.moveToNext()) {
			String path = mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			
			// Log.v(TAG, path);
			pathList.add(path);
			AlbumModel.currentPathFile.add(path);

			// д��һ���ļ����б�
			// ���ַ�ʽ�ȽϺ��ڴ氡�����ǵ�����취��Ҫ��new
			String parentName = new File(path).getParentFile().getName(); // ��ȡ�ļ�������
			String parentName2 = new File(path).getParent(); // ��ȡ�������ļ���·��

			int tmlCounter = 0;
			int position = -1;
			if ((position = AlbumModel.dcimPathFullNameList
					.indexOf(parentName2)) == -1) {
				// if(!AblumModel.dcimPathFullNameList.contains(parentName2)){
				AlbumModel.dcimPathList.add(parentName);
				AlbumModel.dcimPathFullNameList.add(parentName2);
				AlbumModel.dcimPicNumCounter.add(1);
			} else {
				tmlCounter = AlbumModel.dcimPicNumCounter.get(position) + 1;
				AlbumModel.dcimPicNumCounter.set(position, tmlCounter);
			}
		}
		mCursor.close();
		// CursorLoader loader=new CursorLoader(this);
		// CursorLoader loader=new CursorLoader(this,uri,)
		// loader.loadInBackground();
		return pathList;
	}

	// init LRUcache
	// LRUcache�Ĳ���Ӧ�÷ŵ�ͳһ���ļ�������ȥʵ��
	private LruCache<String, Bitmap> initCache() {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
//		Log.v(TAG, String.valueOf(maxMemory / (1024 * 1024)));
		int cacheSize = maxMemory / (8 * 1024);		
//		Log.v(TAG, String.valueOf(cacheSize));

		LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(
				cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount() / 1024;
			}

		};

		return bitmapCache;
	}


	@Override
	public void onClick(View v) {
	}


	//��������
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
//		Log.v(TAG,"onScroll");
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
//		Log.v(TAG,"onScrollstateChanged");
//		Log.v(TAG,"scrollState:"+String.valueOf(scrollState));
		switch(scrollState){
		case 0:				//��ȫֹͣ			
			resetWaitingList();
			break;
		case 1:				//��ʼ����
			
			break;
		case 2:				//�ſ�����
			//ֹͣ����
			cancelLoadingTask();			
			break;
		}
		
	}
	
	
	//ֹͣ����ͼƬ
	private void cancelLoadingTask(){
		if(adapter!=null){
			adapter.finishTask();
		}		
	}
	
	//���ݵ�ǰҳ����������ͼƬ����
	private void resetWaitingList(){
		List<Integer> list=new ArrayList<Integer>();
		for(int tmpMark=mainView.getFirstVisiblePosition();tmpMark<=mainView.getLastVisiblePosition();tmpMark++){
			list.add(tmpMark);
		}
		adapter.resetWaitingList(list);		
	}
	
	// ֧�ַ���
	/*
	private <K, V> LruCache<K, V> initCache2() {
		LruCache<K, V> cache = null;
		return cache;
	}
	*/
	
	//����
	private int mResultCode;
	private Intent mResultIntent;
	private void setResultEx(int resultCode,Intent data){
		mResultCode=resultCode;
		mResultIntent=data;
		setResult(resultCode,data);
	}

	public int getResultCode(){
		return mResultCode;
	}
		
	public Intent getResultData(){
		return mResultIntent;
	}
}
