package com.kosoku.demo

import com.kosoku.kirby.fragment.KBYFragment
import com.kosoku.kirby.fragment.NavigationFragment
import timber.log.Timber

class TestNavigationFragment : NavigationFragment() {
    var data: String? = null
        set(value) {
            field = value
            Timber.d("TEST: value updated $value")
        }

    companion object {
        fun getModalInstance(rootFragment: KBYFragment): TestNavigationFragment {
            return TestNavigationFragment().getModalInstance(rootFragment) as TestNavigationFragment
        }

        fun getInstance(rootFragment: KBYFragment): TestNavigationFragment {
            return TestNavigationFragment().getInstance(rootFragment) as TestNavigationFragment
        }
    }
}