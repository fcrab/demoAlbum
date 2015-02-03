package com.crabfibber.fccamalbum.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

//�����Ʋ�����imageView

public class CustomPreviewImageView extends ImageView {

	private static final String TAG="customPreviewImageView";
	
	//����״̬��¼
	private enum MODE{
		NONE,DRAG,ZOOM
	}
	
	private boolean isMonitorV=false;		//��ֱ���
	
	private boolean isMonitorH=false;		//ˮƽ���
	
	private boolean isScaleAnim=false;	//�Ƿ�������
	
	private ScaleAnimation scaleAnimation;	//���Ŷ���
	
	public MODE mode=MODE.NONE;
	
	private int screenW,screenH;		//��Ļ�ɼ����
	
	private int imageW,imageH;			//ͼƬ��ǰ���
		
	//λ�ü�¼
	private int MAX_W,MAX_H,MIN_W,MIN_H;		//��߼���ֵ
	
	private int curTop,curRight,curBottom,curLeft;		//��ǰͼƬ�߿�ֵ
	
	private int defaultTop=-1,defaultRight=-1,defaultBottom=-1,defaultLeft=-1;		//Ĭ�ϱ߿�ֵ
	
	private Point defaultPoint,currentPoint;		//����λ��
	
	private float defaultDistance,newDistance;		//���������
	
	private float tml_scale;		//��ǰ���ű���
	
	
	
	
	public CustomPreviewImageView(Context context) {
		super(context);
		//initial variable
		defaultPoint=new Point();
		currentPoint=new Point();
	}
	
	public CustomPreviewImageView(Context context,AttributeSet attrs){
		super(context,attrs);
		//initial variable
		defaultPoint=new Point();
		currentPoint=new Point();		
	}

