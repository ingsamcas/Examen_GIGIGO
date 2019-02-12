package com.gigigo.examen

import android.app.Application
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.utils.StorageUtils

class GigigoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val cacheDir = StorageUtils.getOwnCacheDirectory(
                getApplicationContext(), this.getApplicationInfo().dataDir + "cache_images")

        val options = DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true).build()

        val config = ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(options)
                .diskCache(LimitedAgeDiskCache(cacheDir, 100))
                .build()

        ImageLoader.getInstance().init(config)
    }
}
