package com.kosoku.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.kosoku.demo.databinding.FragmentABinding
import com.kosoku.kirby.extension.setDebounceOnClickListener
import com.kosoku.kirby.fragment.KBYFragment
import timber.log.Timber

class FragmentA : KBYFragment() {
    private var passedValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        passedValue = arguments?.getString(PASSED_STRING_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_a, container, false)

        (binding as? FragmentABinding)?.textView?.text = passedValue ?: "NO VALUE"
        (binding as? FragmentABinding)?.textView?.setDebounceOnClickListener {
            navigationController?.get()?.let { navController ->
                (navController as? TestNavigationFragment)?.data = "Data from Fragment A"
                navController.pushFragment(FragmentB.getInstance("Fragment B"))
            }
        }

        return binding?.root
    }

    companion object {
        private const val PASSED_STRING_KEY = "PASSED_STRING_KEY"
        fun getModalInstance(passedString: String): FragmentA {
            val bundle = bundleOf(PASSED_STRING_KEY to passedString)
            return FragmentA().getModalInstance(bundle) as FragmentA
        }
        fun getInstance(passedString: String): FragmentA {
            val bundle = bundleOf(PASSED_STRING_KEY to passedString)
            return FragmentA().getInstance(bundle) as FragmentA
        }
    }
}