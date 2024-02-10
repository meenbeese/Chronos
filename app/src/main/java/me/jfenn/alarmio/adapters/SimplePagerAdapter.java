package me.jfenn.alarmio.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.Objects;

import me.jfenn.alarmio.fragments.BasePagerFragment;
import me.jfenn.alarmio.interfaces.FragmentInstantiator;


public class SimplePagerAdapter extends FragmentStatePagerAdapter {

    private final FragmentInstantiator[] fragments;

    public SimplePagerAdapter(Context context, FragmentManager fm, FragmentInstantiator... fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public BasePagerFragment getItem(int position) {
        return Objects.requireNonNull(fragments[position].newInstance(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments[position].getTitle(position);
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
