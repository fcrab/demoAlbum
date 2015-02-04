package com.crabfibber.fccamalbum.data;

import java.util.List;

import com.crabfibber.fccamalbum.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 *目录Adapter
 * @author fc
 *
 */
public class CatalogItemAdapter extends BaseAdapter {

	private List<String> subList;
	private List<Integer> numList;
	private Context context;
	private LayoutInflater inflater;
	
	public CatalogItemAdapter(Context context,List<String> subList,List<Integer> numList) {
		// TODO Auto-generated constructor stub
		this.subList=subList;
		this.numList=numList;
		this.context=context;
		this.inflater=LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return subList.size();
	}

	@Override
	public Object getItem(int position) {
		return subList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder=null;
		if(convertView==null){
			convertView=inflater.inflate(R.layout.catalog_item_layout, parent,false);
			holder=new Holder();
			holder.nameView=(TextView)convertView.findViewById(R.id.catalog_name);
			holder.numView=(TextView)convertView.findViewById(R.id.num_count);
			convertView.setTag(holder);
		}else{
			holder=(Holder)convertView.getTag();
		}
		holder.nameView.setText(subList.get(position));
//		holder.numView.setText(numList.get(position)!=null?numList.get(position):0);
		holder.numView.setText(String.valueOf(numList.get(position)));
		return convertView;
	}

	private static class Holder{
		public TextView nameView;
		public TextView numView;
	}
	
}
