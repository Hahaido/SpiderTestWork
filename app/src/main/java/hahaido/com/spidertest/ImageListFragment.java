package hahaido.com.spidertest;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hahaido.com.spidertest.helper.GalleryAdapter;

public class ImageListFragment extends Fragment {

    private Context mContext;
    private GalleryAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGrid;
    protected boolean mIsLoading = false;
    private int pastVisibleItems;

    private static final String galleryViral = "https://api.imgur.com/3/gallery/hot/viral/";
    private static final String TAG = "ImageListFragment";

    public ImageListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View parent = (View) inflater.inflate(R.layout.image_list_fragment, container, false);

        mStaggeredGrid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) parent.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mStaggeredGrid);

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setAdapter();
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (mStaggeredGrid == null) {
                mStaggeredGrid = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
            }

            int visibleItemCount = mStaggeredGrid.getChildCount();
            int totalItemCount = mStaggeredGrid.getItemCount();
            int[] firstVisibleItems = null;
            firstVisibleItems = mStaggeredGrid.findFirstVisibleItemPositions(firstVisibleItems);

            if(firstVisibleItems != null && firstVisibleItems.length > 0) {
                pastVisibleItems = firstVisibleItems[0];
            }

            if (totalItemCount > 0 && pastVisibleItems + visibleItemCount >= totalItemCount  && !mIsLoading) {
                mIsLoading = true;
                mAdapter.fetchNextPage();
            }
        }

    };

    private void setAdapter() {
        mAdapter = new GalleryAdapter((MainActivity) getActivity());
        mAdapter.setCallbacks(new GalleryAdapter.GalleryCallbacks() {

            @Override
            public void onCompleted(boolean result) {
                mIsLoading = false;
            }

        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        mAdapter.loadGallery(galleryViral);
    }

}
