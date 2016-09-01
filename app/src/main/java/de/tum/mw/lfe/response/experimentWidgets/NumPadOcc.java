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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.tum.mw.lfe.response.ExperimentActivity;
import de.tum.mw.lfe.response.R;


public class NumPadOcc extends ExperimentWidget implements View.OnClickListener{

	private static final String TAG = "ExperimentWidget.NumPad";
	private View mView;
    private int mEorSumMs; //eyes off road sum [ms}
    private long mLastKeyTimestamp;

	//constructor
	public NumPadOcc(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode){
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
        mEorSumMs = 0;
        mLastKeyTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
	}

	public String getResult() {
		if (mView == null){ return ""; }
		EditText numpadTextEdit = (EditText)mView.findViewById(R.id.numpadText);
		return numpadTextEdit.getText().toString();
	}

	public void onClick(View v) {

        Button b = (Button)v;
        String bText = b.getText().toString();
        int bID = v.getId();


        if(!mParent.occViewIsOpen()) {//input while closed / occluded
            mParent.writeToLoggingFile("occAssistantDebug", "inputWhileOccluded"+ mParent.CSV_DELIMITER + bText);//log
            return;//return and dismiss input
        }


        mParent.writeToLoggingFile("occAssistantDebug", "before"+ mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log


        //---------------------------------
        long now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        long diff = now - mLastKeyTimestamp;
        mParent.writeToLoggingFile("occAssistantDebug", "diff"+ mParent.CSV_DELIMITER + Long.toString(diff));//log
        if (diff < 300){//rule5
            diff = 300;
            mParent.writeToLoggingFile("occAssistantDebug", "rule5"+ mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log
        }
        if (diff > 1000){//rule 2; order important before rule1
            mEorSumMs = 0;
            mParent.writeToLoggingFile("occAssistantDebug", "rule2"+ mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log
        }else{
            mEorSumMs +=  diff;//rule3
            mParent.writeToLoggingFile("occAssistantDebug", "rule3" + mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log
            if ((mEorSumMs+diff) > 2000){//rule4
                mParent.triggerOneOcclusion(); //!!!!!!!!!!!!
                mEorSumMs = 0;
                mParent.writeToLoggingFile("occAssistantDebug", "rule4" + mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log
            }
        }
        if (mEorSumMs == 0){
            mEorSumMs += 700;//rule 1
            mParent.writeToLoggingFile("occAssistantDebug", "rule1" + mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log
        }


        mParent.writeToLoggingFile("occAssistantDebug", "after" + mParent.CSV_DELIMITER + Long.toString(mEorSumMs));//log


        mLastKeyTimestamp = now;
        //---------------------------------

		if (mView == null){ return; }
		
		mUserChangedSomething = true;


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
