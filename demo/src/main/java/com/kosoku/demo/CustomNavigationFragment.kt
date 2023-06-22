package com.kosoku.demo

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.kosoku.kirby.fragment.KBYFragment
import com.kosoku.kirby.fragment.NavigationFragment
import timber.log.Timber

class CustomNavigationFragment : NavigationFragment() {
    private var passedFragments: List<KBYFragment<*>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            args.getStringArrayList(CLASS_NAMES_LIST_KEY)?.let { fragmentClassNames ->
                try {
                    val fragmentList = mutableListOf<KBYFragment<*>>()
                    fragmentClassNames.forEachIndexed { index, name ->
                        (parentFragmentManager.fragmentFactory.instantiate(ClassLoader.getSystemClassLoader(), name) as? KBYFragment<*>)?.let {
                            it.arguments = arguments?.getBundle(index.toString())
                            fragmentList.add(it)
                        }
                    }
                    passedFragments = fragmentList
                } catch (e: Exception) {
                    dismiss()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (passedFragments.isNotEmpty()) {
            fragments = passedFragments
        }
    }

    companion object {
        private const val CLASS_NAMES_LIST_KEY = "classNamesListKey"

        fun getModalInstance(rootFragment: KBYFragment<*>): CustomNavigationFragment {
            return CustomNavigationFragment().getModalInstance(rootFragment) as CustomNavigationFragment
        }

        fun getModalInstance(navHistory: List<KBYFragment<*>>): CustomNavigationFragment {
            val bundles = bundleOf(CLASS_NAMES_LIST_KEY to navHistory.map { it.javaClass.name })
            navHistory.forEachIndexed { index, fragment ->
                bundles.putBundle(index.toString(), fragment.arguments)
            }
            return CustomNavigationFragment().getModalInstance(
                null,
                bundles
            ) as CustomNavigationFragment
        }
    }
}