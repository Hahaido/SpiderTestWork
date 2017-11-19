package hahaido.com.spidertest.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hahaido.com.spidertest.MainActivity;
import hahaido.com.spidertest.R;
import hahaido.com.spidertest.helper.ImageResponse.ImgurImage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private MainActivity mActivity;
    private Handler mHandler = new Handler();
    private OkHttpClient httpClient;
    private int mPageCount, mNewItems, mColumnWidth;
    private String mGalleryUrl;
    private List<ImgurImage> mImages= new ArrayList<ImgurImage>();
    private GalleryCallbacks mCallbacks;

    private static final String clientID = "6b43ed616be596a";
    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(MainActivity activity) {
        mActivity = activity;
        httpClient = new OkHttpClient.Builder().build();

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        final int factor = metrics.heightPixels <= metrics.widthPixels ? metrics.heightPixels: metrics.widthPixels;
        int layoutMargin = (int) mActivity.getResources().getDimension(R.dimen.card_margin);
        mColumnWidth = factor/2 - 3 * layoutMargin;
    }

    public void setCallbacks(GalleryCallbacks callbacks) {
        mCallbacks = callbacks;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView mCardView;
        private final ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView.findViewById(R.id.card_view);
            mImageView = (ImageView) itemView.findViewById(R.id.card_image);

            mImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    mActivity.changeFragment(1, mImages.get(position));
                }

            });
        }

    }

    private class MyTransformation implements Transformation {

        private int mTargetHeight;

        public MyTransformation(int targetHeight) {
            mTargetHeight = targetHeight;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap result = Bitmap.createScaledBitmap(source, mColumnWidth, mTargetHeight, false);

            if (result != source) {
                source.recycle();
            }

            return result;
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }

    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        return new ViewHolder(inflater.inflate(R.layout.image_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImgurImage image = mImages.get(position);

        String imageId;
        int width = 0, height = 0;
        if (image.isAlbum) {
            imageId = image.cover;
            width = image.cover_width;
            height = image.cover_height;
        } else {
            imageId = image.id;
            width = image.width;
            height = image.height;
        }

        double aspectRatio = (double) height / (double) width;
        final int targetHeight = (int) (mColumnWidth * aspectRatio);

        ViewGroup.LayoutParams layoutParams = holder.mImageView.getLayoutParams();
        layoutParams.height = targetHeight;
        holder.mImageView.setLayoutParams(layoutParams);

        Picasso.with(mActivity)
            .load("https://i.imgur.com/" + imageId + ".jpg")
            .transform(new MyTransformation(targetHeight))
            .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public void getGallery(String pageUrl) {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
            .url(pageUrl)
            .header("Authorization", "Client-ID " + clientID)
            .header("User-Agent", "")
            .build();

        final int currSize = mImages.size();
        httpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONArray items = data.getJSONArray("data");

                    mNewItems = 0;
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        ImgurImage photo = new ImgurImage();

                        String id = item.getString("id");
                        if (mImages.contains(id))
                            continue;

                        photo.id = id;
                        photo.title = item.getString("title");

                        photo.isAlbum = item.getBoolean("is_album");
                        if (photo.isAlbum) {
                            photo.cover= item.getString("cover");
                            photo.cover_width= item.getInt("cover_width");
                            photo.cover_height= item.getInt("cover_height");
                        } else {
                            photo.width= item.getInt("width");
                            photo.height= item.getInt("height");
                        }
                        photo.page = mPageCount;

                        mImages.add(photo);
                        ++mNewItems;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "An error has occurred " + e);
                }

                ++mPageCount;
                Log.d(TAG, "Page loaded: " + mPageCount);

                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        notifyItemRangeInserted(currSize, mNewItems);
                        Log.d(TAG, "New items inserted: " + mNewItems);
                    }
                });

                mCallbacks.onCompleted(true);
            }
        });

    }

    private void reset() {
        mPageCount = 0;
        mImages.clear();
        notifyDataSetChanged();
    }

    public void refresh() {
        reset();
        fetchNextPage();
    }

    public void loadGallery(String url) {
        reset();
        mGalleryUrl = url;
        fetchNextPage();
    }

    public void fetchNextPage() {
        getGallery(mGalleryUrl + mPageCount + ".json");
    }

    public interface GalleryCallbacks {

        public void onCompleted(boolean result);

    }

}