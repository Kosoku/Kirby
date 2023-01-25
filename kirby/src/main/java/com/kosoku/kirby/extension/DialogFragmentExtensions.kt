package com.kosoku.kirby.extension

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.kosoku.kirby.fragment.NavigationFragment

/**
 * Extension on [DialogFragment] to get the fragment it was presented from
 */
fun DialogFragment.presentingFragment(): Fragment? {
    var retval = this.parentFragment
    return if (retval == null) {
        val lastIndex = activity?.supportFragmentManager?.fragments?.lastIndex ?: 0
        retval = if (lastIndex == 0) {
            activity?.supportFragmentManager?.fragments?.lastOrNull()
        } else {
            activity?.supportFragmentManager?.fragments?.get(lastIndex-1)
        }
        if (retval is NavigationFragment) {
            if (retval.childFragmentManager.fragments.isNotEmpty()) {
                retval.childFragmentManager.fragments.lastOrNull()
            } else {
                retval
            }
        } else {
            retval
        }
    } else {
        if (retval is NavigationFragment && !retval.isModal) {
            retval.presentingFragment()
        } else {
            retval
        }
    }
}
