package com.happy.pro.floating;

import static com.happy.pro.activity.MainActivity.bitversi;
import static com.happy.pro.activity.MainActivity.game;
import static com.happy.pro.activity.MainActivity.gameint;
import static com.happy.pro.activity.MainActivity.hiderecord;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.blankj.molihuan.utilcode.util.ToastUtils;
import com.happy.pro.R;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.FPrefs;
import com.happy.pro.Component.MainService;
import com.happy.pro.security.BearMemoryProtection;
import com.happy.pro.utils.Shell;
import com.happy.pro.automation.BearAutomationManager;

public class FloatRei extends Service implements BearAutomationManager.BearStatusListener {

	private boolean checkStatus;
	private boolean espEnabled = false;
	private View mainView;
	private LinearLayout miniFloatView,mainFloat;
	private WindowManager windowManager;
	private LayoutParams paramsView;
	
	// Automation system
	private BearAutomationManager automationManager;
	
	// UI status indicators
	private TextView statusIndicator;
	private TextView espStatusText;

    public FPrefs getPref() {
        return FPrefs.with(this);
    }

	static {
        try {
            System.loadLibrary("happy");
        } catch(UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Initialize automation system
		                automationManager = BearAutomationManager.getInstance(this);
                automationManager.initialize(null);
		
		ShowMainView();
	}

	private void ShowMainView() {
		mainView = LayoutInflater.from(this).inflate(R.layout.floataimbot, null);
		paramsView = getParaams();
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		windowManager.addView(mainView, paramsView);
		InitShowMainView();
	}

	private WindowManager.LayoutParams getParaams() {
		final WindowManager.LayoutParams params =
			new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
										   LayoutParams.WRAP_CONTENT, getLayoutType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		if (hiderecord == 1) {
            HideRecorder.setFakeRecorderWindowLayoutParams(params);
        }
		params.gravity = Gravity.CENTER | Gravity.CENTER;
		params.x = 0;
		params.y = 0;
		return params;
	}

	private static int getLayoutType() {
		int LAYOUT_FLAG;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_TOAST;
		} else {
			LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}
		return LAYOUT_FLAG;
	}

