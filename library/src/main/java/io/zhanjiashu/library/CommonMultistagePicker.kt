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
import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import io.zhanjiashu.library.internal.MultistagePickerInterface
import io.zhanjiashu.library.provider.MultistagePickerDataProvider
import io.zhanjiashu.library.internal.OptionAdapter
import io.zhanjiashu.library.internal.scrollToPositionAtTop

class CommonMultistagePicker(context: Context) : MultistagePickerInterface {

    private val pickerView: View = View.inflate(context, R.layout.multistage_picker_view, null)

    private val recyclerView: RecyclerView
    private val tabLayout: TabLayout
    private val okBtn: View

    private val optionAdapter: OptionAdapter

    private lateinit var dataProvider:  MultistagePickerDataProvider
    private var preSelectedOptions: Map<String, String>? = null
    private var completedListener: ((selectedOptions: Map<String, String>) -> Unit)? = null

    private var curStagePosition = 0
    private val curStageKey: String
        get() = dataProvider.stageKeys()[curStagePosition]
    private val isLowestStage: Boolean
        get() = curStagePosition == stageCount - 1
    private val stageCount: Int
        get() = dataProvider.stageKeys().size

    private val mSelectedOptions = mutableMapOf<String, String>()   // 已选值

    private val tabSelectedListener: TabLayout.OnTabSelectedListener

    init {
        recyclerView = pickerView.findViewById(R.id.rcv)
        tabLayout = pickerView.findViewById(R.id.tab_layout)
        okBtn = pickerView.findViewById(R.id.tv_btn_ok)

        optionAdapter = OptionAdapter()
        optionAdapter.setOnItemViewClickListener { _, _, option ->
            onOptionSelected(option)
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = optionAdapter

        tabSelectedListener = object : TabLayout.OnTabSelectedListener {
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

        }
        tabLayout.addOnTabSelectedListener(tabSelectedListener)

        okBtn.setOnClickListener { notifyPickCompleted() }
    }

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

    private fun preSelect() {
        preSelectedOptions?.let { options ->
            var latestTab: TabLayout.Tab? = null
            (0 until stageCount)
                    .asSequence()
                    .takeWhile {
                        val key = dataProvider.stageKeys()[it]
                        return@takeWhile options.containsKey(key) && optionInTheTargetStage(key, options[key])
                    }
                    .forEach {
                        curStagePosition = it
                        val key = dataProvider.stageKeys()[it]
                        val selectedOption = options[key]!!

                        mSelectedOptions[key] = selectedOption

                        latestTab = getStageTab(it).apply {
                            text = selectedOption
                        }
                    }

            tabLayout.removeOnTabSelectedListener(tabSelectedListener)
            latestTab?.select()
            tabLayout.addOnTabSelectedListener(tabSelectedListener)

            okBtn.isEnabled = isLowestStage

            refreshOptions()
            val latestStageOptions = dataProvider.stageData(curStageKey, mSelectedOptions)
            val targetOptionIndex = latestStageOptions?.indexOf(options[curStageKey]) ?: -1
            recyclerView.scrollToPositionAtTop(targetOptionIndex)

        }

    }

    /**
     * 层级切换事件处理。
     * @param upwards true - 从低层级切换到高层级， false - 从高层级切换到低层级
     */
    private fun onStageSelected(stagePosition: Int, tab: TabLayout.Tab, upwards: Boolean) {
        curStagePosition = stagePosition
        refreshOptions()
        val selectedOption = mSelectedOptions[curStageKey]
        tab.text = selectedOption ?: dataProvider.stageTabText(curStageKey)
        if (upwards) {
            resetLowerStages()
            okBtn.isEnabled = false
            selectedOption?.let {
                val latestStageOptions = dataProvider.stageData(curStageKey, mSelectedOptions)
                recyclerView.scrollToPositionAtTop(latestStageOptions?.indexOf(it) ?: -1)
            }
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
        val nextTab = getStageTab(tabLayout.selectedTabPosition + 1)
        nextTab.select()
    }

    private fun getStageTab(stagePosition: Int): TabLayout.Tab {
        val tab: TabLayout.Tab
        if (stagePosition < tabLayout.tabCount) {
            tab = tabLayout.getTabAt(stagePosition)!!
        } else {
            tab = tabLayout.newTab()
            tabLayout.addTab(tab)
        }
        return tab
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

    private fun optionInTheTargetStage(stageKey: String, selectedOption: String?): Boolean {
        return selectedOption != null && dataProvider.stageData(stageKey, mSelectedOptions)?.contains(selectedOption) ?: false
    }


    /* 对外暴露的方法 */

    override fun setDataProvider(provider: MultistagePickerDataProvider) {
        this.dataProvider = provider

        refreshOptions()
    }

    override fun setSelectedOptions(selectedOptions: Map<String, String>) {
        preSelectedOptions = selectedOptions
        preSelect()
    }

    override fun setOnPickCompletedListener(l: (selectedOptions: Map<String, String>) -> Unit) {
        completedListener = l
    }

    fun getView(): View {
        return pickerView
    }

    fun bindToLayout(layout: ViewGroup) {
        layout.addView(pickerView)
    }
}