/*
 * Copyright (C) 2011  Joaquín Fernández Moreno.
 * 				All rights reserved.
 */
package calif.etsit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MyTextView extends TextView {
	
	private int visibility = GONE;

	public MyTextView(Context context) {
		super(context);
	}

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void changeVisibility() {
		if (visibility == VISIBLE)
			visibility = GONE;
		else
			visibility = VISIBLE;
		setVisibility(visibility);
	}

	public void setVisibility() {
		setVisibility(visibility);
	}
}
