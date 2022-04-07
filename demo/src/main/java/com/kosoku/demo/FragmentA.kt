package com.kosoku.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.kosoku.demo.databinding.FragmentABinding
import com.kosoku.kirby.extension.setDebounceOnClickListener
import com.kosoku.kirby.fragment.KBYFragment
import timber.log.Timber

class FragmentA : KBYFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_a, container, false)

        (binding as? FragmentABinding)?.textView?.setDebounceOnClickListener {
            dismiss()
        }

        return binding?.root
    }

    companion object {
        fun getModalInstance() = FragmentA().getModalInstance() as FragmentA
    }
}