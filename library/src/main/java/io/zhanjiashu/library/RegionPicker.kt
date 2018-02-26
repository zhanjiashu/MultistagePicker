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
import io.zhanjiashu.library.internal.PickerConfigInterface
import io.zhanjiashu.library.internal.PickerDialogInterface
import io.zhanjiashu.library.provider.*


class RegionPicker private constructor(context: Context, private val picker: MultistagePickerDialog) : PickerDialogInterface by picker, PickerConfigInterface by picker {

    constructor(context: Context) : this(context, MultistagePickerDialog(context, AddressPickerDataProvider(context)))

    private var addressPickSuccessListener: ((province: String, city: String, district: String) -> Unit)? = null

    init {
        setTitle("所在地区")
        picker.setOnPickCompletedListener { selectedOptions ->
            addressPickSuccessListener?.invoke(
                    selectedOptions[STAGE_KEY_PROVINCE] ?: "",
                    selectedOptions[STAGE_KEY_CITY] ?: "",
                    selectedOptions[STAGE_KEY_DISTRICT] ?: ""
            )
        }
    }

    fun setDefaultRegions(province: String?, city: String?, district: String?) {
        val options = mutableMapOf<String, String>()
        province?.let { options[STAGE_KEY_PROVINCE] = it }
        city?.let { options[STAGE_KEY_CITY] = it }
        district?.let { options[STAGE_KEY_DISTRICT] = it }
        picker.setPreselectedOptions(options)
    }

    fun setOnAddressPickSuccessListener(l: (province: String, city: String, district: String) -> Unit) {
        addressPickSuccessListener = l
    }
}
