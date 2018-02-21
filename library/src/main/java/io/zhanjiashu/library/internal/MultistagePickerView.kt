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


package io.zhanjiashu.library.internal

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import io.zhanjiashu.library.R
import io.zhanjiashu.library.adapter.MultistagePickerDataProvider

internal class MultistagePickerView(context: Context) {

    val view: View = View.inflate(context, R.layout.multistage_picker_view, null)

    private val recyclerView: RecyclerView
    private val tabLayout: TabLayout
    private val okBtn: View

    private val optionAdapter: OptionAdapter

    private lateinit var dataProvider:  MultistagePickerDataProvider
    internal var completedListener: ((selectedOptions: Map<String, String>) -> Unit)? = null

    private var curStagePosition = 0
    private val curStageKey: String
        get() = dataProvider.stageKeys()[curStagePosition]
    private val isLowestStage: Boolean
        get() = curStagePosition == stageCount - 1
    private val stageCount: Int
        get() = dataProvider.stageKeys().size

    private val mSelectedOptions = mutableMapOf<String, String>()   // 已选值

    init {
        recyclerView = view.findViewById(R.id.rcv)
        tabLayout = view.findViewById(R.id.tab_layout)
        okBtn = view.findViewById(R.id.tv_btn_ok)

        optionAdapter = OptionAdapter()
        optionAdapter.setOnItemViewClickListener { _, _, option ->
            onOptionSelected(option)
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = optionAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tabLayout.selectedTabPosition
                if (position != curStagePosition) {
                    onStageSelected(position, tab, position < curStagePosition)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })

        okBtn.setOnClickListener { notifyPickCompleted() }
    }

    fun setDataProvider(provider: MultistagePickerDataProvider) {
        this.dataProvider = provider

        refreshOptions()
    }

//    fun setOnPickCompletedListener(l: (selectedOptions: Map<String, String>) -> Unit) {
//        this.completedListener = l
//    }

    private fun refreshOptions() {
        val options = dataProvider.stageData(curStageKey, mSelectedOptions)

        optionAdapter.apply {
            data.clear()
            options?.let {
                data.addAll(it)
            }
            notifyDataSetChanged()
        }

    }

    /**
     * 层级切换事件处理。
     * @param upwards true - 从低层级切换到高层级， false - 从高层级切换到低层级
     */
    private fun onStageSelected(stagePosition: Int, tab: TabLayout.Tab, upwards: Boolean) {
        curStagePosition = stagePosition
        refreshOptions()
        val selectedOptionOfCurStage = mSelectedOptions[curStageKey]
        tab.text = selectedOptionOfCurStage ?: dataProvider.stageTabText(curStageKey)
        if (upwards) {
            resetLowerStages()
            okBtn.isEnabled = false
        } else {
            recyclerView.scrollToPosition(0)
        }
    }

    // 当前层级选中事件处理
    private fun onOptionSelected(option: String) {
        tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text = option
        mSelectedOptions[curStageKey] = option
        if (isLowestStage) {
            okBtn.isEnabled = true
        } else {
            jumpToNextStage()
        }
    }

    // 跳至下一层级，如果该选择器尚未存在，则创建并渲染；若已存在，则渲染
    private fun jumpToNextStage() {
        val nextTabItem: TabLayout.Tab
        if (tabLayout.selectedTabPosition < tabLayout.tabCount - 1) {
            nextTabItem = tabLayout.getTabAt(tabLayout.selectedTabPosition + 1)!!
            nextTabItem.select()
        } else {
            nextTabItem = tabLayout.newTab()
            tabLayout.addTab(nextTabItem, true)
        }
    }

    // 重置低层级选择器
    private fun resetLowerStages() {
        (curStagePosition + 1 until stageCount).forEach {
            mSelectedOptions.remove(dataProvider.stageKeys()[it])
            tabLayout.getTabAt(it)?.text = dataProvider.stageTabText(dataProvider.stageKeys()[it])
        }
    }

    private fun notifyPickCompleted() {
        completedListener?.invoke(mSelectedOptions)
    }
}