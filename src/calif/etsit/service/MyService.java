package calif.etsit.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import calif.etsit.Calificaciones;
import calif.etsit.NoteList;
import calif.etsit.R;

public class MyService extends Service {

	private Timer timer = new Timer();

	@Override
	public void onCreate() {
		super.onCreate();

	}
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		SharedPreferences settings = getSharedPreferences(Calificaciones.PREFS_NAME, 0);
		long period = settings.getInt("auto_update_timer", 1200) * 60000;
		if (settings.getBoolean("auto_update", false))
			timer.scheduleAtFixedRate( new MyTimerTask(), 0, period);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void notificate(String subject, String info) {

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText = "¡Han salido nuevas notas!";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = subject;
		CharSequence contentText = info;
		Intent notificationIntent = new Intent(this, Calificaciones.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

		final int HELLO_ID = R.id.MnuOpc2;

		mNotificationManager.notify(HELLO_ID, notification);
	}
	private class MyTimerTask extends TimerTask {

		private ArrayList<String> data;
		private SharedPreferences settings;
		private SharedPreferences.Editor editor;
		private String user;
		private String pass;
		private String imgVersion;
		private String version;

		private void initialize() throws IOException {
			settings = getSharedPreferences(Calificaciones.PREFS_NAME, 0);
			editor = settings.edit();
			user = settings.getString("user", "");
			pass = settings.getString("pass", "");
			imgVersion = settings.getString("imgVersion", "1.0");

			if (user != "" && pass != "") {
				PackageManager manager = getApplicationContext().getPackageManager();
				PackageInfo info = null;
				try {
					info = manager.getPackageInfo(getPackageName(), 0);
				} catch (NameNotFoundException e) {
				}
				if (info == null) {
					version = null;
				}
				else
					version = info.versionName;
			}
		}

		@SuppressWarnings("unchecked")
		public void run() {
			try {
				initialize();
				if (!settings.getBoolean("auto_update", true))
					stopSelf();
				NoteList note = new NoteList(user, pass);
				if (version != null && imgVersion != null) {
					String newVersion = note.updateProgram(version);
					if (newVersion != "") {
						editor.putString("newVersionAvailable", newVersion);
						editor.commit();
					}
					String newImgVersion = note.updateImg(imgVersion);
					if (newImgVersion != "") {
						editor.putString("imgVersion", newImgVersion);
						editor.commit();
					}
				}
				ArrayList<String> newData = note.getList();
				if (data == null || data.size() < 3)
					data = (ArrayList<String>) newData.clone();
				if (newData != null) {
					int size = newData.size();
					int x = 0;
					if (size > 2) {
						String subject = "";
						String info = "";
						for (int i = 1; i < size; i++) {
							if (newData.get(i).compareTo(data.get(i)) != 0) {
								subject = newData.get(i - 1);
								info = newData.get(i);
								info = searchNote(info);
								x++;
							}
						}
						if (subject != "" && info != "" && x < 4) {
							data = (ArrayList<String>) newData.clone();
							notificate(subject, info);						
						}
					}
				}
			} catch (IOException e) {
			}
		}

		private String searchNote(String info) {
			int start = info.indexOf("nota:");
			int end = info.indexOf("percentil:");
			String note = "";
			if (start != -1 && end != - 1)
				note = info.substring(start, end);
			else
				note = info;
			return note;
		}
	}
}
