/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;
import com.android.settings.R;

/*
 * @author Danesh
 * @author nebkat
 */

public class QuadNumberPickerPreference extends DialogPreference {
    private int mMin1, mMax1, mDefault1;
    private int mMin2, mMax2, mDefault2;
    private int mMin3, mMax3, mDefault3;
    private int mMin4, mMax4, mDefault4;

    private String mMaxExternalKey1, mMinExternalKey1;
    private String mMaxExternalKey2, mMinExternalKey2;
    private String mMaxExternalKey3, mMinExternalKey3;
    private String mMaxExternalKey4, mMinExternalKey4;

    private String mPickerTitle1;
    private String mPickerTitle2;
    private String mPickerTitle3;
    private String mPickerTitle4;

    private NumberPicker mNumberPicker1;
    private NumberPicker mNumberPicker2;
    private NumberPicker mNumberPicker3;
    private NumberPicker mNumberPicker4;

    public QuadNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray dialogType = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.DialogPreference, 0, 0);
        TypedArray quadNumberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.QuadNumberPickerPreference, 0, 0);

        mMaxExternalKey1 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_maxExternal1);
        mMinExternalKey1 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_minExternal1);
        mMaxExternalKey2 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_maxExternal2);
        mMinExternalKey2 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_minExternal2);
        mMaxExternalKey3 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_maxExternal3);
        mMinExternalKey3 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_minExternal3);
        mMaxExternalKey4 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_maxExternal4);
        mMinExternalKey4 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_minExternal4);

        mPickerTitle1 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_pickerTitle1);
        mPickerTitle2 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_pickerTitle2);
        mPickerTitle3 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_pickerTitle3);
        mPickerTitle4 = quadNumberPickerType.getString(R.styleable.QuadNumberPickerPreference_pickerTitle4);

        mMax1 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_max1, 5);
        mMin1 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_min1, 0);
        mMax2 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_max2, 5);
        mMin2 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_min2, 0);
        mMax3 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_max3, 5);
        mMin3 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_min3, 0);
        mMax4 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_max4, 5);
        mMin4 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_min4, 0);

        mDefault1 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_defaultValue1, mMin1);
        mDefault2 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_defaultValue2, mMin2);
        mDefault3 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_defaultValue3, mMin3);
        mDefault4 = quadNumberPickerType.getInt(R.styleable.QuadNumberPickerPreference_defaultValue4, mMin4);

        dialogType.recycle();
        quadNumberPickerType.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        int max1 = mMax1;
        int min1 = mMin1;
        int max2 = mMax2;
        int min2 = mMin2;
        int max3 = mMax3;
        int min3 = mMin3;
        int max4 = mMax4;
        int min4 = mMin4;

        // External values
        if (mMaxExternalKey1 != null) {
            max1 = getSharedPreferences().getInt(mMaxExternalKey1, mMax1);
        }
        if (mMinExternalKey1 != null) {
            min1 = getSharedPreferences().getInt(mMinExternalKey1, mMin1);
        }
        if (mMaxExternalKey2 != null) {
            max2 = getSharedPreferences().getInt(mMaxExternalKey2, mMax2);
        }
        if (mMinExternalKey2 != null) {
            min2 = getSharedPreferences().getInt(mMinExternalKey2, mMin2);
        }

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.quad_number_picker_dialog, null);

        mNumberPicker1 = (NumberPicker) view.findViewById(R.id.number_picker_1);
        mNumberPicker2 = (NumberPicker) view.findViewById(R.id.number_picker_2);
        mNumberPicker3 = (NumberPicker) view.findViewById(R.id.number_picker_3);
        mNumberPicker4 = (NumberPicker) view.findViewById(R.id.number_picker_4);

        if (mNumberPicker1 == null || mNumberPicker2 == null) {
            throw new RuntimeException("mNumberPicker1 or mNumberPicker2 is null!");
        }

        // Initialize state
        mNumberPicker1.setWrapSelectorWheel(false);
        mNumberPicker1.setMaxValue(max1);
        mNumberPicker1.setMinValue(min1);
        mNumberPicker1.setValue(getPersistedValue(1));
        mNumberPicker2.setWrapSelectorWheel(false);
        mNumberPicker2.setMaxValue(max2);
        mNumberPicker2.setMinValue(min2);
        mNumberPicker2.setValue(getPersistedValue(2));
        mNumberPicker3.setWrapSelectorWheel(false);
        mNumberPicker3.setMaxValue(max3);
        mNumberPicker3.setMinValue(min3);
        mNumberPicker3.setValue(getPersistedValue(3));
        mNumberPicker4.setWrapSelectorWheel(false);
        mNumberPicker4.setMaxValue(max3);
        mNumberPicker4.setMinValue(min3);
        mNumberPicker4.setValue(getPersistedValue(4));

        // Titles
        TextView pickerTitle1 = (TextView) view.findViewById(R.id.picker_title_1);
        TextView pickerTitle2 = (TextView) view.findViewById(R.id.picker_title_2);
        TextView pickerTitle3 = (TextView) view.findViewById(R.id.picker_title_3);
        TextView pickerTitle4 = (TextView) view.findViewById(R.id.picker_title_4);

        if (pickerTitle1 != null && pickerTitle2 != null) {
            pickerTitle1.setText(mPickerTitle1);
            pickerTitle2.setText(mPickerTitle2);
        }

        if (pickerTitle3 != null && pickerTitle4 != null) {
            pickerTitle3.setText(mPickerTitle3);
            pickerTitle4.setText(mPickerTitle4);
        }

        // No keyboard popup
        EditText textInput1 = (EditText) mNumberPicker1.findViewById(com.android.internal.R.id.numberpicker_input);
        EditText textInput2 = (EditText) mNumberPicker2.findViewById(com.android.internal.R.id.numberpicker_input);
        EditText textInput3 = (EditText) mNumberPicker3.findViewById(com.android.internal.R.id.numberpicker_input);
        EditText textInput4 = (EditText) mNumberPicker4.findViewById(com.android.internal.R.id.numberpicker_input);

        if (textInput1 != null && textInput2 != null) {
            textInput1.setCursorVisible(false);
            textInput1.setFocusable(false);
            textInput1.setFocusableInTouchMode(false);
            textInput2.setCursorVisible(false);
            textInput2.setFocusable(false);
            textInput2.setFocusableInTouchMode(false);
        }

        if (textInput3 != null && textInput4 != null) {
            textInput3.setCursorVisible(false);
            textInput3.setFocusable(false);
            textInput3.setFocusableInTouchMode(false);
            textInput4.setCursorVisible(false);
            textInput4.setFocusable(false);
            textInput4.setFocusableInTouchMode(false);
        }

        return view;
    }

    private int getPersistedValue(int value) {
        String[] values = getPersistedString(mDefault1 + "|" + mDefault2 + "|" + mDefault3 + "|" + mDefault4).split("\\|");
        if (value == 1) {
            try {
                return Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                return mDefault1;
            }
        } else if (value == 2) {
            try {
                return Integer.parseInt(values[1]);
            } catch (NumberFormatException e) {
                return mDefault2;
            }
        } else if (value == 3) {
            try {
                return Integer.parseInt(values[2]);
            } catch (NumberFormatException e) {
                return mDefault2;
            }
        } else {
            try {
                return Integer.parseInt(values[3]);
            } catch (NumberFormatException e) {
                return mDefault2;
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (Math.abs(mNumberPicker2.getValue() - mNumberPicker1.getValue()) > 25 &&
                Math.abs(mNumberPicker4.getValue() - mNumberPicker3.getValue()) > 25) {
            Toast.makeText(getContext(), R.string.gesture_area_too_large,
                    Toast.LENGTH_LONG).show();
            positiveResult = false;
        }
        if (positiveResult) {
            String result = mNumberPicker1.getValue() + "|" + mNumberPicker2.getValue() + "|" + mNumberPicker3.getValue() + "|" + mNumberPicker4.getValue();
            persistString(result);
            callChangeListener(new String(result));
        }
    }

    public void setMin1(int min) {
        mMin1 = min;
    }
    public void setMax1(int max) {
        mMax1 = max;
    }
    public void setMin2(int min) {
        mMin2 = min;
    }
    public void setMax2(int max) {
        mMax2 = max;
    }
    public void setMin3(int min) {
        mMin3 = min;
    }
    public void setMax3(int max) {
        mMax3 = max;
    }
    public void setMin4(int min) {
        mMin4 = min;
    }
    public void setMax4(int max) {
        mMax4 = max;
    }
    public void setDefault1(int def) {
        mDefault1 = def;
    }
    public void setDefault2(int def) {
        mDefault2 = def;
    }
    public void setDefault3(int def) {
        mDefault3 = def;
    }
    public void setDefault4(int def) {
        mDefault4 = def;
    }

}
