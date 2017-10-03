package com.hch.fourchessnet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.hch.fourchessnet.UDPClient.OnUDPClientListener;

public class SearchActivity extends Activity {
	private String TAG = "SearchActivityLog";
	private ListView listView;
	private Button button;
	private List<String> ipList = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	// private SimpleAdapter simpleAdapter;
	// private List<Map<String, Object>> listItems = new ArrayList<Map<String,
	// Object>>();

	private UDPClient client;
	private boolean isOnline;
	private String localhost;
	private String ip;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			adapter.notifyDataSetChanged();
			// simpleAdapter.notifyDataSetChanged();
		}

	};

	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		// 获取wifi服务
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		localhost = intToIp(ipAddress);
		Log.d(TAG, "localhost:" + localhost);

		button = (Button) findViewById(R.id.button);
		listView = (ListView) findViewById(R.id.listView);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, ipList);
		listView.setAdapter(adapter);

		// Map<String,Object> listItem=new HashMap<String, Object>();
		// listItem.put("ip", "192.168.0.11");
		// listItem.put("status", "等待连接");
		// Map<String,Object> listItem1=new HashMap<String, Object>();
		// listItem1.put("ip", "192.168.0.12");
		// listItem1.put("status", "等待连接");
		// listItems.add(listItem);
		// listItems.add(listItem1);
		// simpleAdapter = new SimpleAdapter(this, listItems,
		// R.layout.listitem_search, new String[] { "ip", "status" },
		// new int[] { R.id.ipText_item, R.id.statusText_item });
		// listView.setAdapter(simpleAdapter);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				client = UDPClient.getInstance();
				client.setOnUDPClientListener(new OnUDPClientListener() {
					@Override
					public void onReceiveMsg_ONLINE(String ip) {
						ipList.add(ip);
						// Map<String, Object> listItem = new HashMap<String,
						// Object>();
						// listItem.put("ip", "192.168.0.11");
						// listItem.put("status", "在线");
						// listItems.add(listItem);
						Message message = new Message();
						handler.sendMessage(message);
					}

					@Override
					public void onReceiveMsg_OFFLINE(String ip) {
						ipList.remove(ip);
						Message message = new Message();
						handler.sendMessage(message);
					}

					@Override
					public void onReceiveMsg(String ip, String msg) {
					}

					public void onSelf_OFFLINE() {
						ipList.clear();
						Message message = new Message();
						handler.sendMessage(message);
					}

					@Override
					public void onReceiveMsg_CHOOSE(String ip, String msg) {
					}

					@Override
					public void onReceiveMsg_CONNECT(String ip) {
						Intent intent = new Intent(SearchActivity.this,
								ChooseActivity.class);
						intent.putExtra("ip", ip);
						intent.putExtra("localhost", localhost);
						startActivity(intent);
					}

					@Override
					public void onReceiveMsg_MOVE(String ip, String msg) {
					}
				});
				if (!isOnline) {
					isOnline = true;
					client.createSocket(localhost);
					client.setUdpLife(true);
					button.setText("离开");
					Thread t = new Thread(client);
					t.start();
					new Thread() {
						public void run() {
							client.sendMsg_ONLINE("255.255.255.255");
						}
					}.start();

				} else {
					isOnline = false;
					button.setText("搜索");
					new Thread() {
						public void run() {
							client.sendMsg_OFFLINE("255.255.255.255");
							client.setUdpLife(false);
						}
					}.start();
				}

			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ip = ipList.get(position);
				new Thread() {
					public void run() {
						client.sendMsg(UDPClient.MSG_CONNECT, ip, "");
					}
				}.start();
				Intent intent = new Intent(SearchActivity.this,
						ChooseActivity.class);
				intent.putExtra("ip", ip);
				intent.putExtra("localhost", localhost);
				startActivity(intent);
			}

		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "------onDestroy----");
		if (isOnline) {
			new Thread() {
				public void run() {
					client.sendMsg_OFFLINE("255.255.255.255");
				}
			}.start();
		}
		System.exit(0);
	}

}
