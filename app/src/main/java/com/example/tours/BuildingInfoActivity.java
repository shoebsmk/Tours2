package com.example.tours;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.tours.databinding.ActivityFenceInfoBinding;

public class BuildingInfoActivity extends AppCompatActivity {

    private Typeface myCustomFont;

    ActivityFenceInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFenceInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFonts();
        setUpActionBar();

        if (getIntent().hasExtra("FENCE_ID")) {
            String id = getIntent().getStringExtra("FENCE_ID");
            if(((Building) MapsActivity.buildingMap.get(id)) == null || MapsActivity.buildingMap.size() == 0){return;}
            binding.buildingNameTV.setText(id);
            setBuildingPhoto(binding, id);
            binding.buildingAddressTV.setText(((Building) MapsActivity.buildingMap.get(id)).getAddress());
            binding.buildingDescriptionTV.setText(((Building) MapsActivity.buildingMap.get(id)).getDescription());
            binding.buildingDescriptionTV.setMovementMethod(new ScrollingMovementMethod());
        }

    }

    private void setBuildingPhoto(ActivityFenceInfoBinding binding, String id) {
        try {
            String imageURL = ((Building) MapsActivity.buildingMap.get(id)).getImageURL();
            if (imageURL  != null) {

                Glide.with(this)
                        .load(imageURL)
                        //.load("https://cdn.britannica.com/33/194733-050-4CF75F31/Girl-with-a-Pearl-Earring-canvas-Johannes-1665.jpg")
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }

                        })
                        .error(R.drawable.brokenimage)
                        .placeholder(R.drawable.loading)
                        .into(binding.buildingPhotoIV);


            } else {
                binding.buildingPhotoIV.setImageResource(R.drawable.noimage);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            // Comment out the below to show the default home indicator
            actionBar.setHomeAsUpIndicator(R.drawable.home_image);
            actionBar.setTitle("");
        }
    }

    private void setFonts() {
        // Fonts go in the "assets" folder, with java and res
        myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        binding.buildingDescriptionTV.setTypeface(myCustomFont);
        binding.buildingNameTV.setTypeface(myCustomFont);
        binding.buildingAddressTV.setTypeface(myCustomFont);

    }
}