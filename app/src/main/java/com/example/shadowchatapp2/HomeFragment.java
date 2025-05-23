package com.example.shadowchatapp2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements PostAdapter.OnPostInteractionListener {


    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private Button createFirstPostButton;
    private PostAdapter postAdapter;
    private List<Post> posts;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_posts);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyState = view.findViewById(R.id.empty_state);
        createFirstPostButton = view.findViewById(R.id.btn_create_first_post);

        // Initialize posts list
        posts = new ArrayList<>();

        // Set up RecyclerView with LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up PostAdapter
        postAdapter = new PostAdapter(getContext(), posts, this);
        recyclerView.setAdapter(postAdapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
            }
        });

        // Set up create first post button
        createFirstPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                startActivity(intent);
            }
        });

        // Load posts immediately
        loadPosts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload posts when returning to this fragment
        loadPosts();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Also load posts when the fragment starts
        loadPosts();
    }

    private void loadPosts() {
        try {
            Log.d(TAG, "HomeFragment: Starting to load posts...");

            // Show loading indicator
            if (posts.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "HomeFragment: Showing progress bar");
            }

            // Get posts from GlobalPosts
            List<Post> globalPosts = GlobalPosts.getInstance(requireContext()).getPosts();
            Log.d(TAG, "HomeFragment: Retrieved posts from GlobalPosts. Size: " + (globalPosts != null ? globalPosts.size() : 0));

            // Update the posts list
            posts.clear();
            if (globalPosts != null) {
                posts.addAll(globalPosts);
                Log.d(TAG, "HomeFragment: Added " + globalPosts.size() + " posts to local list");
            }

            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Update adapter
                            if (postAdapter != null) {
                                postAdapter.updatePosts(posts);
                                Log.d(TAG, "HomeFragment: Updated adapter with " + posts.size() + " posts");
                            } else {
                                Log.e(TAG, "HomeFragment: PostAdapter is null");
                            }

                            // Hide loading indicators
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            // Show/hide empty state
                            if (posts.isEmpty()) {
                                Log.d(TAG, "HomeFragment: No posts, showing empty state");
                                if (emptyState != null) {
                                    emptyState.setVisibility(View.VISIBLE);
                                }
                                if (recyclerView != null) {
                                    recyclerView.setVisibility(View.GONE);
                                }
                            } else {
                                Log.d(TAG, "HomeFragment: Posts available, hiding empty state");
                                if (emptyState != null) {
                                    emptyState.setVisibility(View.GONE);
                                }
                                if (recyclerView != null) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "HomeFragment: Error updating UI", e);
                        }
                    }
                });
            } else {
                Log.e(TAG, "HomeFragment: Activity is null, cannot update UI");
            }
        } catch (Exception e) {
            Log.e(TAG, "HomeFragment: Error loading posts", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLikeClick(Post post, int position) {
        // Toggle like state
        post.setLiked(!post.isLiked());

        // Update likes count
        if (post.isLiked()) {
            post.setLikesCount(post.getLikesCount() + 1);
        } else {
            post.setLikesCount(post.getLikesCount() - 1);
        }

        // Update UI
        // Update UI
        if (postAdapter != null) {
            postAdapter.updateSinglePost(post, position);
        }

        // Update in GlobalPosts
        GlobalPosts.getInstance(requireContext()).updatePost(post);
    }

    @Override
    public void onCommentClick(Post post, int position) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra("post_id", post.getId());
        startActivity(intent);
    }


    @Override
    public void onShareClick(Post post, int position) {
        // TODO: Implement share functionality
        Toast.makeText(getContext(), "Share functionality coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookmarkClick(Post post, int position) {
        // Toggle bookmark state
        post.setBookmarked(!post.isBookmarked());

        // Update UI
        if (postAdapter != null) {
            postAdapter.updateSinglePost(post, position);
        }

        // Update in GlobalPosts
        GlobalPosts.getInstance(requireContext()).updatePost(post);
    }

    @Override
    public void onProfileClick(Post post, int position) {
        // TODO: Navigate to user profile
        Toast.makeText(getContext(), "Profile view coming soon!", Toast.LENGTH_SHORT).show();
    }
}
