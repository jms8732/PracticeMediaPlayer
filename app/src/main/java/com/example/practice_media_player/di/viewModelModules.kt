package com.example.practice_media_player.di

import com.example.practice_media_player.viewModel.MusicListActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module{
    viewModel{ MusicListActivityViewModel()}
}