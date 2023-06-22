package com.kosoku.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.kosoku.demo.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = AppFragmentFactory()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.buttonOne.setOnClickListener {
            CustomNavigationFragment.getModalInstance(DestinationFragment.getInstance(1)).show(supportFragmentManager, UUID.randomUUID().toString())
        }

        binding.buttonTwo.setOnClickListener {
            CustomNavigationFragment.getModalInstance(
                listOf(
                    DestinationFragment.getInstance(1),
                    DestinationFragment.getInstance(2)
                )
            ).show(supportFragmentManager, UUID.randomUUID().toString())
        }

        binding.buttonThree.setOnClickListener {
            CustomNavigationFragment.getModalInstance(
                listOf(
                    DestinationFragment.getInstance(1),
                    DestinationFragment.getInstance(2),
                    DestinationFragment.getInstance(3)
                )
            ).show(supportFragmentManager, UUID.randomUUID().toString())
        }
    }
}