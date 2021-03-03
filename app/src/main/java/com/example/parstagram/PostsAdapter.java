package com.example.parstagram;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.List;

import static com.example.parstagram.RelativeTimeAgo.getRelativeTimeAgo;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> postList) {
        posts.addAll(postList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfilePicture;
        private TextView tvUsername;
        private ImageView ivImage;
        private ImageButton imgBtnLike;
        private ImageButton imgBtnComment;
        private ImageButton imgBtnDirect;
        private TextView tvLikes;
        private TextView tvUsernameDescription;
        private TextView tvDescription;
        private TextView tvCreatedAt;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage);
            imgBtnLike = itemView.findViewById(R.id.imgBtnLike);
            imgBtnComment = itemView.findViewById(R.id.imgBtnComment);
            imgBtnDirect = itemView.findViewById(R.id.imgBtnDirect);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvUsernameDescription = itemView.findViewById(R.id.tvUsernameDescription);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(Post post) {
            // Bind the post data to the view elements

            tvUsername.setText(post.getUser().getUsername());
            tvUsernameDescription.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            /*
            String time = post.getCreatedAt().toString();
            time = getRelativeTimeAgo(time) + " ago";
            tvCreatedAt.setText(time);
             */

            ParseFile userProfilePic = post.getUser().getParseFile("profilePicture");
            if(userProfilePic != null)
                Glide.with(context).load(userProfilePic.getUrl())
                        .placeholder(R.drawable.defaultavatar)
                        .into(ivProfilePicture);

            ParseFile image = post.getImage();
            if (image != null)
            {
                Glide.with(context).load(post.getImage().getUrl()).into(ivImage);
            }

            if(post.getLikeStatus() == false){
                imgBtnLike.setBackgroundResource(R.drawable.ufi_heart);
            }
            imgBtnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgBtnLike.setBackgroundResource(R.drawable.ufi_heart_icon);
                    post.setLike();
                    Log.i("imgBtnLikeClick", "clicked");
                    tvLikes.setText(1 + " likes");
                }
            });

        }
    }
}
