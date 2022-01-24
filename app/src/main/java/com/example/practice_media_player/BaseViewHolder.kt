package com.example.practice_media_player

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T: ViewDataBinding>(view : View) : RecyclerView.ViewHolder(view) {
    protected val binding : T = DataBindingUtil.bind(view)!!
}