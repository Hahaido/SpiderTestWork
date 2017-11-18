package hahaido.com.spidertest;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import hahaido.com.spidertest.helper.CommentsAdapter;
import hahaido.com.spidertest.helper.ImageResponse.ImgurImage;
import okhttp3.OkHttpClient;

public class ImageInfoFragment extends Fragment {

    private Context mContext;
    private CommentsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ImageView mImageView;
    private OkHttpClient httpClient;
    private ImgurImage mImage;
    protected boolean mIsLoading = false;

    private static final String EXTRA_IMAGE = "IMAGE";
    private static final String clientID = "6b43ed616be596a";
    private static final String gallery = "https://api.imgur.com/3/gallery/";
    private static final String comments = "/comments/best/";
    private static final String TAG = "ImageInfoFragment";

    public ImageInfoFragment() {}

    public static ImageInfoFragment newInstance(ImgurImage image) {
        final ImageInfoFragment fragment = new ImageInfoFragment();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        if (savedInstanceState == null) {
            if (getArguments() != null) {
                mImage = getArguments().getParcelable(EXTRA_IMAGE);
            }
        } else {
            mImage = savedInstanceState.getParcelable(EXTRA_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View parent = (View) inflater.inflate(R.layout.image_info_fragment, container, false);

        mRecyclerView = (RecyclerView) parent.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mImageView = (ImageView) parent.findViewById(R.id.card_image);

        String imageId = "";
        if (mImage.isAlbum) {
            imageId = mImage.cover;
        } else {
            imageId = mImage.id;
        }

        Picasso.with(getActivity())
            .load("https://i.imgur.com/" + imageId + ".jpg")
            .into(mImageView);

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            setAdapter();
        }
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        LinearLayoutManager layoutMgr = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        int visibleItemCount = layoutMgr.getChildCount();
        int totalItemCount = layoutMgr.getItemCount();
        int firstVisibleItemPosition = layoutMgr.findFirstVisibleItemPosition();

        if (totalItemCount > 0 && firstVisibleItemPosition + visibleItemCount >= totalItemCount && !mIsLoading) {
            mIsLoading = true;
            mAdapter.fetchNextPage();
        }
        }

    };

    private void setAdapter() {
        mAdapter = new CommentsAdapter((MainActivity) getActivity());
        mAdapter.setCallbacks(new CommentsAdapter.CommentsCallbacks() {

            @Override
            public void onCompleted(boolean result) {
                mIsLoading = false;
            }

        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        mAdapter.loadComments(gallery + mImage.id + comments);
    }

}
