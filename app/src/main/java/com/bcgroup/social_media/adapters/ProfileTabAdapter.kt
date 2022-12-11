package com.bcgroup.social_media.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ProfileTabAdapter : FragmentPagerAdapter {
    var f_list = ArrayList<Fragment>()
    var f_name_list = ArrayList<String>()

    constructor(fm: FragmentManager) : super(fm)
    override fun getCount(): Int {
        return f_list.size
    }

    override fun getItem(position: Int): Fragment {
        return f_list[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return f_name_list[position]
    }
    fun addFragment(fragment: Fragment,title:String){
        f_list.add(fragment)
        f_name_list.add(title)
    }
}