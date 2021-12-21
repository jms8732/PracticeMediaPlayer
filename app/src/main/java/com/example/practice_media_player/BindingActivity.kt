package com.example.practice_media_player

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BindingActivity <T : ViewDataBinding> : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutIds() : Int

    protected val binding : T get() = _binding!!
    private var _binding : T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<T>(this,getLayoutIds()).apply {
            _binding = this
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}