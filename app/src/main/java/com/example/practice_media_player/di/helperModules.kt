package com.example.practice_media_player.di

import com.example.practice_media_player.helper.MusicServiceHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val helperModules = module {
    single {
        MusicServiceHelper(androidContext())
    }
}