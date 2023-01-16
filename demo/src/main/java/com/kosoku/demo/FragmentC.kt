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
import java.lang.ref.WeakReference

class FragmentC : KBYFragment() {
    private var passedValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        passedValue = arguments?.getString(PASSED_STRING_KEY)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("DEBUG: Fragment C onStart called")
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
                navController.fragments = listOf(
                    FragmentA.getInstance("Fragment A"),
                    FragmentB.getInstance("Fragment B"),
                )
            }
        }

        return binding?.root
    }

    override fun wilNavigateBack(closure: (() -> Unit)?) {
        Timber.d("TEST: navigating back from fragment C")
        super.wilDismiss(closure)
    }

    companion object {
        private const val PASSED_STRING_KEY = "PASSED_STRING_KEY"
        fun getModalInstance(passedString: String): FragmentC {
            val bundle = bundleOf(PASSED_STRING_KEY to passedString)
            return FragmentC().getModalInstance(bundle) as FragmentC
        }
        fun getInstance(passedString: String): FragmentC {
            val bundle = bundleOf(PASSED_STRING_KEY to passedString)
            return FragmentC().getInstance(bundle) as FragmentC
        }
    }
}