package com.kosoku.kirby.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class ModalNavigationFragmentFactory constructor(
    private val rootFragment: KBYFragment
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ModalNavigationFragment::class.java.name -> ModalNavigationFragment(rootFragment)
            else -> super.instantiate(classLoader, className)
        }
    }
}