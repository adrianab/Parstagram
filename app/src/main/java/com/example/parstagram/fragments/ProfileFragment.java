package com.example.parstagram.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parstagram.LoginActivity;
import com.example.parstagram.Post;
import com.example.parstagram.PostsAdapter;
import com.example.parstagram.R;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    public static final String PROFILE_PICTURE = "profile_picture";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private File photoFile;
    private String photoFileName = "photo.jpg";
    private List<Post> userPosts;
    ParseUser parseUser;
    PostsAdapter postsAdapter;
    Button btnLogout;
    ImageView ivProfilePicture;
    //TextView tvScreenName;
    TextView tvUserName;
    //TextView tvJoined;
    RecyclerView rvUserPosts;
    Post post;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parseUser = ParseUser.getCurrentUser();
        userPosts = new ArrayList<>();
        postsAdapter = new PostsAdapter(getContext(), userPosts);

        btnLogout = view.findViewById(R.id.btnLogOut);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        //tvScreenName = view.findViewById(R.id.tvScreenName);
        tvUserName = view.findViewById(R.id.tvUsername);
        //tvJoined = view.findViewById(R.id.tvJoined);
        rvUserPosts = view.findViewById(R.id.rvUserPosts);

        rvUserPosts.setAdapter(postsAdapter);
        rvUserPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        queryPosts();

        // tvScreenName.setText((user.get("screenname")).toString());
        //Log.i(TAG, "ScreenName:" + (String) user.get("screenname"));
        // String screenName = parseUser.getString("screenname").toString();
        // tvScreenName.setText(parseUser.get("screenname").toString());
        /*
        if (parseUser.getString("screenname") != null) {
            tvScreenName.setText(parseUser.getString("screenname"));
        }
        */
        //tvScreenName.setText(post.getUser().getString("screenname"));
        //tvScreenName.setText(parseUser.getUsername().toUpperCase());

        tvUserName.setText("@" + parseUser.getUsername());

        /*
        String pattern = "E, dd MMMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(parseUser.getCreatedAt());
        tvJoined.setText(date);
        */

        ParseFile userProfilePic = (ParseFile) parseUser.get(PROFILE_PICTURE);
        if(userProfilePic != null)
            Glide.with(this).load(userProfilePic.getUrl())
                    .placeholder(R.drawable.defaultavatar)
                    .into(ivProfilePicture);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogout();
            }
        });

        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATEDAT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null)
                {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                for (Post post : posts)
                {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }

                userPosts.addAll(posts);
                postsAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivProfilePicture.setImageBitmap(takenImage);

                saveProfilePicture();

            } else {
                Toast.makeText(getContext(), "Picture was not taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfilePicture() {

        parseUser.put(PROFILE_PICTURE,new ParseFile(photoFile));

        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if(e != null){
                    Log.d(TAG,"Image failed to upload!",e);
                    Toast.makeText(getContext(), "Image failed to upload!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG,"Image was successfully saved!");
                Toast.makeText(getContext(), "Image was successfully saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //launches the camera and get photo
    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void userLogout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error logging out!", e);
                    Toast.makeText(getContext(), "Error logging out!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Successfully logged out!");
                Toast.makeText(getContext(), "Successfully logged out", Toast.LENGTH_SHORT).show();
                goLoginActivity();
            }
        });
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
    }

    private void goLoginActivity() {
        Intent i  = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

}