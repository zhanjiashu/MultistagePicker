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
import io.zhanjiashu.library.provider.*

class RegionPicker(context: Context) {

    private var addressPickSuccessListener: ((province: String, city: String, district: String) -> Unit)? = null

    private val picker = MultistagePicker(context).apply {
        setDataProvider(AddressPickerDataProvider(context))
        setOnPickCompletedListener { selectedOptions ->
            addressPickSuccessListener?.invoke(
                    selectedOptions[STAGE_KEY_PROVINCE] ?: "",
                    selectedOptions[STAGE_KEY_CITY] ?: "",
                    selectedOptions[STAGE_KEY_DISTRICT] ?: ""
            )
        }
    }

    fun setOnAddressPickSuccessListener(l: (province: String, city: String, district: String) -> Unit) {
        addressPickSuccessListener = l
    }

    fun show() {
        picker.show()
    }
}