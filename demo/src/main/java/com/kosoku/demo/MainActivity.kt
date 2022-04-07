package com.kosoku.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.kosoku.kirby.extension.setDebounceOnClickListener
import com.kosoku.kirby.fragment.ModalNavigationFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = AppFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.text_view).setDebounceOnClickListener {
            ModalNavigationFragment.getInstance(FragmentA()).show(supportFragmentManager, UUID.randomUUID().toString())
//            FragmentA.getModalInstance().show(supportFragmentManager, UUID.randomUUID().toString())
        }
    }
}