package com.nbt.hudat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nbt.hudat.tools.ChatRecyclerAdapter;

/**
 * Created by user on 08-Aug-17.
 */

public class Tab2Chats extends Fragment {
    FragmentPagerAdapter adapterViewPager;
    ChatRecyclerAdapter ChatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2chats, container, false);
        ViewPager vpPager = (ViewPager) rootView.findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).doneIntialLoadg = true;
    }

    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("Tab2", "is destroyed");
    }


    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;
        private Outgoing outgoing;
        private Incoming incoming;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    if(outgoing == null) {
                        Log.i("detected", "outgoing is null");
                        outgoing = new Outgoing();
                        outgoing.setRetainInstance(true);
                    }
                    return outgoing;
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    if(incoming == null) {
                        incoming = new Incoming();
                        incoming.setRetainInstance(true);
                    }
                    return incoming;
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "Outgoing";
            else
                return "Incoming";
        }

    }
}
