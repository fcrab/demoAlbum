package com.crabfibber.fccamalbum.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * @author fc
 *
 */
public class CustomGridItemView extends ImageView {

	public CustomGridItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public CustomGridItemView(Context context,AttributeSet attrs){
		super(context,attrs);
	}
	
	public CustomGridItemView(Context context,AttributeSet attrs,int defStyle){
		super(context,attrs,defStyle);
	}
	
	
	//设定每个view的长宽相等
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}

}