	private void InitShowMainView() {
		miniFloatView = mainView.findViewById(R.id.miniFloatMenu);
		mainFloat = mainView.findViewById(R.id.mainmenu);
		RelativeLayout layoutView = mainView.findViewById(R.id.layoutControlView);
		LinearLayout islandmenu = mainView.findViewById(R.id.islandmenu);
		final Switch myImageView = mainView.findViewById(R.id.imgprotc);
		final Switch imyImageView = mainView.findViewById(R.id.iimgprotc);
		final TextView textprotc = mainView.findViewById(R.id.textprotc);
		final Switch espToggle = mainView.findViewById(R.id.toggleESPOverlay);
		final TextView textESP = mainView.findViewById(R.id.textesptoggle);

		if (gameint == 5){
			islandmenu.setVisibility(View.VISIBLE);
		}
		
		// Store references to status indicators
		statusIndicator = mainView.findViewById(R.id.textprotc);
		espStatusText = textESP;
		
		// Add ESP toggle listener with automation system
		espToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				FLog.info("ESP Toggle changed: " + isChecked);
				if (isChecked) {
					textESP.setText("🔍 TESTING ESP");
					// Use automation manager for intelligent ESP startup
					                                        automationManager.performAutomatedESPStartup();
					espEnabled = true;
				} else {
					textESP.setText("ESP DISABLED");
					// Use automation manager to stop all ESP systems
					automationManager.stopESPSystems();
					espEnabled = false;
				}
			}
		});

		View layout_close_main_view = mainView.findViewById(R.id.closeprotcst);
		layout_close_main_view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View p1) {
				mainFloat.setVisibility(View.GONE);
				miniFloatView.setVisibility(View.VISIBLE);
			}
		});



		layoutView.setOnTouchListener(new View.OnTouchListener() {
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;
				final View collapsedView = miniFloatView;
				final View expandedView = mainFloat;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							initialX = paramsView.x;
							initialY = paramsView.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							return true;

						case MotionEvent.ACTION_UP:
							int Xdiff = (int) (event.getRawX() - initialTouchX);
							int Ydiff = (int) (event.getRawY() - initialTouchY);
							if (Xdiff < 5 && Ydiff < 5) {
								if (isViewCollapsed()) {
									collapsedView.setVisibility(View.GONE);
									expandedView.setVisibility(View.VISIBLE);

									myImageView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
										@Override
										public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
											if (isChecked){
												Exec("/TW "+game+" 005","ISLAD PROTECTION SUCCESS");

											}
										}
									});

									if(gameint == 5){
										imyImageView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
											@Override
											public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
												if (isChecked){
													if (bitversi == 64 ){
														Exec("/TW "+game+" 003","ISLAD BYPASS ENABLE");
														textprotc.setText("ISLAND 64BIT ENABLE");
													}else if (bitversi == 32){
														Exec("/TW "+game+" 003","ISLAD BYPASS ENABLE");
														textprotc.setText("ISLAND 64BIT ENABLE");
													}
												}else{
													if (bitversi == 64 ){
														Exec("/TW "+game+" 003","ISLAD BYPASS ENABLE");
														textprotc.setText("ISLAND 64BIT ENABLE");
													}else if (bitversi == 32){
														Exec("/TW "+game+" 004","ISLAD BYPASS DISABLE");
														textprotc.setText("ISLAND DISBLE");
													}
													Exec("/TW "+game+" 004","ISLAD BYPASS DISABLE");
													textprotc.setText("ISLAND DISBLE");
												}
											}
										});
									}


								}
								/*if (miniFloatView.getVisibility() == View.VISIBLE) {
									myImageView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
										@Override
										public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
											if (isChecked){
												Exec("/TW "+"com.pubg.imobile "+"003","ISLAD BYPASS ENABLE");
												textprotc.setText("ISLAND ENABLE");
											}else{
												Exec("/TW "+"com.pubg.imobile "+"004","ISLAD BYPASS DISABLE");
												textprotc.setText("ISLAND DISBLE");
											}
										}
									});
									*//*if (!checkStatus) {
										checkStatus = true;
										Exec("/TW "+USERKEY+" "+"com.pubg.imobile "+"003","ISLAD BYPASS ENABLE");
										textprotc.setText("ISLAND ENABLE");
									} else {
										checkStatus = false;
										Exec("/TW "+USERKEY+" "+"com.pubg.imobile "+"004","ISLAD BYPASS DISABLE");
										textprotc.setText("ISLAND DISBLE");
									}*//*
								}*/
							}
							return true;

						case MotionEvent.ACTION_MOVE:
							paramsView.x = initialX + (int) (event.getRawX() - initialTouchX);
							paramsView.y = initialY + (int) (event.getRawY() - initialTouchY);
							windowManager.updateViewLayout(mainView, paramsView);
							return true;
					}
					return false;
				}
			});
	}


	private boolean isViewCollapsed() {
		return mainView == null || miniFloatView.getVisibility() == View.VISIBLE;
	}

	public static void toastImage(int id, CharSequence msg) {
		ToastUtils _toast = ToastUtils.make();
		_toast.setBgColor(android.R.color.white);
		_toast.setLeftIcon(id);
		_toast.setTextColor(android.R.color.black);
		_toast.setNotUseSystemToast();
		_toast.show(msg);

	}
	public void Exec(String path, String toast) {
		try {
			ExecuteElf("su -c chmod 777 " + getFilesDir() + path);
			ExecuteElf("su -c " + getFilesDir() + path);
			ExecuteElf("chmod 777 " + getFilesDir() + path);
			ExecuteElf("" +  getFilesDir() + path);
			toastImage(R.drawable.ic_check, toast);
		} catch (Exception e) {
		}
	}

	private void ExecuteElf(String shell) {
		try {
			Runtime.getRuntime().exec(shell, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


	// BearAutomationManager.BearStatusListener implementation
	@Override
	public void onESPStatusChanged(BearAutomationManager.ESPSystemStatus status) {
		// Update UI on main thread
		runOnUiThread(() -> {
			if (espStatusText != null) {
				espStatusText.setText(status.icon + " " + status.description);
			}
			
			// Show toast for important status changes
			switch (status) {
				case STANDALONE_ACTIVE:
					toastImage(R.drawable.ic_check, "Standalone ESP Active");
					break;
				case SOCKET_BASED_WORKING:
					toastImage(R.drawable.ic_check, "Socket ESP Active");
					break;
				case ALL_SYSTEMS_FAILED:
					toastImage(R.drawable.ic_error, "ESP Systems Failed");
					break;
			}
		});
	}
	
	@Override
	public void onAutomationEvent(String event, String details) {
		FLog.info("🤖 Automation Event: " + event + " - " + details);
		
		// Update status indicator
		runOnUiThread(() -> {
			if (statusIndicator != null) {
				statusIndicator.setText(event);
			}
		});
	}
	
	@Override
	public void onSystemHealthUpdate(String healthReport) {
		FLog.info("💊 Health Update: " + healthReport);
	}
	
	/**
	 * Helper method to run code on UI thread
	 */
	private void runOnUiThread(Runnable action) {
		if (mainView != null) {
			mainView.post(action);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		checkStatus = false;
		
		// Stop automation system
		if (automationManager != null) {
			automationManager.stopESPSystems();
		}
		
		if (mainView != null)
			windowManager.removeView(mainView);
	}
}


