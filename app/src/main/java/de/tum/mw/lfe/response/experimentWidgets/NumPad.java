package de.tum.mw.lfe.response.experimentWidgets;

/*
MIT License

        Copyright (c) 2015-2016 Michael Krause (krause@tum.de)

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
*/
import java.util.ArrayList;
import java.util.List;

import de.tum.mw.lfe.response.ExperimentActivity;
import de.tum.mw.lfe.response.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class NumPad extends ExperimentWidget implements View.OnClickListener{
	
	private static final String TAG = "ExperimentWidget.NumPad";
	private View mView;
	
	//constructor
	public NumPad(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode){
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}
	
	
	public void init(String param) {
		  mView = LayoutInflater.from(mParent).inflate(R.layout.numpad,null, false);
		  mWidgetLayout.addView(mView);
		  
		  LinearLayout layout = (LinearLayout)mView.findViewById(R.id.NumpadLayout);
		  
		  List<View> allChildren = getAllChildren(layout);
		  
		  for(int i=0; i <allChildren.size(); i++)
		  {
		      View v = allChildren.get(i);
		      if (v instanceof Button) {
		          Button b = (Button) v;
		          b.setOnClickListener(this);
		      }
		  }
		  
		

	}
	
	//helper
	private List<View> getAllChildren(View v) {
	    List<View> visited = new ArrayList<View>();
	    List<View> unvisited = new ArrayList<View>();
	    unvisited.add(v);

	    while (!unvisited.isEmpty()) {
	        View child = unvisited.remove(0);
	        visited.add(child);
	        if (!(child instanceof ViewGroup)) continue;
	        ViewGroup group = (ViewGroup) child;
	        final int childCount = group.getChildCount();
	        for (int i=0; i<childCount; i++) unvisited.add(group.getChildAt(i));
	    }

	    return visited;
	}

	@Override
	public void reset() {
		super.reset();
		if (mView == null){ return; }
		EditText numpadTextEdit = (EditText)mView.findViewById(R.id.numpadText);
		numpadTextEdit.getText().clear();
		//numpadTextEdit.setWidth(700);
	}

	public String getResult() {
		if (mView == null){ return ""; }
		EditText numpadTextEdit = (EditText)mView.findViewById(R.id.numpadText);
		return numpadTextEdit.getText().toString();
	}

	public void onClick(View v) {
		
		if (mView == null){ return; }
		
		mUserChangedSomething = true;
		
		
		 Button b = (Button)v;
		 String bText = b.getText().toString();
		 int bID = v.getId();
		 
		 EditText numpadTextEdit = (EditText)mView.findViewById(R.id.numpadText);
	
		 if (bID == R.id.numpadDel){
			 String temp = numpadTextEdit.getText().toString();
			 if (temp.length() > 0) temp = temp.substring ( 0, temp.length() - 1 );
			 numpadTextEdit.setText(temp);
		 }else{
			 numpadTextEdit.getText().append(bText);
		 }
		 
	    mParent.traceUserInput(bText + mParent.CSV_DELIMITER + getResult());
		
	}

}
