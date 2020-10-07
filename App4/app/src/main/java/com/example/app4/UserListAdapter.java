package com.example.app4;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyViewHolder> {
    private ArrayList<User> mDataset;
//    public static InteractWithRecyclerView interact;

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserListAdapter(ArrayList<User> myDataset, Context ctx) {
        mDataset = myDataset;
//        interact = (InteractWithRecyclerView) ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_info_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        User user = mDataset.get(position);

        holder.age.setText("("+user.age+")");
        holder.email.setText(user.email);
        holder.name.setText(user.fname+" "+user.lname);

        if(user.gender.equals("Female")||user.gender.equals("female")){
            holder.avatarImage.setImageResource(R.drawable.female);
        }else{
            holder.avatarImage.setImageResource(R.drawable.male);
        }

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Demo","Selected Position is :" + mDataset.get(position));
//                interact.getDetails(mDataset.get(position));
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        ImageView avatarImage;
        TextView name, age, email;
        ConstraintLayout constraintLayout;
        public MyViewHolder(View view) {
            super(view);
            avatarImage = view.findViewById(R.id.userImage);
            name = view.findViewById(R.id.nameUsersInfo);
            age = view.findViewById(R.id.ageUsersInfo);
            email = view.findViewById(R.id.emailUsersInfo);
            constraintLayout = view.findViewById(R.id.constraintLayout);
        }

    }

//    public interface InteractWithRecyclerView{
//        public void selectedItem(Emails emailObject);
//        public void getDetails(Emails emailObject);
//    }

}
