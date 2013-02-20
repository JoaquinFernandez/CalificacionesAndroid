/*
 * Copyright (C) 2011  Joaquín Fernández Moreno.
 * 				All rights reserved.
 */
package calif.etsit.service;

import calif.etsit.Calificaciones;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MyStartupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = context.getSharedPreferences(Calificaciones.PREFS_NAME, 0);
		boolean autoUpdate = settings.getBoolean("auto_update", false);
		if (autoUpdate && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent startServiceIntent = new Intent(context, MyService.class);
	        context.startService(startServiceIntent);
		}
	}
}