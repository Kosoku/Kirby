package com.kosoku.kirby.extension

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.kosoku.kirby.fragment.KBYFragment
import com.kosoku.kirby.fragment.NavigationFragment
import timber.log.Timber

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

/**
 * Extension on [DialogFragment] to recursively dismiss the current fragment and its presenters
 */
fun DialogFragment.dismissRecursively() {
    val fragmentsToDismiss: MutableList<DialogFragment> = mutableListOf(this)
    var parent: DialogFragment? = this.presentingFragment() as? DialogFragment

    while (parent != null) {
        if ((parent as? KBYFragment<*>)?.isModal == true || (parent.parentFragment as? NavigationFragment)?.isModal == true) {
            fragmentsToDismiss.add(parent)
        }

        val presenter = parent.presentingFragment()
        if ((presenter as? KBYFragment<*>)?.isModal == true || (presenter?.parentFragment as? NavigationFragment)?.isModal == true) {
            parent = if (presenter is NavigationFragment && presenter.isModal) {
                presenter
            } else if (presenter is KBYFragment<*> && presenter.isModal) {
                presenter
            } else {
                presenter.parentFragment as? DialogFragment
            }
            if (parent != null) {
                fragmentsToDismiss.add(parent)
            }
        } else {
            parent = null
        }
    }

    fragmentsToDismiss.forEach { it.dismiss() }
}
