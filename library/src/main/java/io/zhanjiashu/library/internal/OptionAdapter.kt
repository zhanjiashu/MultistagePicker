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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.zhanjiashu.library.R
import io.zhanjiashu.library.safeCast


internal class OptionAdapter constructor(val data: MutableList<String>) : RecyclerView.Adapter<OptionVH>() {

    constructor() : this(mutableListOf<String>())

    private lateinit var mContext: Context
    private lateinit var mLayoutInflater: LayoutInflater
    private val internalListener = View.OnClickListener {
        it.tag.safeCast<Int> {
            mItemViewClickListener?.invoke(it, this@safeCast, data[this@safeCast])
        }
    }

    private var mItemViewClickListener: ((view: View, position: Int, option: String) -> Unit)? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mContext = recyclerView.context
        mLayoutInflater = LayoutInflater.from(mContext)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionVH {
        val vh = OptionVH(mLayoutInflater.inflate(R.layout.multistage_picker_potion_item_view, parent, false))
        vh.textView.setOnClickListener(internalListener)
        return vh
    }

    override fun onBindViewHolder(vh: OptionVH, position: Int) {
        vh.textView.text = data[position]
        vh.textView.tag = position
    }

    override fun getItemCount(): Int {
        return data.size
    }


    public fun setOnItemViewClickListener(l: (v: View, position: Int, option: String) -> Unit) {
        mItemViewClickListener = l
    }

}

internal class OptionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.optionText)
}