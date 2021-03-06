/*
 * Copyright (c) 2015 Vamo Junto. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Vamo Junto
 * ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Vamo Junto.
 *
 * VAMO JUNTO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. VAMO JUNTO SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * See LICENSE.txt
 */

package co.vamojunto.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import co.vamojunto.R;
import co.vamojunto.ui.activities.VamoJuntoActivity;
import co.vamojunto.ui.widget.SlidingTabLayout;

/**
 * Tela de visualização das caronas cadastradas pelo usuário, tanto os pedidos, quanto as ofertas.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 2.0
 */
public class MinhasCaronasFragment extends Fragment {

    private static final String TAG = "MinhasCaronasFragment";

    /**
     * Application Bar instance
     *
     * @since 0.6.0
     */
    private Toolbar mAppBar;

    public MinhasCaronasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_minhas_caronas, container, false);

        // assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        // assigning the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setSelectedIndicatorColors(getResources().getColor(R.color.white));

        // setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.ColorPrimaryDark);
            }
        });

        // setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        setupAppBar();

        return rootView;
    }

    private Toolbar getAppBar() {
        if (mAppBar == null) {
            mAppBar = ((VamoJuntoActivity) getActivity()).getAppBar();
        }

        return mAppBar;
    }

    /**
     * Configure the application bar when this fragment is inflated.
     *
     * @since 0.6.0
     */
    private void setupAppBar() {
        // changes the action bar title
        getAppBar().setTitle(getString(R.string.minhas_caronas_fragment_title));
        getAppBar().getMenu().clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Adapter utilizado para preencher as páginas exibidas em cada uma das abas do Fragment.
     * São exibidas duas páginas, uma contendo as ofertas de caronas do usuário, e outra contendo
     * os pedidos.
     *
     * An Adapter to fill the fragment tabs.
     *
     * Currently it displays the three following pages:
     * <ul>
     *     <li>
     *         A page where the user can view all his ride offers.
     *         The {@link co.vamojunto.ui.fragments.ListMyRidesFragment} is used to render this page.
     *     </li>
     *     <li>
     *         A page where the user can view all rides that he is confirmed as a passenger.
     *         The {@link co.vamojunto.ui.fragments.RidesAsPassengerFragment} is used to render
     *         this page.
     *     </li>
     *     <li>
     *         A page where the user can manage all his ride requests.
     *         The {@link co.vamojunto.ui.fragments.ListMyRequestsFragment} is used to render
     *         this page.
     *     </li>
     * </ul>
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.1.0
     * @version 1.0.0
     */
    public class MyPagerAdapter extends FragmentPagerAdapter {

        /**
         * A tag for log messages.
         *
         * @since 0.1.0
         */
        private static final String TAG = "MyPagerAdapter";

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.ride_offers);
            } else if (position == 1) {
                return getString(R.string.minhas_caronas_fragment_tab3);
            }

            Log.e(TAG, "[getPageTitle] An invalid tab position was given.");
            return "";
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ListMyRidesFragment();
            } else if (position == 1) {
                return new ListMyRequestsFragment();
            }

            Log.e(TAG, "[getItem] An invalid tab position was given.");
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
