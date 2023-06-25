/**
    ******************************************************************************
    * @file     BaseFragment.java
    * @brief    This file contains a base class for fragments
    ******************************************************************************
    */

package com.smoke.detection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;


/**
    ******************************************************************************
    * @defgroup    baseFragment BaseFragment class
    * @brief       An abstract class to work with binding
    ******************************************************************************
    */

abstract class BaseFragment<VB extends ViewBinding> extends Fragment {
    protected VB binding;   ///<    Binding for simplified work with elements on the fragment


    /**
        ******************************************************************************
        * @brief        Is a rewrite of the mapping creation method
        * @ingroup      baseFragment
        * @param[in]    inflater  - The LayoutInflater class is used to instantiate
        *                           the contents of layout XML files into their
        *                           corresponding View objects.
        * @param[in]    container - The view group is the base class for layouts and
        *                           views containers
        * @param[in]    savedInstanceState - A mapping from String keys to various
        *                                    Parcelable values.
        * @param[out]   binding   - for simplified work with elements on the fragment
        ******************************************************************************
        */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = initViewBinding(inflater, container);
        return binding.getRoot();
    }


    /**
     ******************************************************************************
     * @brief        This overwrites the method of destroying the fragment display
     * @ingroup      baseFragment
     ******************************************************************************
     */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     ******************************************************************************
     * @brief        This overwrites the initialization method of the fragment
     *               display
     * @ingroup      baseFragment
     * @param[in]    inflater  - The LayoutInflater class is used to instantiate
     *                           the contents of layout XML files into their
     *                           corresponding View objects.
     * @param[in]    container - The view group is the base class for layouts and
     *                           views containers
     ******************************************************************************
     */

    abstract VB initViewBinding(LayoutInflater inflater, ViewGroup container);
}
