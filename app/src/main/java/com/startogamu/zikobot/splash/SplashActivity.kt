package com.startogamu.zikobot.splash

import com.joxad.easydatabinding.activity.ActivityBase
import com.startogamu.zikobot.R
import com.startogamu.zikobot.BR
import com.startogamu.zikobot.databinding.SplashActivityBinding

import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Generated by generator-android-template
 */
class SplashActivity : ActivityBase<SplashActivityBinding, SplashActivityVM>() {
    override fun data(): Int {
        return BR.splashActivityVM
    }

    override fun layoutResources(): Int {
        return R.layout.splash_activity
    }

    override fun baseActivityVM(binding: SplashActivityBinding?, savedInstanceState: Bundle?): SplashActivityVM {
        return SplashActivityVM(this, binding, savedInstanceState)
    }

    companion object {

        /***
         * Generate the intent for the activity
         * Use this to start the activity
         */
        fun newInstance(context: Context): Intent {
            return Intent(context, SplashActivity::class.java)
        }
    }
}