	/*
	 * ��ʼ����Ļ�ɼ������С����
	 */
	public void setScreenSize(int width,int height){
		screenH=height;
		screenW=width;
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		
		//ͼƬĬ�ϴ�С
		imageW=bm.getWidth();
		imageH=bm.getHeight();
		
		//�趨����ֵ(3����С,1/2��Сֵ)
		MAX_H=imageH*3;
		MAX_W=imageW*3;
		
		MIN_H=imageH/2;
		MIN_W=imageW/2;
	}
	
	

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//��ʼ������λ��
		if(defaultTop==-1){
			defaultTop=top;
			defaultLeft=left;
			defaultRight=right;
			defaultBottom=bottom;
		}
	}

	/**
	 * �ж������¼�
	 * ����:
	 * ����ָ������ACTION_DOWN---ACTION_MOVE----ACTION_UP
	 * ����ָ������ACTION_DOWN---ACTION_POINTER_DOWN---ACTION_MOVE--ACTION_POINTER_UP---ACTION_UP.
	 * 
	 */
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch(event.getAction()&MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			onTouchDown(event);		//�����µĶ���(��һ������)
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			//�ж��Ƿ����ֲ������Ŷ���
			onZoomTouch(event);
			break;
		case MotionEvent.ACTION_MOVE:
			onTouchMove(event);		//�϶�
			break;
		case MotionEvent.ACTION_UP:
			//��������
			mode=MODE.NONE;
			break;			
		case MotionEvent.ACTION_POINTER_UP:
			mode=MODE.NONE;
			//ִ�����Ŷ���
			if(isScaleAnim){
				//TODO	����Ч��
				actScaleAnim();
			}
			break;
		}
		
		return true;
	}

	/**
	 * һЩ����
	 */
	
	//����
	private void onTouchDown(MotionEvent event){
		mode=MODE.DRAG;		//��קģʽ��ʼ
		
		//��ʼ������
		currentPoint.x=(int) event.getRawX();
		currentPoint.y=(int) event.getRawY();
		
		defaultPoint.x=(int) event.getX();
		defaultPoint.y=currentPoint.y-getTop();
		
		Log.v(TAG,"curPoint:"+currentPoint.toString());
		Log.v(TAG,"defaultPoint:"+defaultPoint.toString());
	}

	//���Ŷ���(����ָ)
	private void onZoomTouch(MotionEvent event){
		if(event.getPointerCount()==2){
			mode=MODE.ZOOM;
			//��ȡ�������
			defaultDistance=getDistance(event);
		}
	}

		
	//�����϶���Ӧ
	private void onTouchMove(MotionEvent event){
		Log.v(TAG, "moving mode="+mode);
		int tmpLeft=0,tmpRight=0,tmpTop=0,tmpBottom=0;
		if(mode==MODE.DRAG){		//�϶��¼�
			
			//test
			//only zoom
			
			//��ֹdragԽ��
			Log.v(TAG,"Draging");
			//��ȡ�϶�����λ��
			tmpLeft=currentPoint.x-defaultPoint.x;
			tmpRight=currentPoint.x+getWidth()-defaultPoint.x;
			tmpTop=currentPoint.y-defaultPoint.y;
			tmpBottom=currentPoint.y+getHeight()-defaultPoint.y;
			
			//ˮƽ�ж�
			if(isMonitorH){
				if(tmpLeft>=0){
					tmpLeft=0;
					tmpRight=getWidth();
				}
				if(tmpRight<=screenW){
					tmpLeft=screenW-getWidth();
					tmpRight=screenW;
				}
			}else{
				tmpLeft=getLeft();
				tmpRight=getRight();
			}
			
			//��ֱ�ж�
			if(isMonitorV){
				if(tmpTop>=0){
					tmpTop=0;
					tmpBottom=getHeight();
				}
				if(tmpBottom<=screenH){
					tmpTop=screenH-getHeight();
					tmpBottom=screenH;
				}
			}else{
				tmpTop=getTop();
				tmpBottom=getBottom();
			}
			
			if(isMonitorH || isMonitorV){
				//����λ���ػ�
				layout(tmpLeft, tmpTop, tmpRight, tmpBottom);
			}
			
			currentPoint.x=(int) event.getRawX();
			currentPoint.y=(int) event.getRawY();
		
		}else if(mode==MODE.ZOOM){		//��������
			newDistance=getDistance(event);
			
			float gapDistance=newDistance-defaultDistance;		//�仯�ľ���
			
			if(Math.abs(gapDistance)>5f){		//����5�Ϳ�ʼ�������ű���
				tml_scale=newDistance/defaultDistance;
				
				setScale(tml_scale);
				
				defaultDistance=newDistance;
			}
			
		}
	}

	
	//�����������
	private float getDistance(MotionEvent event){
		float x=event.getX(0)-event.getX(1);
		float y=event.getY(0)-event.getY(1);
		return FloatMath.sqrt(x*x+y*y);
	}
	
	//���Ŵ���
	void setScale(float scale){
		int disX=(int)(getWidth()*Math.abs(1-scale))/4;			//����ˮƽ����
		int disY=(int)(getHeight()*Math.abs(1-scale))/4;			//���Ŵ�ֱ����
		

		if(scale>1&&getWidth()<=MAX_W){
			//zoom in �Ŵ�
			//�����µĴ�С
			curLeft=getLeft()-disX;
			curTop=getTop()-disY;
			curRight=getRight()+disX;
			curBottom=getBottom()+disY;
			
			setFrame(curLeft, curTop, curRight, curBottom);
			
			//TODO
			//�Գƣ�ֻ��һ���ж�
			if(curTop<=0 && curBottom >=screenH ){
				//�Ѿ�������Ļ��Χ
				isMonitorV=true;		//��ֱ���
			}else{
				isMonitorV=false;
			}
			if(curLeft<=0 && curRight>= screenW){
				//�Ѿ�������Ļ��Χ
				isMonitorH=true;		//ˮƽ���
			}else{
				isMonitorH=false;
			}
		}else if(scale<1 && getWidth()>=MIN_W){
			//zoom out ��С
			curLeft=getLeft()+disX;
			curTop=getTop()+disY;
			curRight=getRight()-disX;
			curBottom=getBottom()-disY;
			
			//�Ϸ�Խ��
			if(isMonitorV && curTop>0){
				curTop=0;
				curBottom=getBottom()-2*disY;		//�������ٶ�����
				if(curBottom<screenH){
					curBottom=screenH;		//����Ϊ��Ҫǿ����������
					isMonitorV=false;		//�Ѿ��ص���Ļ��
				}
			}
			
			//�·�Խ��
			if(isMonitorV && curBottom<screenH){
				curBottom=screenH;
				curTop=getTop()+2*disY;
				if(curTop>0){
					curTop=0;
					isMonitorV=false;		
				}
			}
			
			//���Խ��
			if(isMonitorH && curLeft>=0){
				curLeft=0;
				curRight=getRight()-2*disX;
				if(curRight<=screenW){
					curRight=screenW;
					isMonitorH=false;
				}
			}
			
			
			//�ұ�Խ��
			if(isMonitorH && curRight<=screenW){
				curRight=screenW;
				curLeft=getLeft()+2*disX;
				if(curLeft>=0){
					curLeft=0;
					isMonitorH=false;
				}
			}
			
			//
			this.setFrame(curLeft, curTop, curRight, curBottom);
			if(isMonitorH || isMonitorV){
				//���ڷ�Χ��
			}else{
				isScaleAnim=true;		//��������
			}
			
		}
		
		
		
	}
	
	/**
	 * ������������
	 */
	private void actScaleAnim(){
		
	}
	
	class AnimAsyncTask extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}
		
	}
	
}
