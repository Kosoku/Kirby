package com.kosoku.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.kosoku.demo.databinding.ActivityMainBinding
import com.kosoku.kirby.extension.setDebounceOnClickListener
import com.kosoku.kirby.fragment.NavigationFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = AppFragmentFactory()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        val rootFragment = NavigationFragment.getInstance(FragmentA.getInstance("Fragment A"))
//        supportFragmentManager.beginTransaction()
//            .apply {
//                replace(binding.containerView.id, rootFragment)
//                commit()
//            }

        binding.containerView.setDebounceOnClickListener {
            TestNavigationFragment.getModalInstance(FragmentA.getInstance("Fragment A")).show(supportFragmentManager, UUID.randomUUID().toString())
//            FragmentA.getModalInstance("THIS IS ANOTHER TEST").show(supportFragmentManager, UUID.randomUUID().toString())
        }
    }
}