package com.crabfibber.fccamalbum.utils;

import java.io.File;

import android.content.Context;

/**
 * 上传模块
 * @author fc
 *
 */
public class UploadController {
	private Context context;	
	private UploadController ins;
	
	private UploadController(){
		
	}
	
	public UploadController getIns(Context context){
		this.context=context;
		if(ins==null){
			ins=new UploadController();
		}
		
		return ins;
	}
	
	public void upload(File file){
	
	}
	
	public void upload(String filePath){
		
	}
	
}
