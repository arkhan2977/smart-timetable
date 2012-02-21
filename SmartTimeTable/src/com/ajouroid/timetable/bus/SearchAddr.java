package com.ajouroid.timetable.bus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.ajouroid.timetable.R;
import com.ajouroid.timetable.R.id;
import com.ajouroid.timetable.R.layout;
import com.ajouroid.timetable.R.string;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchAddr extends Activity {
	static final int PROGRESS_DIALOG = 0;
	Button button;
	ListView addrlist;
	ArrayList<YahooAddress> arr_list;
	AddrAdapter adapter;
	EditText input;
	List<Address> addr;
	List<Address> result2;	
	String err_msg = null;	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.search_address);		
		addrlist = (ListView) findViewById(R.id.addr_list);			
		button = (Button) findViewById(R.id.btn_confirm);
		input = (EditText) findViewById(R.id.search_addr);
		
		arr_list = new ArrayList<YahooAddress>();	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		adapter = new AddrAdapter();		
		addrlist.setAdapter(adapter);

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				arr_list.clear();
				MyAsyncTask task = new MyAsyncTask();
				task.execute(input.getText().toString());
			}
		});
		
		addrlist.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = getIntent();
				YahooAddress addr = arr_list.get(position);

				double lat = addr.getLatitude();
				double lon = addr.getLongitude();
				
				String strlat = Double.toString(lat);
				String strlon = Double.toString(lon);			
								
				i.putExtra("LAT", strlat);
				i.putExtra("LON", strlon);
				setResult(RESULT_OK,i);
				finish();				
			}
		});

	}

	class MyAsyncTask extends AsyncTask <String, YahooAddress, Void> {
		private ProgressDialog dialog;
		

		@Override
		protected void onPreExecute() {
			// 작업을 시작하기 전 할일

			dialog = new ProgressDialog(SearchAddr.this);
			dialog.setTitle(getResources().getString(R.string.searching));
			dialog.setMessage(getResources().getString(R.string.waitforcomplete));
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
			super.onPreExecute();
		}

		protected Void doInBackground(String... params) {

			YahooAddress finder = new YahooAddress();
			ArrayList<YahooAddress> result;
			try {
				result = finder.findAddress(params[0]);
				if(result.size() > 0){
					for (int i=0; i<result.size(); i++){
						publishProgress(result.get(i));								
					}  
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(YahooAddress... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);		
			
			arr_list.add(values[0]);
			adapter.notifyDataSetChanged();
			
			//adapter.notifyDataSetChanged();
		}  
		
		@Override
		protected void onPostExecute(Void result) {
			// 작업이 완료 된 후 할일            
			adapter.notifyDataSetChanged();
			dialog.dismiss();
			super.onPostExecute(result);
		}
		
	}
	private class AddrAdapter extends ArrayAdapter<YahooAddress> {

		AddrAdapter(){
			super(SearchAddr.this,R.layout.row2,R.id.row_addr, arr_list);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();

			View row = inflater.inflate(R.layout.row2, parent, false);


			TextView tv = (TextView) row.findViewById(R.id.row_addr);
			tv.setText(arr_list.get(position).getAddress());
			tv.setTextColor(0x99000000);

			return row;
		}
	}
}

