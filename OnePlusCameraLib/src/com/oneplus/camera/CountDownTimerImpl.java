package com.oneplus.camera;

import android.os.SystemClock;

import com.oneplus.base.EventArgs;
import com.oneplus.base.Handle;
import com.oneplus.base.HandlerUtils;

public class CountDownTimerImpl extends CameraComponent implements CountDownTimer {
	// compute data
	private final long interval = 1000;
	private long countdownSecs = 0;
	private long elapsedTime = 0;

	// Constructor
	CountDownTimerImpl(CameraActivity cameraActivity)
	{
		super("CountDown Timer manager", cameraActivity, true);
	}	

	@Override
	public Handle start(long seconds, int flags) {
		verifyAccess();
		setReadOnly(PROP_IS_STARTED, Boolean.valueOf(true));
		elapsedTime = SystemClock.elapsedRealtime();
		countdownSecs = seconds;
		//start timer
		HandlerUtils.post(CountDownTimerImpl.this, new Runnable() {
			   @Override
			   public void run() {
				  // notify count down
				  setReadOnly(PROP_REMAINING_SECONDS, Long.valueOf(--countdownSecs));
				  // repeat or finish
				  if(countdownSecs != 0){
					  // compensate timer deviation
					  long currentTime = SystemClock.elapsedRealtime();
					  HandlerUtils.post(CountDownTimerImpl.this, this, interval - (currentTime - elapsedTime - 1000));
				      elapsedTime = currentTime;
				  }else{
					  // reset compute data
					  elapsedTime = 0;
					  countdownSecs = 0;
				  }
			   }}, interval);
		
		
		return new Handle("CountDownTimer"){
			// handle user cancel
			@Override
			protected void onClose(int flags) {
				raise(EVENT_CANCELLED,  EventArgs.EMPTY);
			}};
	}
}
