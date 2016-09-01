package de.tum.mw.lfe.response;
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
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.tum.mw.lfe.response.experimentWidgets.ExperimentWidget;
import de.tum.mw.lfe.response.experimentWidgets.NumPad;
import de.tum.mw.lfe.response.experimentWidgets.NumPadOcc;


public class ExperimentActivity extends Activity{
	private static final String TAG = "ExperimentActivity";

    private static final String PREFERENCES = "responsePreferences";

    private ExperimentWidget mTask;

    protected int mThisIsActivity = 0;
    public static final int ACTIVITY_1 = 1;
    public static final int ACTIVITY_2 = 2;
    public static final int ACTIVITY_3 = 3;

    private IntentFilter mFilter;
   
	private PowerManager.WakeLock mWakeLock = null;	
	private ExperimentActivity mContext = this;
	private Handler mHandler = new Handler();
    private ProgressDialog mProgress;
    private boolean mDelayInProgress = false;


    //occlusion
    private View mOccView;
    private boolean mOccViewStatus = OCC_VIEW_CLOSED;
    public static final boolean OCC_VIEW_OPEN = true;
    public static final boolean OCC_VIEW_CLOSED = false;
    public static final int OCC_COLOR = Color.LTGRAY;
    public static final int OCC_VIEW_OPEN_MS = 1500;
    public static final int OCC_VIEW_CLOSE_MS = 1500;

    public boolean occViewIsOpen(){ return mOccViewStatus; }


    //experimentWidgets
	//public ArrayList<ExperimentWidget> mExpWidgets = new ArrayList<ExperimentWidget>();
	public ExperimentWidget[] mTasks;// = new ExperimentWidget[13];


    private Button mOkButton;

	//LOGGING related	
	private File mLogFile=null;//logging file
	public static final String CSV_DELIMITER = ";"; //delimiter within csv
	public static final String CSV_LINE_END = "\r\n"; //line end in csv
	public static final String FOLDER = "RESPONSE"; //folder
	public static final String FOLDER_DATE_STR = "yyyy-MM-dd";//logging folder format
	public String mFile_ext = ".txt";
	public static final String HEADER ="timestamp;reason;data";    



    private static int mTelNumber = 0;
    public static final String[] TEL_NUMBERS = {
            "0170 197 536",
            "0171 843 752",
            "0151 614 279",
            "0157 503 218",
            "0160 104 926",
            "0161 372 854",
            "0165 520 196",
            "0167 618 753"
    };


    
    // Experiment Arrays:
    public RelativeLayout mExperimentWidgetLayout;


    // randomize the Array of ExpWidgets
    static void shuffleArray(ExperimentWidget[] ar)
	  {
	    Random rnd = new Random();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      ExperimentWidget a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }



    protected void prepareOccView() {
        if (mOccView == null) {
            mOccView = new LinearLayout(this);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.FILL_PARENT,
                    WindowManager.LayoutParams.FILL_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.addView(mOccView, params);
            mOccView.setBackgroundColor(OCC_COLOR - 0xff000000);//remove opacity => transparent
            mOccView.bringToFront();
            mOccViewStatus = OCC_VIEW_OPEN;
        }
    }



    protected void removeOccView() {
        //remove callbacks
        mOccView.removeCallbacks(screenOpen);
        mOccView.removeCallbacks(screenCloseDelayedOpen);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.removeView(mOccView);
    }

    private void openShutter() {
        if (mOccView != null) {
            mOccView.setVisibility(View.INVISIBLE);
            mOccView.setBackgroundColor(OCC_COLOR & 0xff000000);//remove opacity => transparent
            mOccViewStatus = OCC_VIEW_OPEN;
            writeToLoggingFile("occView", "open");
        }
    }

    private void closeShutter() {
        if (mOccView != null) {
            mOccView.setVisibility(View.VISIBLE);
            mOccView.setBackgroundColor(OCC_COLOR);
            mOccViewStatus = OCC_VIEW_CLOSED;
            writeToLoggingFile("occView", "close");
        }
    }

