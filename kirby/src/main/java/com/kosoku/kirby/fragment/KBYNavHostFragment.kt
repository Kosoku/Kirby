package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Subclass of [NavHostFragment] that provides convenience methods for observing the current
 * fragment and its position on the backstack
 */
class KBYNavHostFragment : NavHostFragment() {
    /**
     * Data class model representing a [KBYFragment] and its position in the backstack
     */
    data class FragmentWithPosition(
        val fragment: KBYFragment<*>,
        val position: Int
    )

    private val currentFragment = BehaviorSubject.create<KBYFragment<*>>()
    private val currentPosition = BehaviorSubject.createDefault(-1)
    private val _currentFragment: MutableLiveData<KBYFragment<*>> by lazy { MutableLiveData() }
    private val _currentPosition: MutableLiveData<Int> by lazy { MutableLiveData(-1) }

    /**
     * A [LiveData<FragmentWithPosition>] property for observing changes in the top-level fragment
     * and its position on the backstack
     */
    val currentFragmentWithPosition: LiveData<FragmentWithPosition>
        get() {

            val retval = MediatorLiveData<FragmentWithPosition>()

            retval.addSource(_currentFragment) {
                retval.postValue(combineFragmentAndPosition(_currentFragment, _currentPosition))
            }
            retval.addSource(_currentPosition) {
                retval.postValue(combineFragmentAndPosition(_currentFragment, _currentPosition))
            }

            return retval
        }

    /**
     * Method for getting an RxObservable of the current fragment and its position
     *
     * @return Pair<Int, KBYFragment> where pair.first is the position and pair.second is the fragment
     */
    @Deprecated("This method will be moved to KirbyRxExtensions library in the future. " +
            "Consider using [currentFragmentWithPosition] [LiveData] object instead.")
    fun observeCurrentFragmentWithPosition(): Observable<Pair<Int, KBYFragment<*>>> {
        return Observables.zip(currentPosition, currentFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.addOnBackStackChangedListener {
            childFragmentManager.fragments.lastOrNull()?.let {
                postCurrentFragment(it)
            }
            val stackCount = childFragmentManager.backStackEntryCount - 1
            currentPosition.onNext(stackCount)
            _currentPosition.postValue(stackCount)
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
        if (fragment is KBYFragment<*>) {
            currentFragment.onNext(fragment)
            _currentFragment.postValue(fragment)
        }
    }

    private fun combineFragmentAndPosition(fragment: LiveData<KBYFragment<*>>, position: LiveData<Int>): FragmentWithPosition? {
        val fragmentValue = fragment.value
        val positionValue = position.value

        return if (fragmentValue == null || positionValue == null) {
            null
        } else {
            FragmentWithPosition(fragmentValue, positionValue)
        }
    }
}