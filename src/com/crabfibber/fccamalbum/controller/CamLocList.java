package com.crabfibber.fccamalbum.controller;

import java.util.ArrayList;
import java.util.List;

import com.crabfibber.fccamalbum.R;
import com.crabfibber.fccamalbum.data.LocAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

/**
 * 单项&多项选择页面
 * @author fc
 * example
 */

public class CamLocList extends Activity implements OnClickListener,
		Handler.Callback {
	private static final String TAG = "CamLocList";

	private ListView locList;
	private LocAdapter adapter;
	private Button listBtn;
	private Switch listSwitch;

	private String[] testStr = new String[] { "ab", "cb", "db", "eb", "fb",
			"ab", "cb", "db", "eb", "fb", "ab", "cb", "db", "eb", "fb", "ab",
			"cb", "db", "eb", "fb", "ab", "cb", "db", "eb", "fb", "ab", "cb",
			"db", "eb", "fb", "ab", "cb", "db", "eb", "fb", "ab", "cb", "db",
			"eb", "fb", "ab", "cb", "db", "eb", "fb", "ab", "cb", "db", "eb",
			"fb", "ab", "cb", "db", "eb", "fb", "ab", "cb", "db", "eb", "fb",
			"ab", "cb", "db", "eb", "fb", "ab", "cb", "db", "eb", "fb" };
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cam_loc_list_layout);

		// initial component
		locList = (ListView) findViewById(R.id.loc_listview);
		listSwitch = (Switch) findViewById(R.id.loc_switch);
		listBtn = (Button) findViewById(R.id.get_loc_list_btn);

		List<String> list = new ArrayList<String>();
		for (String aStr : testStr) {
			list.add(aStr);
		}
		adapter = new LocAdapter(this, list, 0);
		locList.setAdapter(adapter);
		// initial actions
		listBtn.setOnClickListener(this);
		listSwitch.setOnCheckedChangeListener(switchListener);
		handler = new Handler(this);
	}

	@Override
	public void onClick(View v) {
		Log.v(TAG, "on btn Click");
	}

	private final static int CHANGE_ITEM_TYPE = 0x000011;

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case CHANGE_ITEM_TYPE:

			break;

		default:
			break;

		}
		return false;
	}

	private OnCheckedChangeListener switchListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				Toast.makeText(getApplicationContext(), "switch is checked",
						Toast.LENGTH_LONG).show();
				adapter.changeItemStyle(adapter.RADIOBTN_VAL);
				adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(getApplicationContext(),
						"switch is not checked", Toast.LENGTH_LONG).show();
				adapter.changeItemStyle(adapter.CHECKBOX_VAL);
				adapter.notifyDataSetChanged();
			}

		}
	};

}
