package com.bcgroup.social_media.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bcgroup.social_media.fragments.PostFragment
import com.bcgroup.social_media.fragments.SavedFragment

class ProfileTabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->{
                PostFragment()
            }
            1->{
                SavedFragment()
            }
            else->{
                Fragment()
            }
        }
    }
}