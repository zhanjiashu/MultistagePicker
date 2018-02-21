/*
 * Copyright 2018 stingzhan. https://github.com/stingzhan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.zhanjiashu.library

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.zhanjiashu.library.adapter.MultistagePickerDataProvider
import io.zhanjiashu.library.internal.MultistagePickerView

class MultistagePicker(context: Context) {

    private val pickerView: MultistagePickerView = MultistagePickerView(context)

    fun setDataProvider(provider: MultistagePickerDataProvider) {
        pickerView.setDataProvider(provider)
    }

    fun setOnPickCompletedListener(l: (selectedOptions: Map<String, String>) -> Unit) {
        pickerView.completedListener = l
    }

    private fun getView(): View {
        return pickerView.view
    }

    fun bindToLayout(layout: ViewGroup) {
        layout.addView(getView())
    }

}