package calif.etsit;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import calif.etsit.view.MyTextView;

public class MyArrayAdapter extends ArrayAdapter<String> {

	private List<String> data;
	Activity context;
	private List<String> dataInfo;

	public MyArrayAdapter(Activity context, int textViewResourceId, 
			List<String> data, List<String> dataInfo) {
		super(context, textViewResourceId, data);
		this.data = data;
		this.dataInfo = dataInfo;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		LinearLayout item = (LinearLayout) inflater.inflate(R.layout.lista_item, null);
		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout l = (LinearLayout) v;
				MyTextView info = (MyTextView) l.getChildAt(1);
				info.changeVisibility();
			}
		});

		TextView lblTitulo = (TextView) item.findViewById(R.id.each_note_title);
		String text = data.get(position);
		lblTitulo.setText(text);

		MyTextView lblSubtitulo = (MyTextView) item.findViewById(R.id.each_note_subtitle);
		text = dataInfo.get(position);
		lblSubtitulo.setText(text);
		lblSubtitulo.setVisibility();

		if (text.indexOf("provisional)") != -1 || text.indexOf("provisionales)") != -1) {
			if (text.indexOf("AP ") != -1 || text.indexOf("APROBADO") != -1 || 
					text.indexOf("NT ") != -1 || text.indexOf("NOTABLE") != -1 ||
					text.indexOf("SB ") != -1 || text.indexOf("SOBRESALIENTE") != -1 ||
					text.indexOf("MH ") != -1 || text.indexOf("MATRICULA") != -1)

				lblTitulo.setTextColor(0xff008419);
			else if (text.indexOf("SS ") != -1 || text.indexOf("SUSPENSO") != -1)

				lblTitulo.setTextColor(0xffac2b2b);
			else if (text.indexOf("NP ") != -1 || text.indexOf("NO PRESENTADO") != -1)

				lblTitulo.setTextColor(0xff6d6c6c);
		}
		else if (text.indexOf("definitivas)") != -1 || text.indexOf("definitiva)") != -1) {
			if (text.indexOf("AP ") != -1 || text.indexOf("APROBADO") != -1 || 
					text.indexOf("NT ") != -1 || text.indexOf("NOTABLE") != -1 ||
					text.indexOf("SB ") != -1 || text.indexOf("SOBRESALIENTE") != -1 ||
					text.indexOf("MH ") != -1 || text.indexOf("MATRICULA") != -1)

				lblTitulo.setTextColor(0xff00844b);
			else if (text.indexOf("SS ") != -1 || text.indexOf("SUSPENSO") != -1)

				lblTitulo.setTextColor(0xffac2b55);
			else if (text.indexOf("NP ") != -1 || text.indexOf("NO PRESENTADO") != -1)

				lblTitulo.setTextColor(0xff6d6c6c);
		}
		return(item);
	}
}