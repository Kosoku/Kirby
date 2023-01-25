package com.kosoku.demo

import android.os.Bundle
import androidx.core.os.bundleOf
import com.kosoku.kirby.fragment.KBYFragment
import com.kosoku.kirby.fragment.NavigationFragment
import timber.log.Timber

class CustomNavigationFragment : NavigationFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("DEBUG: passed to nav controller: ${arguments?.getString(PASSED_STRING_KEY)}")
    }

    companion object {
        private const val PASSED_STRING_KEY = "PASSED_STRING_KEY.CustomNavigationFragment"

        fun getModalInstance(rootFragment: KBYFragment, passedString: String): CustomNavigationFragment {
            return CustomNavigationFragment().getModalInstance(rootFragment, bundleOf(PASSED_STRING_KEY to passedString)) as CustomNavigationFragment
        }
    }
}