/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;
import android.content.Context;
import android.widget.TextView;
import com.android.settings.R;

public class ThemeStatus {
    private final Context mContext;
    private TextView mText;

    public ThemeStatus(Context context, TextView text_) {
        mContext = context;
        mText = text_;
    }
    private void updateText() {
        String theme = mContext.getResources().getConfiguration().customTheme.getThemeId();
        mText.setText(theme);
    }
    public void resume() {
        updateText();
    }

    public void pause() {
    }
    public void setTextView(TextView text_) {
        if (mText == text_) return;
        mText = text_;
        updateText();
    }
}