    public void triggerOneOcclusion(){
        mOccView.post(screenCloseDelayedOpen);
    }

    private Runnable screenOpen = new Runnable() {
        public void run() {
            openShutter();
        }
    };

    private Runnable screenCloseDelayedOpen = new Runnable() {
        public void run() {
            closeShutter();
            mOccView.postDelayed(screenOpen, OCC_VIEW_CLOSE_MS);//schedule the next open
        }
    };


    private void saveToPrefs(){
        //save changes to app preferences
        SharedPreferences settings = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("lastTelnumber", mTelNumber);
        editor.commit();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
        //no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //full light
        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 255);



        //load from preferences
        SharedPreferences settings = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        mTelNumber = settings.getInt("lastTelnumber", 0);
        mTelNumber++;
        if (mTelNumber >= TEL_NUMBERS.length){
            mTelNumber = 0;
        }
        saveToPrefs();

        
		
	    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.main);
				
	    getWakeLock();
        
        //get Layout for Widgets
        mExperimentWidgetLayout = (RelativeLayout)findViewById(R.id.RelativeLayoutExperimentWidget);



        switch (mThisIsActivity) {
            case ACTIVITY_1:
                //toasting("1",2000);
                mFile_ext = ".telN.txt";
                mTask = new NumPad((byte) 1, this, mExperimentWidgetLayout, "", new String[]{TEL_NUMBERS[mTelNumber]}, 0, ExperimentWidget.DELAY_NONE);
                break;
            case ACTIVITY_2:
                //toasting("2",2000);
                mFile_ext = ".telD.txt";
                mTask = new NumPad((byte) 2, this, mExperimentWidgetLayout, "", new String[]{TEL_NUMBERS[mTelNumber]}, 8000, ExperimentWidget.DELAY_INDETERMINED);
                break;
            case ACTIVITY_3:
                //toasting("3",2000);
                mFile_ext = ".telO.txt";
                mTask = new NumPadOcc((byte) 3, this, mExperimentWidgetLayout, "", new String[]{TEL_NUMBERS[mTelNumber]}, 0, ExperimentWidget.DELAY_NONE);
                break;
            default:
                mTask = new NumPad((byte) 4, this, mExperimentWidgetLayout, "", new String[]{TEL_NUMBERS[mTelNumber]}, 0, ExperimentWidget.DELAY_NONE);
                break;
        }



        TextView instruction = (TextView) findViewById(R.id.instruction);
        instruction.setText(TEL_NUMBERS[mTelNumber]);

        // get OK-Button
        mOkButton = (Button) findViewById(R.id.OkButton);
                
        // OnClickListener fÃ¼r OK-Button
        mOkButton.setOnClickListener (new View.OnClickListener() {
        	public void onClick (View v) {
                if(occViewIsOpen()) {
                    done(mTask);
                }//else discard

        	}
        });



        prepareLogging();

        if (mLogFile != null) writeToLoggingFile("app", "start" + CSV_DELIMITER + Integer.toString(mThisIsActivity));

        startTask();


	    
	}

	

	  @Override
	    protected void onStop() {
	        super.onStop();
	        

	    }	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//this.unregisterReceiver(mReceiver);

		
        if(mWakeLock != null){
         	mWakeLock.release();
        }
		
	}
	
	@Override
	public void onPause() {
        super.onPause();

	    //unregisterReceiver(mReceiver);
        
        if (mLogFile != null) writeToLoggingFile("app", "pause");

        removeOccView();

        finish();//not just pause end/exit
        
	}
	
	
	@Override
	public void onResume() {
        super.onResume();

        if (mLogFile != null) writeToLoggingFile("app", "resume");

        prepareOccView();
        
	}
	
   protected void getWakeLock(){
	    try{
			PowerManager powerManger = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        mWakeLock = powerManger.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.FULL_WAKE_LOCK, "de.tum.ergonomie.buttons");
	        mWakeLock.acquire();
		}catch(Exception e){
       	Log.e(TAG,"get wakelock failed:"+ e.getMessage());
		}	
   }
   


	//---LOGGING--------------------	
	public void prepareLogging(){

		File folder = null;
		SimpleDateFormat  dateFormat = new SimpleDateFormat(FOLDER_DATE_STR);
		String folderTimeStr =  dateFormat.format(new Date());
		String timestamp = Long.toString(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
		

	   try{
		   //try to prepare external logging
		   String folderStr = Environment.getExternalStorageDirectory () + File.separator + FOLDER + File.separator + folderTimeStr;
		   mLogFile = new File(folderStr, timestamp + mFile_ext);
		   folder = new File(folderStr);
		   folder.mkdirs();//create missing dirs
		   mLogFile.createNewFile();
		   if (!mLogFile.canWrite()) throw new Exception();
	   }catch(Exception e){
		   try{
	    	   error("maybe no SD card inserted");//toast
			   finish();//we quit. we will not continue without file logging

			   //we do not log to internal memory, its not so easy to get the files back, external is easier via usb mass storage
			   /*
			   //try to prepare internal logging
				File intfolder = getApplicationContext().getDir("data", Context.MODE_WORLD_WRITEABLE);
				String folderStr = intfolder.getAbsolutePath() + File.separator + folderTimeStr;
				toasting("logging internal to: " +folderStr, Toast.LENGTH_LONG);
				file = new File(folderStr, timestamp + FILE_EXT);
			    folder = new File(folderStr);
			    folder.mkdirs();//create missing dirs
				file.createNewFile();
				if (!file.canWrite()) throw new Exception();
				*/
		   }catch(Exception e2){
			   mLogFile= null;
	    	   error("exception during prepareLogging(): " + e2.getMessage());//toast
			   finish();//we quit. we will not continue without file logging
		   }//catch(Exception e2)
	   }//catch(Exception e)	   
		   
		
	   try{
		String header = HEADER + CSV_DELIMITER + getVersionString() + "\r\n";
	    byte[] headerBytes = header.getBytes("US-ASCII");
		writeToFile(headerBytes,mLogFile);

	   }catch(Exception e3){
		   error("error writing header: "+e3.getMessage());//toast
		   finish();//we quit. we will not continue without file logging
	   }		   
		   
	}	

	
	public void writeToLoggingFile(String reason, String data){
		 StringBuilder log = new StringBuilder(2048);
		 log.append(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
		 log.append(CSV_DELIMITER);
		 log.append(reason);
		 log.append(CSV_DELIMITER);
		 log.append(data);
		 log.append(CSV_LINE_END);
		 		 
		 
		   try{
			   String tempStr = log.toString();
			    byte[] bytes = tempStr.getBytes("US-ASCII");
				writeToFile(bytes,mLogFile);
			   }catch(Exception e){
				   error("error writing log data: "+e.getMessage());//toast
				   finish();//we quit. we will not continue without file logging
			   }		
	}
	
	public void writeToFile(byte[] data, File file){
		   		       		
   		if (data == null){//error
       		error("writeFile() data==null?!");
       		finish();//we quit. we will not continue without proper file logging
   		}
   		
		FileOutputStream dest = null; 
							
		try {
			dest = new FileOutputStream(file, true);
			dest.write(data);
		}catch(Exception e){
			error("writeFile() failed. msg: " + e.getMessage());
       		finish();//we quit. we will not continue without file logging
			
		}finally {
			try{
				dest.flush();
				dest.close();
			}catch(Exception e){}
		}
		
		return;
   }	
	
	private void error(final String msg){//toast and log some errors
		toasting(msg, Toast.LENGTH_LONG);
		Log.e(TAG,msg);
	}
	
	public void toasting(final String msg, final int duration){
		Context context = getApplicationContext();
		CharSequence text = msg;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();		
	}
	
	private String getVersionString(){
		String retString = "";
		String appVersionName = "";
		int appVersionCode = 0;
		try{
			appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
			appVersionCode= getPackageManager().getPackageInfo(getPackageName(), 0 ).versionCode;
		}catch (Exception e) {
			Log.e(TAG, "getVersionString failed: "+e.getMessage());
		 }
		
		retString = "V"+appVersionName+"."+appVersionCode;
		
		return retString;
	}

	
    private Runnable delayedShow = new Runnable() {
		public void run() {
            writeToLoggingFile("endOfDelay", "");
			mDelayInProgress = false;
		    mProgress.dismiss();
		    mTask.show();

            TextView instruction = (TextView) findViewById(R.id.instruction);
            instruction.setVisibility(View.VISIBLE);

		} 			    	
    };	
    
	public static final byte UPDATE_PROGRESS_MS = 50;
	private long mLastDelayedUpdateTime = 50;
	
    private Runnable updateProgess = new Runnable() {
 		public void run() {
 			long now = System.currentTimeMillis();
 			mProgress.incrementProgressBy((int)(now - mLastDelayedUpdateTime));//+10 sligthly adjust for time drift
 			mLastDelayedUpdateTime = now;
 			if (mProgress.isShowing()){
 				mHandler.postDelayed(updateProgess,UPDATE_PROGRESS_MS);
 			}
 			
 		} 			    	
     };   
	
	public void startTask(){
		//findViewById(R.id.InstructionLayout).setVisibility(View.GONE);
		
		if(mTask.getDelay() > 0){
			mDelayInProgress = true;
			//sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_DELAY, false, false);
			mHandler.postDelayed(delayedShow,mTask.getDelay());//start with delay the show() command

	        //mProgress = new ProgressDialog(this,R.style.myProgressBar);
	        mProgress = new ProgressDialog(this,ProgressDialog.THEME_TRADITIONAL);
	        mProgress.setCancelable(false);
	        mProgress.setCanceledOnTouchOutside(false);

            TextView instruction = (TextView) findViewById(R.id.instruction);

		    switch (mTask.getDelayMode() ) {
	    	case ExperimentWidget.DELAY_DETERMINED:
			    mProgress.setProgress(0);
			    mProgress.setProgressNumberFormat(null);
			    mProgress.setIndeterminate(false);
			    mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			    mProgress.setMax((int)mTask.getDelay());//delay max ~30 sec!!!
			    mProgress.show();
			    mLastDelayedUpdateTime = System.currentTimeMillis();
			    mHandler.postDelayed(updateProgess,UPDATE_PROGRESS_MS);
                instruction.setVisibility(View.INVISIBLE);
	    		break;
	    	case ExperimentWidget.DELAY_INDETERMINED:
			    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			    mProgress.setIndeterminate(true);
			    mProgress.show();
                instruction.setVisibility(View.INVISIBLE);
	    		break;	    		
	    	case ExperimentWidget.DELAY_NOINDICATION:
                instruction.setVisibility(View.INVISIBLE);
	    		break;

	        default:
		    }

		     

		}else{
			mDelayInProgress = false;
			mTask.show();	//start dircetly
			//sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_TRIAL1, false, false);
		}
	}
		
	
	public void traceUserInput(String data){
        if (mLogFile != null) writeToLoggingFile("trace", data);
	}
	
	// Method that gets called as soon as the User finishes a task with ok-button OR
	// the experiment widgets calls done functionality (e.g., by rotary knob 'ok/done'-push)
	public void done(ExperimentWidget expWidget){
		
		
		if (!mTask.userChangedSomething()) return;// if user changed nothing, do nothing just return


		//data logging
        String data = expWidget.getDesiredResults()[0];
		data += CSV_DELIMITER;
		data += expWidget.getResult();		
	    writeToLoggingFile("result", data);

        finish();

	}

	

	
		
}