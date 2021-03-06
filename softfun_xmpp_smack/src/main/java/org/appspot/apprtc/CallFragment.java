/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.utils.ToastUtils;

import org.webrtc.RendererCommon.ScalingType;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
  private View controlView;
  private TextView contactView;
  private ImageButton disconnectButton;
  private ImageButton cameraSwitchButton;
  private ImageButton videoScalingButton;
  private ImageButton toggleMuteButton;
  private TextView captureFormatText;
  private SeekBar captureFormatSlider;
  private OnCallEvents callEvents;
  private ScalingType scalingType;
  private boolean videoCallEnabled = true;

  /**
   * Call control interface for container activity.
   */
  public interface OnCallEvents {
    void onCallHangUp();
    void onCameraSwitch();
    void onVideoScalingSwitch(ScalingType scalingType);
    void onCaptureFormatChange(int width, int height, int framerate);
    boolean onToggleMic();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    controlView =
        inflater.inflate(R.layout.fragment_rtc_call, container, false);

    // Create UI controls.
    contactView =
        (TextView) controlView.findViewById(R.id.contact_name_call);
    disconnectButton =
        (ImageButton) controlView.findViewById(R.id.button_call_disconnect);
    cameraSwitchButton =
        (ImageButton) controlView.findViewById(R.id.button_call_switch_camera);
    videoScalingButton =
        (ImageButton) controlView.findViewById(R.id.button_call_scaling_mode);
    toggleMuteButton =
        (ImageButton) controlView.findViewById(R.id.button_call_toggle_mic);
    captureFormatText =
        (TextView) controlView.findViewById(R.id.capture_format_text_call);
    captureFormatSlider =
        (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);

    // Add buttons click events.
    disconnectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCallHangUp();
      }
    });

    cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
        System.out.println("====================  摄像头个数：  ====================="+cameraCount);
        if(cameraCount>1){

          try {
            callEvents.onCameraSwitch();
          } catch (Exception e) {
            ToastUtils.showToastSafe_Long("出错了！！！！！！！！！");
            e.printStackTrace();
          }
        }

      }
    });

    videoScalingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
          videoScalingButton.setBackgroundResource(
              R.drawable.rtc_normal_screen);
          scalingType = ScalingType.SCALE_ASPECT_FIT;
        } else {
          videoScalingButton.setBackgroundResource(
              R.drawable.rtc_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FILL;
        }
        callEvents.onVideoScalingSwitch(scalingType);
      }
    });
    scalingType = ScalingType.SCALE_ASPECT_FILL;

    toggleMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean enabled = callEvents.onToggleMic();
        if(enabled)
            toggleMuteButton.setBackgroundResource(R.drawable.rtc_mic) ;
        else
            toggleMuteButton.setBackgroundResource(R.drawable.rtc_mic_dis);
          //toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    boolean captureSliderEnabled = false;
    Bundle args = getArguments();
    if (args != null) {
      String contactName = args.getString(CallActivity.EXTRA_ROOMID);
      String targetName = args.getString("mTargetNickName");
      contactView.setText("正在等待 "+targetName+" 接入...");
      videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
      captureSliderEnabled = videoCallEnabled
          && args.getBoolean(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
    }
    if (!videoCallEnabled) {
      cameraSwitchButton.setVisibility(View.INVISIBLE);
    }
    if (captureSliderEnabled) {
      captureFormatSlider.setOnSeekBarChangeListener(
          new CaptureQualityController(captureFormatText, callEvents));
    } else {
      captureFormatText.setVisibility(View.GONE);
      captureFormatSlider.setVisibility(View.GONE);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    callEvents = (OnCallEvents) activity;
  }


  public void setcontactViewText(String msg){
    contactView.setText(msg);
  }

}
