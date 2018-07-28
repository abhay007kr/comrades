package com.macbitsgoa.comrades.profileFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.macbitsgoa.comrades.R;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author aayush singla
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {
    private final ArrayList<String> items = new ArrayList<>();

    ProfileAdapter() {
        items.add("My Courses");
        items.add("Ongoing Downloads");
        items.add("Ongoing Uploads");
        items.add("Notification Settings");
        items.add("Eateries");
        items.add("Utilities");
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.vh_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        holder.tvProfile.setText(items.get(position));
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}
