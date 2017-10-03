package com.hch.fourchessnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.hch.fourchessnet.UDPClient.OnUDPClientListener;

public class ChooseActivity extends Activity {
	private Button whiteBtn, blackBtn;
	private TextView chooseText;
	private UDPClient client;
	private String localhost;
	private String ip;
	private String sendMsg;
	private boolean isReady = false;
	private boolean isOtherReady = false;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			String ip = message.getData().getString("ip");
			String msg = message.getData().getString("msg");
			String chesstype = "black";
			isOtherReady = true;
			if ("white".equals(msg)) {
				whiteBtn.setText("对方已准备");
				whiteBtn.setEnabled(false);
				chooseText.setText("对方已准备，执白棋");
				chesstype = "black";
			} else if ("black".equals(msg)) {
				blackBtn.setText("对方已准备");
				blackBtn.setEnabled(false);
				chooseText.setText("对方已准备，执黑棋");
				chesstype = "white";
			}
			if (isReady && isOtherReady) {
				Intent intent = new Intent(ChooseActivity.this,
						ChessActivity.class);
				intent.putExtra("ip", ip);
				intent.putExtra("localhost", localhost);
				intent.putExtra("chesstype", chesstype);
				startActivity(intent);
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);

		Intent intent = getIntent();
		ip = intent.getExtras().getString("ip");
		localhost = intent.getExtras().getString("localhost");
		setTitle("已与" + ip + "连接，请选择棋子");

		whiteBtn = (Button) findViewById(R.id.whiteBtn);
		blackBtn = (Button) findViewById(R.id.blackBtn);
		chooseText = (TextView) findViewById(R.id.chooseText);

		client = UDPClient.getInstance();
		client.setOnUDPClientListener(new OnUDPClientListener() {
			@Override
			public void onReceiveMsg_ONLINE(String ip) {
			}

			@Override
			public void onReceiveMsg_OFFLINE(String ip) {
			}

			@Override
			public void onSelf_OFFLINE() {
			}

			@Override
			public void onReceiveMsg(String ip, String msg) {
			}

			@Override
			public void onReceiveMsg_CHOOSE(String ip, String msg) {
				Message message = new Message();
				message.getData().putString("ip", ip);
				message.getData().putString("msg", msg);
				handler.sendMessage(message);
			}

			@Override
			public void onReceiveMsg_CONNECT(String ip) {
			}

			@Override
			public void onReceiveMsg_MOVE(String ip, String msg) {
			}

		});

		whiteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMsg = "white";
				isReady = true;
				whiteBtn.setText("我方已准备");
				blackBtn.setText("等待对方准备");
				chooseText.setText("我方已准备，执白棋");
				whiteBtn.setEnabled(false);
				blackBtn.setEnabled(false);
				new Thread() {
					public void run() {
						client.sendMsg(UDPClient.MSG_CHOOSE, ip, sendMsg);
					}
				}.start();
				if (isReady && isOtherReady) {
					Intent intent = new Intent(ChooseActivity.this,
							ChessActivity.class);
					intent.putExtra("ip", ip);
					intent.putExtra("localhost", localhost);
					intent.putExtra("chesstype", sendMsg);
					startActivity(intent);
				}
			}
		});

		blackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMsg = "black";
				isReady = true;
				whiteBtn.setText("等待对方准备");
				blackBtn.setText("我方已准备");
				whiteBtn.setEnabled(false);
				blackBtn.setEnabled(false);
				chooseText.setText("我方已准备，执黑棋");
				new Thread() {
					public void run() {
						client.sendMsg(UDPClient.MSG_CHOOSE, ip, sendMsg);
					}
				}.start();
				if (isReady && isOtherReady) {
					Intent intent = new Intent(ChooseActivity.this,
							ChessActivity.class);
					intent.putExtra("ip", ip);
					intent.putExtra("localhost", localhost);
					intent.putExtra("chesstype", sendMsg);
					startActivity(intent);
				}
			}
		});

	}
}
