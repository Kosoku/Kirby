package com.kosoku.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.kosoku.demo.databinding.FragmentDestinationBinding
import com.kosoku.kirby.fragment.KBYFragment

class DestinationFragment : KBYFragment<FragmentDestinationBinding>() {
    private var position: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDestinationBinding.inflate(inflater, container, false)

        position = arguments?.getInt(POSITION_KEY) ?: -1

        binding?.label?.text = "Destination ${position}"

        binding?.nextButton?.apply {
            this.isEnabled = position < 3
            this.setOnClickListener {
                navigationController?.get()?.pushFragment(DestinationFragment.getInstance(position + 1))
            }
        }
        binding?.prevButton?.apply {
            this.isEnabled = position > 1
            this.setOnClickListener {
                navigationController?.get()?.pop()
            }
        }

        return binding?.root
    }

    companion object {
        private const val POSITION_KEY = "positionKey"
        fun getInstance(position: Int): DestinationFragment {
            return DestinationFragment().getInstance(
                bundleOf(
                POSITION_KEY to position
            )
            ) as DestinationFragment
        }
    }
}