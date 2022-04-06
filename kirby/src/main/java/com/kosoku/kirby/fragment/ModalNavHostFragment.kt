package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ModalNavHostFragment : NavHostFragment() {
    private val currentFragment = BehaviorSubject.create<KBYFragment>()
    private val currentPosition = BehaviorSubject.createDefault(0)

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