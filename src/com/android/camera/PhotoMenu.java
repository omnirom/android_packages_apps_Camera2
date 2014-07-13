/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import java.util.Locale;

import android.content.res.Resources;
import android.hardware.Camera.Parameters;

import com.android.camera.ui.AbstractSettingPopup;
import com.android.camera.ui.CountdownTimerPopup;
import com.android.camera.ui.ListPrefSettingPopup;
import com.android.camera.ui.MoreSettingPopup;
import com.android.camera.ui.PieItem;
import com.android.camera.ui.PieItem.OnClickListener;
import com.android.camera.ui.PieRenderer;
import com.android.camera2.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.util.Log;

import java.util.Locale;

public class PhotoMenu extends PieController
        implements MoreSettingPopup.Listener,
         CountdownTimerPopup.Listener,
        ListPrefSettingPopup.Listener {
    private static String TAG = "PhotoMenu";

    private final String mSettingOff;

    private String[] mOtherKeys1;
    private String[] mOtherKeys2;
    private String[] mOtherKeys3;
    private MoreSettingPopup mPopup1;
    private MoreSettingPopup mPopup2;
    private MoreSettingPopup mPopup3;
    private static final int POPUP_NONE = 0;
    private static final int POPUP_FIRST_LEVEL = 1;
    private static final int POPUP_SECOND_LEVEL = 2;
    private PhotoUI mUI;
    private int mPopupStatus;
    private AbstractSettingPopup mPopup;
    private CameraActivity mActivity;
    private int popupNum = 0;
    private PieItem mHdrItem = null;
    private PieItem mHdrPlusItem = null;
    private boolean mHdrOn = false;

    public PhotoMenu(CameraActivity activity, PhotoUI ui, PieRenderer pie) {
        super(activity, pie);
        mUI = ui;
        mSettingOff = activity.getString(R.string.setting_off_value);
        mActivity = activity;
    }

    public void initialize(PreferenceGroup group) {
        super.initialize(group);
        mPopup = null;
        mPopup1 = null;
        mPopup2 = null;
        mPopup3 = null;
        mPopupStatus = POPUP_NONE;
        PieItem item = null;
        popupNum = 0;
        final Resources res = mActivity.getResources();
        Locale locale = res.getConfiguration().locale;
        // The order is from left to right in the menu.

        // HDR+ (GCam).
        if (group.findPreference(CameraSettings.KEY_CAMERA_HDR_PLUS) != null) {
            mHdrPlusItem = makeSwitchItem(CameraSettings.KEY_CAMERA_HDR_PLUS, true);
            mRenderer.addItem(mHdrPlusItem);
        }

        // HDR.
        if (group.findPreference(CameraSettings.KEY_CAMERA_HDR) != null) {
            mHdrItem = makeSwitchItem(CameraSettings.KEY_CAMERA_HDR, true);
            mRenderer.addItem(mHdrItem);
        }

        mOtherKeys1 = new String[] {
                CameraSettings.KEY_SCENE_MODE,
                CameraSettings.KEY_RECORD_LOCATION,
                CameraSettings.KEY_PICTURE_SIZE,
                CameraSettings.KEY_HISTOGRAM,
                CameraSettings.KEY_JPEG_QUALITY,
                CameraSettings.KEY_ZSL,
                CameraSettings.KEY_TIMER,
                CameraSettings.KEY_TIMER_SOUND_EFFECTS,
                CameraSettings.KEY_CAMERA_SAVEPATH,
                CameraSettings.KEY_LONGSHOT
        };

        mOtherKeys2 = new String[] {
                CameraSettings.KEY_COLOR_EFFECT,
                CameraSettings.KEY_FACE_DETECTION,
                CameraSettings.KEY_FACE_RECOGNITION,
                CameraSettings.KEY_TOUCH_AF_AEC,
                CameraSettings.KEY_SELECTABLE_ZONE_AF,
                CameraSettings.KEY_PICTURE_FORMAT,
                CameraSettings.KEY_SATURATION,
                CameraSettings.KEY_CONTRAST,
                CameraSettings.KEY_SHARPNESS,
                CameraSettings.KEY_AUTOEXPOSURE
        };

        mOtherKeys3 = new String[] {
                CameraSettings.KEY_ANTIBANDING,
                CameraSettings.KEY_ISO,
                CameraSettings.KEY_DENOISE,
                CameraSettings.KEY_ADVANCED_FEATURES,
                CameraSettings.KEY_EXPOSURE,
                CameraSettings.KEY_WHITE_BALANCE,
                CameraSettings.KEY_FLASH_MODE,
                CameraSettings.KEY_FOCUS_MODE,
                CameraSettings.KEY_REDEYE_REDUCTION,
                CameraSettings.KEY_AE_BRACKET_HDR
        };

        PieItem item1 = makeItem(R.drawable.ic_settings_holo_light);
        item1.setLabel(mActivity.getResources().getString(R.string.camera_menu_more_label));
        item1.setOnClickListener(new OnClickListener() {
             @Override
            public void onClick(PieItem item) {
                if (mPopup1 == null || mPopupStatus != POPUP_FIRST_LEVEL){
                    initializePopup();
                mPopupStatus = POPUP_FIRST_LEVEL;
                }
                mUI.showPopup(mPopup1);
                popupNum = 1;
            }
        });
        mRenderer.addItem(item1);

        PieItem item2 = makeItem(R.drawable.ic_settings_holo_light);
        item2.setLabel(mActivity.getResources().getString(R.string.camera_menu_more_label));
        item2.setOnClickListener(new OnClickListener() {
             @Override
            public void onClick(PieItem item) {
                if (mPopup2 == null || mPopupStatus != POPUP_FIRST_LEVEL) {
                    initializePopup();
                    mPopupStatus = POPUP_FIRST_LEVEL;
                }
                mUI.showPopup(mPopup2);
                popupNum = 2;
            }
        });
        mRenderer.addItem(item2);

        PieItem item3= makeItem(R.drawable.ic_settings_holo_light);
        item3.setLabel(mActivity.getResources().getString(R.string.camera_menu_more_label));
        item3.setOnClickListener(new OnClickListener() {
             @Override
            public void onClick(PieItem item) {
                if (mPopup3 == null || mPopupStatus != POPUP_FIRST_LEVEL) {
                    initializePopup();
                    mPopupStatus = POPUP_FIRST_LEVEL;
                }
                mUI.showPopup(mPopup3);
                popupNum = 3;
            }
        });
        mRenderer.addItem(item3);

        // Camera switcher.
        if (group.findPreference(CameraSettings.KEY_CAMERA_ID) != null) {
            item = makeSwitchItem(CameraSettings.KEY_CAMERA_ID, false);
            final PieItem fitem = item;
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(PieItem item) {
                    // Find the index of next camera.
                    ListPreference pref = mPreferenceGroup
                            .findPreference(CameraSettings.KEY_CAMERA_ID);
                    if (pref != null) {
                        int index = pref.findIndexOfValue(pref.getValue());
                        CharSequence[] values = pref.getEntryValues();
                        index = (index + 1) % values.length;
                        pref.setValueIndex(index);
                        mListener.onCameraPickerClicked(index);
                    }
                    updateItem(fitem, CameraSettings.KEY_CAMERA_ID);
                }
            });
            mRenderer.addItem(item);
        }
    }

    @Override
    // Hit when an item in a popup gets selected
    public void onListPrefChanged(ListPreference pref) {
        if (mPopup != null && mPopup1 != null && mPopup2 != null && mPopup3 != null) {
               mUI.dismissPopup();
        }
        onSettingChanged(pref);
    }

   @Override
    public void overrideSettings(final String ... keyvalues) {
        super.overrideSettings(keyvalues);
       if ((mPopup1 == null) &&  (mPopup2 == null)  &&  (mPopup3 == null)) initializePopup();
        mPopup1.overrideSettings(keyvalues);
        mPopup2.overrideSettings(keyvalues);
        mPopup3.overrideSettings(keyvalues);
    }

    protected void initializePopup() {
     LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(
             Context.LAYOUT_INFLATER_SERVICE);
     MoreSettingPopup popup1 = (MoreSettingPopup) inflater.inflate(
             R.layout.more_setting_popup, null, false);
     popup1.setSettingChangedListener(this);
     popup1.initialize(mPreferenceGroup, mOtherKeys1);
     if (mActivity.isSecureCamera()) {
         // Prevent location preference from getting changed in secure camera mode
       popup1.setPreferenceEnabled(CameraSettings.KEY_RECORD_LOCATION,false);
     }
     mPopup1 = popup1;

     MoreSettingPopup popup2 = (MoreSettingPopup) inflater.inflate(
             R.layout.more_setting_popup, null, false);
     popup2.setSettingChangedListener(this);
     popup2.initialize(mPreferenceGroup, mOtherKeys2);
     mPopup2 = popup2;

     MoreSettingPopup popup3 = (MoreSettingPopup) inflater.inflate(
             R.layout.more_setting_popup, null, false);
     popup3.setSettingChangedListener(this);
     popup3.initialize(mPreferenceGroup, mOtherKeys3);
     mPopup3 = popup3;

     ListPreference pref = mPreferenceGroup.findPreference(
             CameraSettings.KEY_SCENE_MODE);
     String sceneMode = (pref != null) ? pref.getValue() : null;
     pref = mPreferenceGroup.findPreference(CameraSettings.KEY_FACE_DETECTION);
     String faceDetection = (pref != null) ? pref.getValue() : null;
     pref = mPreferenceGroup.findPreference(CameraSettings.KEY_ZSL);
     String zsl = (pref != null) ? pref.getValue() : null;
     if ((sceneMode != null) && !Parameters.SCENE_MODE_AUTO.equals(sceneMode)){
         popup3.setPreferenceEnabled(CameraSettings.KEY_FOCUS_MODE,false);
         popup2.setPreferenceEnabled(CameraSettings.KEY_AUTOEXPOSURE,false);
         popup2.setPreferenceEnabled(CameraSettings.KEY_TOUCH_AF_AEC,false);
         popup2.setPreferenceEnabled(CameraSettings.KEY_SATURATION,false);
         popup2.setPreferenceEnabled(CameraSettings.KEY_CONTRAST,false);
         popup2.setPreferenceEnabled(CameraSettings.KEY_SHARPNESS,false);
         popup2.setPreferenceEnabled(CameraSettings.KEY_COLOR_EFFECT,false);
         popup3.setPreferenceEnabled(CameraSettings.KEY_FLASH_MODE,false);
         popup3.setPreferenceEnabled(CameraSettings.KEY_WHITE_BALANCE,false);
         popup3.setPreferenceEnabled(CameraSettings.KEY_EXPOSURE,false);
     }
     if ((zsl != null) && Parameters.ZSL_ON.equals(zsl)) {
         popup3.setPreferenceEnabled(CameraSettings.KEY_FOCUS_MODE,false);
     }
     if ((faceDetection != null) && !Parameters.FACE_DETECTION_ON.equals(faceDetection)){
         popup2.setPreferenceEnabled(CameraSettings.KEY_FACE_RECOGNITION,false);
     }

     pref = mPreferenceGroup.findPreference(CameraSettings.KEY_ADVANCED_FEATURES);
     String advancedFeatures = (pref != null) ? pref.getValue() : null;

     String ubiFocusOn = mActivity.getString(R.string.
         pref_camera_advanced_feature_value_ubifocus_on);
     String chromaFlashOn = mActivity.getString(R.string.
         pref_camera_advanced_feature_value_chromaflash_on);
     String optiZoomOn = mActivity.getString(R.string.
         pref_camera_advanced_feature_value_optizoom_on);

     if ((zsl != null) && Parameters.ZSL_OFF.equals(zsl)) {
         popup3.overrideSettings(CameraSettings.KEY_ADVANCED_FEATURES,
                 mActivity.getString(R.string.pref_camera_advanced_feature_default));

         popup3.setPreferenceEnabled(CameraSettings.KEY_ADVANCED_FEATURES,false);
         if (mHdrItem != null) {
             mHdrItem.setEnabled(true);
         }
         if (mHdrPlusItem != null) {
             mHdrPlusItem.setEnabled(true);
         }
     } else {
         if ((advancedFeatures != null) && (advancedFeatures.equals(ubiFocusOn) ||
                 advancedFeatures.equals(chromaFlashOn) ||
                 advancedFeatures.equals(optiZoomOn))) {
             popup3.setPreferenceEnabled(CameraSettings.KEY_FOCUS_MODE,false);
             popup3.setPreferenceEnabled(CameraSettings.KEY_FLASH_MODE,false);
             popup3.setPreferenceEnabled(CameraSettings.KEY_AE_BRACKET_HDR,false);
             popup3.setPreferenceEnabled(CameraSettings.KEY_REDEYE_REDUCTION,false);
             popup3.setPreferenceEnabled(CameraSettings.KEY_EXPOSURE,false);
             popup2.setPreferenceEnabled(CameraSettings.KEY_COLOR_EFFECT,false);
             popup2.setPreferenceEnabled(CameraSettings.KEY_TOUCH_AF_AEC,false);
             popup1.setPreferenceEnabled(CameraSettings.KEY_SCENE_MODE,false);

             setPreference(CameraSettings.KEY_CAMERA_HDR, mSettingOff);

             if (mHdrItem != null) {
                mHdrItem.setEnabled(false);
             }
             if (mHdrPlusItem != null) {
                mHdrPlusItem.setEnabled(false);
             }
         } else {
             if (mHdrItem != null) {
                mHdrItem.setEnabled(true);
             }
             if (mHdrPlusItem != null) {
                mHdrPlusItem.setEnabled(true);
             }
         }
     }
     }

    public void popupDismissed() {
        if (mPopupStatus == POPUP_SECOND_LEVEL) {
            initializePopup();
            mPopupStatus = POPUP_FIRST_LEVEL;
                if (popupNum == 1)
                    mUI.showPopup(mPopup1);
                else if (popupNum == 2)
                    mUI.showPopup(mPopup2);
                else if (popupNum == 3)
                    mUI.showPopup(mPopup3);
                if(mPopup1 != null) mPopup1 = null;
                if(mPopup2 != null) mPopup2 = null;
                if(mPopup3 != null) mPopup3 = null;
        } else {
            initializePopup();
        }

    }

        @Override
    // Hit when an item in the first-level popup gets selected, then bring up
    // the second-level popup
    public void onPreferenceClicked(ListPreference pref) {
        if (mPopupStatus != POPUP_FIRST_LEVEL) return;

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ListPrefSettingPopup basic = (ListPrefSettingPopup) inflater.inflate(
                R.layout.list_pref_setting_popup, null, false);
        basic.initialize(pref);
        basic.setSettingChangedListener(this);
        mUI.dismissPopup();
        mPopup = basic;
        mUI.showPopup(mPopup);
        mPopupStatus = POPUP_SECOND_LEVEL;
    }

    // Return true if the preference has the specified key but not the value.
    private static boolean notSame(ListPreference pref, String key, String value) {
        return (key.equals(pref.getKey()) && !value.equals(pref.getValue()));
    }

    private void setPreference(String key, String value) {
        ListPreference pref = mPreferenceGroup.findPreference(key);
        if (pref != null && !value.equals(pref.getValue())) {
            pref.setValue(value);
            reloadPreferences();
        }
    }

    @Override
    public void onSettingChanged(ListPreference pref) {
        // Reset the scene mode if HDR is set to on. Reset HDR if scene mode is
        // set to non-auto.
        if (notSame(pref, CameraSettings.KEY_CAMERA_HDR, mSettingOff)) {
            setPreference(CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO);
            Toast.makeText(mActivity, R.string.hdr_enable_message,
                            Toast.LENGTH_LONG).show();
            mHdrOn = true;
        } else if (notSame(pref, CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO)) {
            setPreference(CameraSettings.KEY_CAMERA_HDR, mSettingOff);
            if (mHdrOn) {
                Toast.makeText(mActivity, R.string.scene_enable_message,
                            Toast.LENGTH_LONG).show();
            }
            mHdrOn = false;
        }
        super.onSettingChanged(pref);
    }

}
