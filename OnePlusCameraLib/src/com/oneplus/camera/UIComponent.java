package com.oneplus.camera;

import java.util.ArrayList;
import java.util.List;

import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.PathInterpolator;

import com.oneplus.base.PropertyChangeEventArgs;
import com.oneplus.base.PropertyChangedCallback;
import com.oneplus.base.PropertyKey;
import com.oneplus.base.PropertySource;
import com.oneplus.base.Rotation;
import com.oneplus.widget.ViewUtils;

/**
 * Base class for UI related component.
 */
public abstract class UIComponent extends CameraComponent
{
	/**
	 * Duration of UI fade-in in milliseconds.
	 */
	public static final long DURATION_FADE_IN = 600;
	/**
	 * Duration of UI fade-out in milliseconds.
	 */
	public static final long DURATION_FADE_OUT = 600;
	/**
	 * Duration of UI rotation in milliseconds.
	 */
	public static final long DURATION_ROTATION = 600;
	/**
	 * Interpolator for UI fade-in animation.
	 */
	public static final TimeInterpolator INTERPOLATOR_FADE_IN = null;
	/**
	 * Interpolator for UI fade-out animation.
	 */
	public static final TimeInterpolator INTERPOLATOR_FADE_OUT = null;
	/**
	 * Interpolator for UI rotation animation.
	 */
	public static final TimeInterpolator INTERPOLATOR_ROTATION = new PathInterpolator(0.8f, 0, 0.2f, 1);
	
	
	// Private fields.
	private List<View> m_AutoRotateViews;
	private Rotation m_Rotation = Rotation.LANDSCAPE;
	
	
	// Call-backs.
	private final PropertyChangedCallback<Boolean> m_CaptureUIEnabledChangedCallback = new PropertyChangedCallback<Boolean>()
	{
		@Override
		public void onPropertyChanged(PropertySource source, PropertyKey<Boolean> key, PropertyChangeEventArgs<Boolean> e)
		{
			onCaptureUIEnabledStateChanged(e.getNewValue());
		}
	};
	private final PropertyChangedCallback<Rotation> m_RotationChangedCallback = new PropertyChangedCallback<Rotation>()
	{
		@Override
		public void onPropertyChanged(PropertySource source, PropertyKey<Rotation> key, PropertyChangeEventArgs<Rotation> e)
		{
			onRotationChanged(e.getOldValue(), e.getNewValue());
		}
	};
	
	
	// Static initializer.
	static
	{}
	
	
	/**
	 * Initialize new CameraComponent instance.
	 * @param name Component name.
	 * @param activity {@link CameraActivity} instance.
	 * @param hasHandler Whether internal {@link android.os.Handler Handler} should be created or not.
	 */
	protected UIComponent(String name, CameraActivity cameraActivity, boolean hasHandler)
	{
		super(name, cameraActivity, hasHandler);
	}
	
	
	/**
	 * Add view to auto rotate list.
	 * @param view View to add.
	 */
	protected void addAutoRotateView(View view)
	{
		this.verifyAccess();
		if(m_AutoRotateViews == null)
			m_AutoRotateViews = new ArrayList<>();
		m_AutoRotateViews.add(view);
		this.rotateView(view, m_Rotation, 0);
	}
	
	
	/**
	 * Get current camera activity rotation.
	 * @return Current activity rotation.
	 */
	protected final Rotation getCameraActivityRotation()
	{
		return this.getCameraActivity().get(CameraActivity.PROP_ACTIVITY_ROTATION);
	}
	
	
	/**
	 * Get current UI rotation.
	 * @return Current rotation.
	 */
	protected final Rotation getRotation()
	{
		return this.getCameraActivity().get(CameraActivity.PROP_ROTATION);
	}
	
	
	// Deinitialize.
	@Override
	protected void onDeinitialize()
	{
		CameraActivity cameraActivity = this.getCameraActivity();
		cameraActivity.removeCallback(CameraActivity.PROP_IS_CAPTURE_UI_ENABLED, m_CaptureUIEnabledChangedCallback);
		cameraActivity.removeCallback(CameraActivity.PROP_ROTATION, m_RotationChangedCallback);
		super.onDeinitialize();
	}
	
	
	/**
	 * Check whether capture UI is enabled or not.
	 * @return Capture UI state.
	 */
	protected final boolean isCaptureUIEnabled()
	{
		return this.getCameraActivity().get(CameraActivity.PROP_IS_CAPTURE_UI_ENABLED);
	}
	
	
	// Initialize.
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		CameraActivity cameraActivity = this.getCameraActivity();
		cameraActivity.addCallback(CameraActivity.PROP_IS_CAPTURE_UI_ENABLED, m_CaptureUIEnabledChangedCallback);
		cameraActivity.addCallback(CameraActivity.PROP_ROTATION, m_RotationChangedCallback);
		m_Rotation = this.getRotation();
	}
	
	
	/**
	 * Called when capture UI enabled state changes.
	 * @param isEnabled Whether capture UI isaenabled or not.
	 */
	protected void onCaptureUIEnabledStateChanged(boolean isEnabled)
	{}
	
	
	/**
	 * Called when UI rotation changes.
	 * @param prevRotation Previous rotation.
	 * @param newRotation New rotation.
	 */
	protected void onRotationChanged(Rotation prevRotation, Rotation newRotation)
	{
		m_Rotation = newRotation;
		if(m_AutoRotateViews != null)
		{
			for(int i = m_AutoRotateViews.size() - 1 ; i >= 0 ; --i)
				this.rotateView(m_AutoRotateViews.get(i), newRotation);
		}
	}
	
	
	/**
	 * Remove view from auto rotate list.
	 * @param view View to remove.
	 */
	protected void removedAutoRotateView(View view)
	{
		this.verifyAccess();
		if(m_AutoRotateViews != null)
			m_AutoRotateViews.remove(view);
	}
	
	
	/**
	 * Change rotation of view.
	 * @param view View to change rotation.
	 * @param toRotation New rotation.
	 */
	protected void rotateView(View view, Rotation toRotation)
	{
		this.rotateView(view, toRotation, DURATION_ROTATION, INTERPOLATOR_ROTATION);
	}
	
	
	/**
	 * Change rotation of view.
	 * @param view View to change rotation.
	 * @param toRotation New rotation.
	 * @param duration Animation duration.
	 */
	protected void rotateView(View view, Rotation toRotation, long duration)
	{
		this.rotateView(view, toRotation, duration, INTERPOLATOR_ROTATION);
	}
	
	
	/**
	 * Change rotation of view.
	 * @param view View to change rotation.
	 * @param toRotation New rotation.
	 * @param duration Animation duration.
	 * @param interpolator Animation interpolator.
	 */
	protected void rotateView(View view, Rotation toRotation, long duration, TimeInterpolator interpolator)
	{
		if(view == null)
			return;
		Rotation baseRotation = this.getCameraActivity().get(CameraActivity.PROP_ACTIVITY_ROTATION);
		float fromDegrees = view.getRotation();
		float toDegrees = (baseRotation.getDeviceOrientation() - toRotation.getDeviceOrientation());
		if(Math.abs(fromDegrees - toDegrees) < 0.1f)
			return;
		if(duration > 0)
		{
			if(Math.abs(toDegrees - fromDegrees) > 180)
			{
				if(fromDegrees > toDegrees)
					view.setRotation(fromDegrees - 360);
				else
					view.setRotation(fromDegrees + 360);
			}
			ViewUtils.rotate(view, toDegrees, duration, interpolator);
		}
		else
			view.setRotation(toDegrees);
	}
	
	
	/**
	 * Change visibility of view.
	 * @param view View to change visibility.
	 * @param isVisible Visibility.
	 */
	protected void setViewVisibility(View view, boolean isVisible)
	{
		this.setViewVisibility(view, isVisible, null);
	}
	
	
	/**
	 * Change visibility of view.
	 * @param view View to change visibility.
	 * @param isVisible Visibility.
	 * @param callback Animation call-back.
	 */
	protected void setViewVisibility(View view, boolean isVisible, ViewUtils.AnimationCompletedCallback callback)
	{
		long duration;
		TimeInterpolator interpolator;
		if(isVisible)
		{
			duration = DURATION_FADE_IN;
			interpolator = INTERPOLATOR_FADE_IN;
		}
		else
		{
			duration = 0;
			interpolator = null;
		}
		ViewUtils.setVisibility(view, isVisible, duration, interpolator, callback);
	}
	
	
	/**
	 * Change visibility of view.
	 * @param view View to change visibility.
	 * @param isVisible Visibility.
	 * @param duration Animation duration in milliseconds.
	 * @param interpolator Interpolator.
	 */
	protected void setViewVisibility(View view, boolean isVisible, long duration, TimeInterpolator interpolator)
	{
		ViewUtils.setVisibility(view, isVisible, duration, interpolator);
	}
	
	
	/**
	 * Change visibility of view.
	 * @param view View to change visibility.
	 * @param isVisible Visibility.
	 * @param duration Animation duration in milliseconds.
	 * @param interpolator Interpolator.
	 * @param callback Animation call-back.
	 */
	protected void setViewVisibility(View view, boolean isVisible, long duration, TimeInterpolator interpolator, ViewUtils.AnimationCompletedCallback callback)
	{
		ViewUtils.setVisibility(view, isVisible, duration, interpolator, callback);
	}
}
