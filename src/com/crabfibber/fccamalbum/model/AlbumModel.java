package com.crabfibber.fccamalbum.model;

import java.util.ArrayList;
import java.util.List;

public final class AlbumModel {
	public static List<Integer> currentPathId;			//图片
	public static List<String> currentPathFile;		//全部图片列表
	public static List<String> dcimPathList;			//文件夹列表
	public static List<String> dcimPathFullNameList;	//完整目录名	
	public static List<Integer> dcimPicNumCounter;		//每个文件夹内图片的数量
	
	public static AlbumModel instance;
	
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
	}

	
	// 初始化数据结构
	private void dataInit(){
		if (dcimPathFullNameList == null) {
			dcimPathFullNameList = new ArrayList<String>();
			dcimPathFullNameList.add("");
		}
		if (dcimPathList == null) {
			dcimPathList = new ArrayList<String>();
			dcimPathList.add("所有图片");
		}
		if (currentPathFile == null) { // 全部图片
			currentPathFile = new ArrayList<String>();
		}
		if (dcimPicNumCounter == null) {
			dcimPicNumCounter = new ArrayList<Integer>();
			dcimPicNumCounter.add(0);
		}
	}

	
}
