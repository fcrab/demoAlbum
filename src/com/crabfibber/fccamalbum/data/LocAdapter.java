package com.crabfibber.fccamalbum.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.crabfibber.fccamalbum.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * 多选&单选列表范例
 * @author fc
 *
 */
public class LocAdapter extends BaseAdapter {
	private static final String TAG="locListAdapter";
	
	private Context context;
	private List<String> list;
	public static final int CHECKBOX_VAL=0x0;
	public static final int RADIOBTN_VAL=0x1;
	private int select_style;			//0 for checkbox,1 for radiobutton
	private LayoutInflater inflater;
	private Map<Integer,Boolean> map;
	
	public LocAdapter(Context context,List<String> list,int select_style){
		this.context=context;
		this.list=list;
		this.select_style=select_style;
		inflater=LayoutInflater.from(context);
		initMap();
	}
	
	
	
	public int changeItemStyle(int val){
		
		initMap();			
		switch (val) {
		case CHECKBOX_VAL:
			select_style=val;
			break;
		case RADIOBTN_VAL:
			select_style=val;
			map.clear();
			break;
		default:
			break;
		}
		
		return select_style;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup arg2) {
		viewHolder holder=null;
		if(contentView==null){
			contentView=inflater.inflate(R.layout.loc_list_item, null);
			holder=new viewHolder();
			holder.itemText=(TextView)contentView.findViewById(R.id.loc_item_text);
			holder.itemCb=(CheckBox)contentView.findViewById(R.id.loc_item_cb);
			holder.itemRadio=(RadioButton)contentView.findViewById(R.id.loc_item_radio);
			contentView.setTag(holder);
		}else{
			holder=(viewHolder)contentView.getTag();
		}
		
		
		holder.itemText.setText(list.get(position));
		
		switch (select_style) {
		case CHECKBOX_VAL:
			holder.itemCb.setVisibility(View.VISIBLE);
			holder.itemRadio.setVisibility(View.GONE);
			break;
		case RADIOBTN_VAL:
			holder.itemCb.setVisibility(View.GONE);
			holder.itemRadio.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		final int p=position;
		boolean isChecked=map.containsKey(position);
		
		holder.itemCb.setChecked(isChecked?map.get(position):false);
		holder.itemCb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(TAG,"checkbox click");
				map.put(p, !map.get(p));
			}
		});
	/*	
		holder.itemCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.v(TAG, "checkBox on change");
				Toast.makeText(context, "checkBox:"+String.valueOf(p), Toast.LENGTH_LONG).show();
				map.put(p, isChecked);
			}
		});
*/
		holder.itemRadio.setChecked(isChecked);
		holder.itemRadio.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(TAG, "radioBtn on click");	
				Toast.makeText(context, "RadioBtn:"+String.valueOf(p), Toast.LENGTH_LONG).show();
				radioBtnSetSel(p);
				LocAdapter.this.notifyDataSetChanged();
			}
		});			
		
		return contentView;
	}

	class viewHolder{
		TextView itemText;
		CheckBox itemCb;
		RadioButton itemRadio;
	}

	private void initMap(){
		if(map==null){
			map=new HashMap<Integer,Boolean>();
		}
		map.clear();
		for(int counti=0;counti<list.size();counti++){
			map.put(counti, false);
		}
	}
	
	private void radioBtnSetSel(int position){
		map.clear();
		map.put(position, true);
	}
	
}
