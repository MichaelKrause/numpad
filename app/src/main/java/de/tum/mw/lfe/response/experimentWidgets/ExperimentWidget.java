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
import de.tum.mw.lfe.response.ExperimentActivity;

import android.view.View;
import android.widget.RelativeLayout;


public abstract class ExperimentWidget {
	
	private static final String TAG = "ExperimentWidget";
	protected ExperimentActivity mParent;
	protected RelativeLayout mLayout;//layout from parent where mWidgetLayout should be added
	protected RelativeLayout mWidgetLayout;
	protected String mName;
	protected boolean mNeedsOkButton = true;
	protected String[] mDesiredResults;
	protected int mDesiredResultsCursor=0;
	protected byte mDikablisNumber;
	protected boolean mUserChangedSomething= false;
	
	
	protected long mDelay;
	protected byte mDelayMode;
	public static final byte DELAY_NONE = 0;
	public static final byte DELAY_DETERMINED = 1;
	public static final byte DELAY_INDETERMINED = 2;
	public static final byte DELAY_NOINDICATION = 3;
	
	ExperimentWidget (byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode){

		mDikablisNumber = dikablisNumber;
		mParent = parent;
		mLayout = layout;
		mName = name;
		mDesiredResults = desiredResults;
		mWidgetLayout = new RelativeLayout(parent);
		mLayout.addView(mWidgetLayout);
		mDelay = delay;
		mDelayMode = delayMode;
		
		init();
		dismiss();
	}
	
	
	abstract public void init(String param);
	abstract public String getResult();
	
	public void reset(){
		mUserChangedSomething = false;
	}
	
	public boolean userChangedSomething(){
		return mUserChangedSomething;
	}
	
	public void init(){//stub to call init(string) with default parameter => null
		init(mDesiredResults[mDesiredResultsCursor]);
	}
	
	
	public byte getDikablisNumber(){
		return mDikablisNumber;
	}
	
	public int getDesiredResultsCursor(){
		return mDesiredResultsCursor;
	}
	
	public void setDesiredResultsCursor(int cursor){
		mDesiredResultsCursor = cursor;
	}
	
	public void incDesiredResultsCursor(){
		mDesiredResultsCursor++;
	}
	
	public long getDelay(){
		return mDelay;
	}
	
	public byte getDelayMode(){
		return mDelayMode;
	}
		
	
	public boolean getNeedsOkButton(){
		return mNeedsOkButton;
	}
	
	public void setNeedsOkButton(boolean needsButton) {
		mNeedsOkButton = needsButton;
	};
	
	public String[] getDesiredResults() {
		return mDesiredResults;
	}	

	public String getName() {
		return mName;
	}
	
	public void show() {
		mWidgetLayout.setVisibility(View.VISIBLE);		
	}


	public void dismiss() {
		mWidgetLayout.setVisibility(View.GONE);
		reset();
		mDesiredResultsCursor = 0;
	}
		
	//DDS dispatcher
	public void dispatcher(String event){
		if(event.equals("#tl")) turnLeft();
		if(event.equals("#tr")) turnRight();
		
		if(event.equals("#PP")) okPress();
		if(event.equals("#HP")) okHold();
		if(event.equals("#RP")) okRelease();
		
		if(event.equals("#PU")) upPress();
		if(event.equals("#HU")) upHold();
		if(event.equals("#RU")) upRelease();
		
		if(event.equals("#PD")) downPress();
		if(event.equals("#HD")) downHold();
		if(event.equals("#RD")) downRelease();
		
		if(event.equals("#PL")) leftPress();
		if(event.equals("#HL")) leftHold();
		if(event.equals("#RL")) leftRelease();
		
		if(event.equals("#PR")) rightPress();
		if(event.equals("#HR")) rightHold();
		if(event.equals("#RR")) rightRelease();
		
	}
	
	//rotary
	public void turnLeft(){};  
	public void turnRight(){};
    
    //4way switch / joystick
	public void okPress(){};
	public void okHold(){};
	public void okRelease(){};
    
	public void upPress(){};
	public void upHold(){};
	public void upRelease(){};
    
	public void downPress(){};
	public void downHold(){};
	public void downRelease(){};
    
	public void leftPress(){};
	public void leftHold(){};
	public void leftRelease(){};
    
	public void rightPress(){};
	public void rightHold(){};
	public void rightRelease(){}
    
}
