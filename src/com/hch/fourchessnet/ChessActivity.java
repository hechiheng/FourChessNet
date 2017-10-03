package com.hch.fourchessnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hch.fourchessnet.ChessPanel.OnChessPanelListener;
import com.hch.fourchessnet.UDPClient.OnUDPClientListener;

public class ChessActivity extends Activity {
	private String TAG = "ChessActivityLog";
	private TextView showText;
	private ChessPanel chessPanel;
	private Button exitBtn;
	private UDPClient client;
	private String localhost;
	private String ip;
	private String chesstype;
	private String sendMsg;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			int what = message.what;
			String msg = message.getData().getString("msg");
			if (what == 1) {
				showText.setTextColor(Color.parseColor("#ff0000"));
				showText.setText(msg);
				Toast.makeText(ChessActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			} else if (what == 2) {
				chessPanel.moveChess(msg);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chess);

		showText = (TextView) findViewById(R.id.showText);
		showText.setVisibility(View.INVISIBLE);
		exitBtn = (Button) findViewById(R.id.exitBtn);

		Intent intent = getIntent();
		ip = intent.getExtras().getString("ip");
		localhost = intent.getExtras().getString("localhost");
		chesstype = intent.getExtras().getString("chesstype");
		setTitle("正在与" + ip + "对战");

		chessPanel = (ChessPanel) findViewById(R.id.chessPanel);
		if (chesstype.equals("white")) {
			chessPanel.setHandChessType(1);
			chessPanel.setChooseChessType(1);
		} else if (chesstype.equals("black")) {
			chessPanel.setHandChessType(2);
			chessPanel.setChooseChessType(2);
			chessPanel.setRotation(180);
		}
		chessPanel.invalidate();
		chessPanel.setOnChessPanelListener(new OnChessPanelListener() {
			@Override
			public void onHandTypeChange(int handChessType) {
				if (handChessType == 0) {
					showText.setVisibility(View.INVISIBLE);
				} else if (handChessType == 1) {
					showText.setVisibility(View.VISIBLE);
					showText.setTextColor(Color.parseColor("#ffffff"));
					if (handChessType == chessPanel.getChooseChessType()) {
						showText.setText("我方执棋");
					} else {
						showText.setText("对方执棋");
					}
				} else if (handChessType == 2) {
					showText.setVisibility(View.VISIBLE);
					showText.setTextColor(Color.parseColor("#000000"));
					if (handChessType == chessPanel.getChooseChessType()) {
						showText.setText("我方执棋");
					} else {
						showText.setText("对方执棋");
					}
				}
			}

			public void onMoveChess(int chessType, int chess_i, int point_i,
					int point_j) {
				sendMsg = chessType + "," + chess_i + "," + point_i + ","
						+ point_j;
				new Thread() {
					public void run() {
						client.sendMsg(UDPClient.MSG_MOVE, ip, sendMsg);
					}
				}.start();
			}

			@Override
			public void onGameOver(int handChessType) {
				if (handChessType == 1) {
					showText.setVisibility(View.VISIBLE);
					showText.setTextColor(Color.parseColor("#ffffff"));
					showText.setText("恭喜白棋赢了！");
					showMessage("游戏结束", "恭喜白棋赢了！");
				} else {
					showText.setVisibility(View.VISIBLE);
					showText.setTextColor(Color.parseColor("#000000"));
					showText.setText("恭喜黑棋赢了！");
					showMessage("游戏结束", "恭喜黑棋赢了！");
				}
			}

		});

		client = UDPClient.getInstance();
		client.setOnUDPClientListener(new OnUDPClientListener() {
			@Override
			public void onReceiveMsg_ONLINE(String ip) {
			}

			@Override
			public void onReceiveMsg_OFFLINE(String ip) {
			}

			@Override
			public void onReceiveMsg(String ip, String msg) {
				Message message = new Message();
				message.what = 1;
				message.getData().putString("msg", msg);
				handler.sendMessage(message);
			}

			@Override
			public void onSelf_OFFLINE() {
				Toast.makeText(ChessActivity.this, ip + "下线了",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onReceiveMsg_CHOOSE(String ip, String msg) {
			}

			@Override
			public void onReceiveMsg_CONNECT(String ip) {
			}

			@Override
			public void onReceiveMsg_MOVE(String ip, String msg) {
				Message message = new Message();
				message.what = 2;
				message.getData().putString("msg", msg);
				handler.sendMessage(message);
			}
		});

		exitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new Builder(ChessActivity.this);
				builder.setMessage("是否退出对战？");
				builder.setTitle("提示");
				builder.setPositiveButton("重新开始",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								finish();
								restartGame();
							}
						});
				builder.setNegativeButton("退出对战",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								finish();
								quitGame();
							}
						});
				builder.create().show();
			}
		});

	}

	/**
	 * 显示信息框
	 * 
	 * @param title
	 * @param message
	 */
	private void showMessage(String title, String message) {
		AlertDialog.Builder builder = new Builder(ChessActivity.this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton("重新开始",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						chessPanel.restart();
					}
				});
		builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 返回按键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new Builder(ChessActivity.this);
			builder.setMessage("是否退出对战？");
			builder.setTitle("提示");
			builder.setPositiveButton("重新开始",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
							restartGame();
						}
					});
			builder.setNegativeButton("退出对战",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
							quitGame();
						}
					});
			builder.create().show();
		}
		return true;
	}

	/**
	 * 重新开始
	 */
	public void restartGame() {
		sendMsg = ip + "重新开始";
		new Thread() {
			public void run() {
				client.sendMsg(ip, sendMsg);
			}
		}.start();

		Intent intent = new Intent(ChessActivity.this, ChooseActivity.class);
		intent.putExtra("ip", ip);
		intent.putExtra("localhost", localhost);
		startActivity(intent);
	}

	/**
	 * 退出对战
	 */
	public void quitGame() {
		sendMsg = ip + "退出对战";
		new Thread() {
			public void run() {
				client.sendMsg(ip, sendMsg);
			}
		}.start();

		Intent intent = new Intent(ChessActivity.this, SearchActivity.class);
		startActivity(intent);
	}
}
