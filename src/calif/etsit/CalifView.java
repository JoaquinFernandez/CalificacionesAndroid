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
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CalifView extends Activity {

	private Dialog dialog;

	private SharedPreferences settings;

	private SharedPreferences.Editor editor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.list_view);
		final ArrayList<String> data = getIntent().getStringArrayListExtra("notas");
		settings = getSharedPreferences(Calificaciones.PREFS_NAME, 0);
		editor = settings.edit();
		createWindow(data);
	}

	private void createWindow(ArrayList<String> data) {
		String date = data.remove(0);
		TextView textDate = (TextView) findViewById(R.id.notes_date);
		textDate.setText(date);
		String myTitle = data.remove(0);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.centered_title);
		TextView title = (TextView) findViewById(R.id.centered_title_text);
		title.setText(myTitle);
		int i = 1;
		ArrayList<String> dataInfo = new ArrayList<String>();
		while (i < data.size()) {
			String info = data.remove(i);
			dataInfo.add(info);
			i++;
		}
		ListView lv = (ListView) this.findViewById(R.id.notes_listview);
		lv.setAdapter(new MyArrayAdapter(this, R.layout.lista_item, data, dataInfo));
		lv.setTextFilterEnabled(true);
	}
	
	private void createWindowAux(ArrayList<String> data){
		if (data.size() > 0 && data.size() < 2) {
			Toast t = Toast.makeText(getApplicationContext(), "Hubo un problema con la conexión", Toast.LENGTH_LONG);
			t.show();;	
		}
		else if (data.size() > 0) {
			createWindow(data);
		}
	}


	private final Handler progressHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			ArrayList<String> data = (ArrayList<String>) msg.obj;
			if (data != null) {
				createWindowAux(data);
			}
			if (dialog.isShowing())
				dialog.dismiss();
		}
	};

	/**
	 * @param pass 
	 * @param user 
	 */
	private void loadData(final String user, final String pass) {
		dialog = new Dialog(CalifView.this);
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

	@Override
	protected void onResume() {
		super.onResume();
		String newVersion = settings.getString("newVersionAvailable", "");
		if (newVersion != "") {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case DialogInterface.BUTTON_POSITIVE: 
						Intent myIntent = new Intent(Intent.ACTION_VIEW, 
								Uri.parse("http://mini.webfactional.com/calificaciones/app/calificacionesAndroid.apk")); 
						dialog.dismiss();
						startActivity(myIntent);
						Toast t = Toast.makeText(getApplicationContext(),
								"Tienes que ir a descargas del navegador e instalarlo desde alli", Toast.LENGTH_LONG);

						t.show();			

						break;

					case DialogInterface.BUTTON_NEGATIVE:
						dialog.dismiss();
						break;
					}
				}
			};
			builder.setMessage("¡Actualización " + newVersion + " Disponible! \n" + "¿Desea descargarla?");
			builder.setPositiveButton("Yes", dialogClickListener);
			builder.setNegativeButton("No", dialogClickListener);
			builder.show();
			editor.putString("newVersionAvailable", "");
			editor.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu2, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.MnuOpc2:
			String user = settings.getString("user", "");
			String pass = settings.getString("pass", "");
			if (user != "" && pass != "") {
				loadData(user, pass);
				return true;
			}
			Toast t = Toast.makeText(getApplicationContext(),
					"No hay un perfil guardado", Toast.LENGTH_SHORT);

			t.show();			
		case R.id.MnuOpc1:
			editor.putBoolean("saveBox", false);
			editor.putString("user", "");
			editor.putString("pass", "");
			editor.commit();
			Intent intent = new Intent(CalifView.this, Calificaciones.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.MnuOpc3:
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
		case R.id.MnuOpc4:
			final Dialog dialog = new Dialog(CalifView.this);
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
					SharedPreferences.Editor editor = settings.edit();
					String timerSeconds2 = timerSeconds.getText().toString();
					Integer timer = Integer.valueOf(timerSeconds2);
					if (timer == null || (timer != null && timer < 1))
						timer = 20;
					if (enable.isChecked()) {
						editor.putBoolean("auto_update", true);
						editor.putInt("auto_update_timer", timer);
						Intent serviceIntent = new Intent();
						serviceIntent.setAction("calif.etsit.service.MyService");
						CalifView.this.startService(serviceIntent);
					}
					else {
						editor.putBoolean("auto_update", false);
						editor.putInt("auto_update_timer", 0);
						Intent serviceIntent = new Intent();
						serviceIntent.setAction("calif.etsit.service.MyService");
						CalifView.this.stopService(serviceIntent);
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


