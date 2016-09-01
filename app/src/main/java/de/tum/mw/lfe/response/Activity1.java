package de.tum.mw.lfe.response;


import android.os.Bundle;

public class Activity1 extends ExperimentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mThisIsActivity = ExperimentActivity.ACTIVITY_1;

        super.onCreate(savedInstanceState);//order important!

    }




		
}