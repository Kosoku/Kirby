package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Subclass of [NavHostFragment] that provides convenience methods for observing the current
 * fragment and its position on the backstack
 */
class KBYNavHostFragment : NavHostFragment() {
    private val currentFragment = BehaviorSubject.create<KBYFragment>()
    private val currentPosition = BehaviorSubject.createDefault(0)

    /**
     * Method for getting an RxObservable of the current fragment and its position
     *
     * @return Pair<Int, KBYFragment> where pair.first is the position and pair.second is the fragment
     */
    fun observeCurrentFragmentWithPosition(): Observable<Pair<Int, KBYFragment>> {
        return Observables.zip(currentPosition, currentFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.addOnBackStackChangedListener {
            childFragmentManager.fragments.lastOrNull()?.let {
                postCurrentFragment(it)
            }
            val stackCount = childFragmentManager.backStackEntryCount
            currentPosition.onNext(stackCount)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            postCurrentFragment(fragment)
        }
    }

    private fun postCurrentFragment(fragment: Fragment) {
        if (fragment == currentFragment.value) { return }
        if (fragment is KBYFragment) {
            currentFragment.onNext(fragment)
        }
    }
}