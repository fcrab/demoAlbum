package com.crabfibber.fccamalbum.model;

import java.util.ArrayList;
import java.util.List;

public final class AlbumModel {
	public static List<Integer> currentPathId;			//ͼƬ
	public static List<String> currentPathFile;		//ȫ��ͼƬ�б�
	public static List<String> dcimPathList;			//�ļ����б�
	public static List<String> dcimPathFullNameList;	//����Ŀ¼��	
	public static List<Integer> dcimPicNumCounter;		//ÿ���ļ�����ͼƬ������
	
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

	
	// ��ʼ�����ݽṹ
	private void dataInit(){
		if (dcimPathFullNameList == null) {
			dcimPathFullNameList = new ArrayList<String>();
			dcimPathFullNameList.add("");
		}
		if (dcimPathList == null) {
			dcimPathList = new ArrayList<String>();
			dcimPathList.add("����ͼƬ");
		}
		if (currentPathFile == null) { // ȫ��ͼƬ
			currentPathFile = new ArrayList<String>();
		}
		if (dcimPicNumCounter == null) {
			dcimPicNumCounter = new ArrayList<Integer>();
			dcimPicNumCounter.add(0);
		}
	}

	
}
