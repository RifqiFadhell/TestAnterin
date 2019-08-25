package id.fadhell.testanterin.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity: AppCompatActivity() {

    protected abstract fun init(bundle: Bundle?)

    protected abstract fun initData(bundle: Bundle?)

    protected abstract fun initListener(bundle: Bundle?)
}