package com.kosoku.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textview.MaterialTextView
import com.kosoku.kirby.extension.setDebounceOnClickListener
import com.kosoku.kirby.fragment.ModalNavigationFragment
import com.kosoku.kirby.fragment.ModalNavigationFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = ModalNavigationFragmentFactory(FragmentA())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.text_view).setDebounceOnClickListener {
            val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, ModalNavigationFragment::class.java.name)
            (fragment as? DialogFragment)?.show(supportFragmentManager, UUID.randomUUID().toString())
        }
    }
}