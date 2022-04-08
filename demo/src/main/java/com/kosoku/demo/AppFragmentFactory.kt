package com.kosoku.demo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class AppFragmentFactory : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            FragmentA::class.java.name -> FragmentA()
            FragmentB::class.java.name -> FragmentB()
            FragmentC::class.java.name -> FragmentC()
            else -> super.instantiate(classLoader, className)
        }
    }
}