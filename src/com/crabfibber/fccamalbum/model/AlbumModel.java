package com.crabfibber.fccamalbum.model;

import java.util.ArrayList;
import java.util.List;

public final class AlbumModel {
	public static List<String> currentPathFile;		//全部图片列表
	public static List<String> dcimPathList;			//文件夹列表
	public static List<String> dcimPathFullNameList;	  //完整目录名 
//	public static List<String> dcimTmlPathList;		//除了全部图片外的临时图片列表
	public static List<Integer> dcimPicNumCounter;		//每个文件夹内图片的数量
	public static List<String> resultPath;             //选择的图片
	
	public static AlbumModel instance;
	
	public static int photoNumber;             //可选图片数量
	public static int counterTag;              //解析图片计数
	
	//test
	public static int threadCounter=0;
	
	private AlbumModel(){
		dataInit();
	}
	
	public static AlbumModel getIns(){
		if(instance==null){
			instance=new AlbumModel();
		}
		return instance;
	}
	
	public void dataDestroy(){
		dcimPathFullNameList.clear();
		dcimPathList.clear();
		currentPathFile.clear();
		dcimPicNumCounter.clear();
		resultPath.clear();
		photoNumber=0;
	}
	
    // 初始化数据结构
	public void dataInit(){
		if (dcimPathFullNameList == null) {
			dcimPathFullNameList = new ArrayList<String>();
		}
		if(dcimPathFullNameList.size()==0){
		    dcimPathFullNameList.add("");
		}
		if (dcimPathList == null) {
			dcimPathList = new ArrayList<String>();
		}
		if(dcimPathList.size()==0){
		    dcimPathList.add("所有图片");
		}
		if (currentPathFile == null) { // 全部图片
			currentPathFile = new ArrayList<String>();
		}
		if (dcimPicNumCounter == null) {          
			dcimPicNumCounter = new ArrayList<Integer>();
		}
		if(dcimPicNumCounter.size()==0){
		    dcimPicNumCounter.add(0);                //全部图片的计数器初始化为0
		}
		if(resultPath == null){
		    resultPath=new ArrayList<String>();
		}
		
//		photoNumber=0;
		photoNumber=1000;		//for test
	}

}
