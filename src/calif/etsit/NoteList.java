/*
 * Copyright (C) 2011  Joaquín Fernández Moreno.
 * 				All rights reserved.
 */
package calif.etsit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class NoteList {

	private ArrayList<String> list = new ArrayList<String>();
	private String user;
	private String pass;

	public NoteList(String user,String pass) {
		this.user = user;
		this.pass = pass;
		try {
			list = conectar();
			list.trimToSize();
		} catch (IOException e) {
		}
	}

	private ArrayList<String> conectar() throws IOException {
		ArrayList<String> pagina = new ArrayList<String>();
		URL url = new URL("http://www-app.etsit.upm.es/notas/");
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setConnectTimeout(7000);
		urlConnection.setReadTimeout(7000);
		Authenticator.setDefault(new Authenticator() {
			private int i = 0;
			protected PasswordAuthentication getPasswordAuthentication() {
				PasswordAuthentication auth = null;
				while (i < 10) {
					auth = new PasswordAuthentication(user, pass.toCharArray());
					i++;
				}
				return auth;
			}});
		int response = urlConnection.getResponseCode();
		if (response != HttpURLConnection.HTTP_OK) {
			pagina.add(urlConnection.getResponseMessage());
			return pagina;
		}
		try {
			InputStream input = new BufferedInputStream(urlConnection.getInputStream());
			BufferedReader r = new BufferedReader(new InputStreamReader(input, "iso-8859-1"));
			StringBuilder data = new StringBuilder();
			String line = "";
			while ((line = r.readLine()) != null) {
				data.append(line);
			}
			input.close();
			r.close();
			String codigo = null;
			codigo = new String(data);
			pagina = procesar(codigo);
		}
		finally {
			urlConnection.disconnect();
		}
		return pagina;
	}

	/**
	 * Procesar. It process the information to leave it into the form I need
	 * 
	 * @param codigo
	 *            the codigo
	 * @return the pagina stack with the grades info
	 */
	private static ArrayList<String> procesar(String codigo) {

		ArrayList<String> notas = new ArrayList<String>();


		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
		Date date = new Date();
		String fecha = (sdf.format(date)).toString();
		fecha = "Última actualización \n" + fecha;
		notas.add(fecha);

		// Header, name of the user
		int principio = codigo.indexOf("12puntos");
		if (principio == -1) {
			notas.add(codigo);
			return notas;
		}
		codigo = codigo.substring(principio + 11);

		// End of the user name
		int indexTitle = codigo.indexOf("</div>");
		String title = codigo.substring(0, indexTitle);
		title = title.trim();

		// Cleans the white space
		int j = title.indexOf("  ");
		while (j != -1) {
			title = title.substring(0, j) + title.substring(j + 1);
			j = title.indexOf("  ");
		}
		notas.add(title);

		// Start of grades
		int index25 = codigo.indexOf("Estilo25");
		String contador = codigo.substring(index25);

		// I add to the stack, the title and info of a subject in order
		int iniAsignaturas = contador.indexOf("<b>");
		while (iniAsignaturas != -1) {
			int endAsignaturas = contador.indexOf("</b>");
			int iniInfo = contador.indexOf("<br>");
			int endInfo = contador.indexOf("<br><br>");
			String titulo = contador.substring(iniAsignaturas + 3,
					endAsignaturas);
			titulo = limpiar(titulo);
			notas.add(titulo);
			String info = contador.substring(iniInfo + 4, endInfo);
			info = limpiar(info);
			info = espaciar(info);
			notas.add(info);
			contador = contador.substring(endInfo + 8);
			iniAsignaturas = contador.indexOf("<b>");
		}
		return notas;
	}

	private static String espaciar(String text) {
		int index = text.indexOf(')') + 1;
		if (index != 0) {
			String cadena1 = text.substring(0, index);
			String cadena2 = text.substring(index);
			text = cadena1 + '\n' + '\n' + cadena2;
		}
		return text;
	}

	/**
	 * Limpiar. It searchs the text for unwanted characters from html format and
	 * removes them
	 * 
	 * @param texto
	 *            string
	 * @return the "clean" string
	 */
	private static String limpiar(String texto) {
		int k = texto.indexOf("\r");
		while (k != -1) {
			String cadena1 = texto.substring(0, k);
			String cadena2 = texto.substring(k + 2);
			texto = cadena1 + cadena2;
			k = texto.indexOf("\r");

		}
		int y = texto.indexOf("<br>");
		while (y != -1) {
			String cadena1 = texto.substring(0, y);
			String cadena2 = texto.substring(y + 4);
			texto = cadena1 + cadena2;
			y = texto.indexOf("<br>");
		}
		int j = texto.indexOf("&nbsp;");
		while (j != -1) {
			String cadena1 = texto.substring(0, j);
			String cadena2 = texto.substring(j + 6);
			texto = cadena1 + cadena2;
			j = texto.indexOf("&nbsp;");
		}
		return texto;
	}

	public ArrayList<String> getList() {
		//Aqui guardo el nuevo list
		return list;
	}
	public String updateProgram(String version) {
		URL url;
		try {
			url = new URL("http://mini.webfactional.com/calificaciones/app/ETSIT_ANDROID.ver");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream input = new BufferedInputStream(urlConnection.getInputStream());
			BufferedReader r = new BufferedReader(new InputStreamReader(input, "iso-8859-1"));
			StringBuilder data = new StringBuilder();
			String line = "";
			if ((line = r.readLine()) != null) {
				data.append(line);
			}
			String newVersion = "";
			newVersion = new String(data);
			input.close();
			r.close();
			if (version.compareTo(newVersion) < 0 && newVersion.length() < 5) {
				return newVersion;
			}
		} catch (IOException e) {
		}
		return "";
	}

	public String updateImg(String imgVersion) {
		URL url;
		try {
			url = new URL("http://mini.webfactional.com/calificaciones/app/ETSIT_ANDROID.image");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream input = new BufferedInputStream(urlConnection.getInputStream());
			BufferedReader r = new BufferedReader(new InputStreamReader(input, "iso-8859-1"));
			StringBuilder data = new StringBuilder();
			String line = "";
			if ((line = r.readLine()) != null) {
				data.append(line);
			}
			String newImgVersion = "";
			newImgVersion = new String(data);
			input.close();
			r.close();
			if (imgVersion.compareTo(newImgVersion) != 0 && newImgVersion.length() < 5) {
				storeImage();
				return newImgVersion;
			}
		} catch (IOException e) {
		}
		return "";
	}

	private boolean storeImage() throws IOException {
		Bitmap x = null;

		HttpURLConnection connection;
		connection = (HttpURLConnection)new URL("http://mini.webfactional.com/calificaciones/app/image_android.png").openConnection();
		connection.setRequestProperty("User-agent","Mozilla/4.0");

		connection.connect();
		InputStream input = connection.getInputStream();

		x = BitmapFactory.decodeStream(input);
		if (x == null)
			return false;
		String path = Calificaciones.IMAGE_NAME;
		File imagefile = new File(path);
		FileOutputStream fos;
		try {
			if (imagefile.exists()) {
				fos = new FileOutputStream(imagefile);
				x.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			}
			else {
				File imagedir = new File(path.substring(0, path.indexOf("loadingImage.txt")));
				imagedir.mkdirs();
				imagefile.createNewFile();
			}
		} catch (FileNotFoundException e) {
			return false;
		}
		return true;
	}
}
