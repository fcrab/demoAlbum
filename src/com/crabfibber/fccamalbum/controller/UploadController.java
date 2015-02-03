package com.crabfibber.fccamalbum.controller;

import java.io.File;

import android.content.Context;

/**
 * 上传。暂时不需要了
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
