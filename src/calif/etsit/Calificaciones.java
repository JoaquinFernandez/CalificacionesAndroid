/*
 * Copyright (C) 2011  Joaquín Fernández Moreno.
 * 				All rights reserved.
 */
package calif.etsit;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Calificaciones extends Activity {

	/**
	 * 
	 */
	private Dialog dialog;

	public final static String PREFS_NAME = "calif.etsit.preferences";

	public final static String IMAGE_NAME = "/data/data/calif.etsit/files/loadingImage.txt";

	static ArrayList<String> data;

	private SharedPreferences settings;
	
	private SharedPreferences.Editor editor;

	private EditText editUser;

	private EditText editPass;

	private CheckBox saveBox;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.centered_title);
		TextView title = (TextView) findViewById(R.id.centered_title_text);
		title.setText("E.T.S.I de Telecomunicación");
		settings = getSharedPreferences(PREFS_NAME, 0);
		editor = settings.edit();
		/** 
		 * Le ponemos nombre a la ventana 
		 */
		editUser = (EditText) this.findViewById(R.id.main_userEdit);
		editPass = (EditText) this.findViewById(R.id.main_passEdit);
		saveBox = (CheckBox) this.findViewById(R.id.main_save_checkBox);

		Button button = (Button)this.findViewById(R.id.main_connectButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickear();
			}

		});
		String user = settings.getString("user", "");
		String pass = settings.getString("pass", "");
		boolean checkBox = settings.getBoolean("saveBox", false);
		editUser.setText(user);
		editPass.setText(pass);
		saveBox.setChecked(checkBox);
		if (user != "" && pass != "") {
			clickear();
		}
		if (!settings.getBoolean("seEjecuto", false)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
				}
			};
			builder.setMessage("Si quiere activar las actualizaciones automáticas vaya a preferencias en el menu");
			builder.setPositiveButton("OK", dialogClickListener);
			builder.show();
			editor.putBoolean("seEjecuto", true);
			editor.commit();
		}
	}

	private void clickear() {

		String user = editUser.getText().toString();
		String pass = editPass.getText().toString();
		user = user.trim();
		pass = pass.trim();
		if (user == null || pass == null) {
			Toast t = Toast.makeText(getApplicationContext(),
					"Introduzca sus datos", Toast.LENGTH_SHORT);

			t.show();
		}
		else if (saveBox.isChecked()) {
			// We need an Editor object to make preference changes.
			// All objects are from android.context.Context
			editor.putBoolean("saveBox", true);
			editor.putString("user", user);
			editor.putString("pass", pass);
			editor.commit();
		}
		loadData(user, pass);
	}

	private final Handler progressHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if (msg.obj != null) {
				data = (ArrayList<String>) msg.obj;
				setData(data);					
			}
			if (dialog.isShowing() == true)
				dialog.dismiss();
		}
	};

	/**
	 * @param pass 
	 * @param user 
	 */
	private void loadData(final String user, final String pass) {
		dialog = new Dialog(Calificaciones.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater =  (LayoutInflater)
		this.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.load_dialog, null);
		ImageView image = (ImageView) layout.getChildAt(0);
		Bitmap bMap = BitmapFactory.decodeFile(Calificaciones.IMAGE_NAME);
		if (bMap!= null && image!= null)
			image.setImageBitmap(bMap);
		else
			image.setImageResource(R.drawable.load_image);
		layout.removeViewAt(0);
		layout.addView(image, 0);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		dialog.setContentView(layout);
		dialog.show();
		new Thread(new Runnable(){
			@Override
			public void run() {
				NoteList noteList = new NoteList(user, pass); 
				Message msg = progressHandler.obtainMessage();
				msg.obj = noteList.getList();
				progressHandler.sendMessage(msg);
			}}).start();
	}
	private void setData(ArrayList<String> data){
		if (data.size() > 0 && data.size() <= 2) {
			//its data.get(1) because in 0 there's always the date
			Toast t = Toast.makeText(getApplicationContext(), data.get(1), Toast.LENGTH_LONG);
			t.show();	
		}
		else if (data.size() > 0) {
			Intent intent = new Intent(Calificaciones.this, CalifView.class);
			intent.putStringArrayListExtra("notas", data);
			startActivity(intent);
			finish();
		}
		else {
			Toast t = Toast.makeText(getApplicationContext(), "Hubo un problema con la conexión", Toast.LENGTH_LONG);
			t.show();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.MnuOpc1:
			PackageManager manager = getApplicationContext().getPackageManager();
			PackageInfo info = null;
			try {
				info = manager.getPackageInfo(getPackageName(), 0);
			} catch (NameNotFoundException e) {
			}
			String version;
			if (info == null) {
				version = null;
			}
			else
				version = info.versionName;
			Toast t2 = Toast.makeText(getApplicationContext(), "Version " + version + '\n' + '\n'
					+ "Joaquín Fernández Moreno ©" + '\n', Toast.LENGTH_LONG);
			t2.show();
			return true;
		case R.id.MnuOpc2:
			final Dialog dialog = new Dialog(Calificaciones.this);
			dialog.setContentView(R.layout.preferences_dialog);
			dialog.setTitle("Preferencias");
			final SharedPreferences settings = getSharedPreferences(Calificaciones.PREFS_NAME, 0);
			boolean update = settings.getBoolean("auto_update", false);
			int autoUpdate = settings.getInt("auto_update_timer", 0);
			final ToggleButton enable = (ToggleButton) dialog.findViewById(R.id.auto_update_selection);
			enable.setChecked(update);
			final EditText timerSeconds = (EditText) dialog.findViewById(R.id.auto_update_timer_edit);
			timerSeconds.setText(String.valueOf(autoUpdate));

			Button button = (Button) dialog.findViewById(R.id.preferences_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String timerSeconds2 = timerSeconds.getText().toString();
					Integer timer = Integer.valueOf(timerSeconds2);
					if (timer == null || (timer != null && timer < 1))
						timer = 20;
					if (enable.isChecked()) {
						editor.putBoolean("auto_update", true);
						editor.putInt("auto_update_timer", timer);
						Intent serviceIntent = new Intent();
						serviceIntent.setAction("calif.etsit.service.MyService");
						Calificaciones.this.startService(serviceIntent);
					}
					else {
						editor.putBoolean("auto_update", false);
						Intent serviceIntent = new Intent();
						serviceIntent.setAction("calif.etsit.service.MyService");
						Calificaciones.this.stopService(serviceIntent);
					}
					editor.commit();
					dialog.dismiss();
				}

			});
			dialog.show();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}