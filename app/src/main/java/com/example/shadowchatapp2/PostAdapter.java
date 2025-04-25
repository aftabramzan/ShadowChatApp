package com.example.shadowchatapp2;

import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private OnPostInteractionListener listener;


    public interface OnPostInteractionListener {
        void onLikeClick(Post post, int position);

        void onCommentClick(Post post, int position);

        void onShareClick(Post post, int position);

        void onBookmarkClick(Post post, int position);

        void onProfileClick(Post post, int position);


    }

    public PostAdapter(Context context, List<Post> posts, OnPostInteractionListener listener) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // Set user info
        holder.usernameText.setText(post.getUsername());
        if (post.getUserProfilePic() != null && !post.getUserProfilePic().isEmpty()) {
            Glide.with(context)
                    .load(post.getUserProfilePic())
                    .placeholder(R.drawable.profile_placeholder)
                    .into(holder.profilePic);
        } else {
            holder.profilePic.setImageResource(R.drawable.profile_placeholder);
        }

        // Set location
        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            holder.locationLayout.setVisibility(View.VISIBLE);
            holder.locationText.setText(post.getLocation());
        } else {
            holder.locationLayout.setVisibility(View.GONE);
        }

        // Set post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Set post content
        holder.captionText.setText(post.getContent());

        // Set likes count
        holder.likesCountText.setText(formatCount(post.getLikesCount()) + " likes");

        // Set comments count
        holder.viewCommentsText.setText("View all " + post.getCommentsCount() + " comments");

        // Set timestamp
        holder.timestampText.setText(getTimeAgo(post.getTimestamp()));

        // Set like button state
        holder.likeButton.setImageResource(post.isLiked() ? R.drawable.like : R.drawable.like);

        // Set bookmark button state
        holder.bookmarkButton.setImageResource(post.isBookmarked() ? R.drawable.bookmark : R.drawable.bookmark);

        // Set click listeners
        holder.likeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(post, position);
            }
        });

        holder.commentButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(post, position);
            }
        });

        holder.shareButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareClick(post, position);
            }
        });

        holder.bookmarkButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookmarkClick(post, position);
            }
        });

        holder.profilePic.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClick(post, position);
            }
        });

        holder.usernameText.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClick(post, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = posts != null ? posts.size() : 0;
        Log.d("PostAdapter", "getItemCount called, returning: " + count);
        return count;
    }

    public void updatePosts(List<Post> newPosts) {
        Log.d("PostAdapter", "Updating posts, new size: " + (newPosts != null ? newPosts.size() : 0));
        if (newPosts != null) {
            this.posts = new ArrayList<>(newPosts);
            Log.d("PostAdapter", "Posts updated, current size: " + this.posts.size());
            notifyDataSetChanged();
        }
    }

    public void updateSinglePost(Post post, int position) {
        Log.d("PostAdapter", "Updating single post at position: " + position);
        if (position >= 0 && position < posts.size()) {
            posts.set(position, post);
            notifyItemChanged(position);
            Log.d("PostAdapter", "Single post updated successfully");
        } else {
            Log.e("PostAdapter", "Invalid position for update: " + position);
        }
    }

    private String formatCount(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 1000000) {
            return String.format(Locale.US, "%.1fK", count / 1000.0);
        } else {
            return String.format(Locale.US, "%.1fM", count / 1000000.0);
        }
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) {
            return "Just now";
        } else if (diff < 60 * 60 * 1000) {
            int minutes = (int) (diff / (60 * 1000));
            return minutes + "m ago";
        } else if (diff < 24 * 60 * 60 * 1000) {
            int hours = (int) (diff / (60 * 60 * 1000));
            return hours + "h ago";
        } else if (diff < 7 * 24 * 60 * 60 * 1000) {
            int days = (int) (diff / (24 * 60 * 60 * 1000));
            return days + "d ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.US);
            return sdf.format(new Date(timestamp));
        }
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView usernameText;
        View locationLayout;
        TextView locationText;
        ImageView postImage;
        ImageButton likeButton;
        ImageButton commentButton;
        ImageButton shareButton;
        ImageButton bookmarkButton;
        TextView likesCountText;
        TextView captionText;
        TextView viewCommentsText;
        TextView timestampText;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.iv_profile_pic);
            usernameText = itemView.findViewById(R.id.tv_username);
            locationLayout = itemView.findViewById(R.id.layout_location);
            locationText = itemView.findViewById(R.id.tv_location);
            postImage = itemView.findViewById(R.id.iv_post_image);
            likeButton = itemView.findViewById(R.id.btn_like);
            commentButton = itemView.findViewById(R.id.btn_comment);
            shareButton = itemView.findViewById(R.id.btn_share);
            bookmarkButton = itemView.findViewById(R.id.btn_bookmark);
            likesCountText = itemView.findViewById(R.id.tv_likes_count);
            captionText = itemView.findViewById(R.id.tv_caption);
            viewCommentsText = itemView.findViewById(R.id.tv_view_comments);
            timestampText = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
