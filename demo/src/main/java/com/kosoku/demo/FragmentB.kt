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
import com.kosoku.kirby.fragment.NavigationFragment
import timber.log.Timber
import java.util.*

class FragmentB : KBYFragment() {
    private var passedValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        passedValue = arguments?.getString(PASSED_STRING_KEY)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("DEBUG: Fragment B onStart called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_a, container, false)

        (binding as? FragmentABinding)?.let { viewBinding ->
            viewBinding.textView.text = passedValue
            viewBinding.textView.setDebounceOnClickListener {
                navigationController?.get()?.pushFragment(FragmentC.getInstance("Fragment C"))
            }
        }

        return binding?.root
    }

    companion object {
        private const val PASSED_STRING_KEY = "PASSED_STRING_KEY"
        fun getModalInstance(passedString: String): FragmentB {
            val bundle = bundleOf(PASSED_STRING_KEY to passedString)
            return FragmentB().getModalInstance(bundle) as FragmentB
        }
        fun getInstance(passedString: String): FragmentB {
            val bundle = bundleOf(PASSED_STRING_KEY to passedString)
            return FragmentB().getInstance(bundle) as FragmentB
        }
    }
}