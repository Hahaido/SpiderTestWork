package hahaido.com.spidertest.helper;

import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hahaido.com.spidertest.MainActivity;
import hahaido.com.spidertest.R;
import hahaido.com.spidertest.helper.CommentResponse.ImgurComment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private MainActivity mActivity;
    private Handler mHandler = new Handler();
    private OkHttpClient httpClient;
    private int mPageCount, mNewItems;
    private String mCommentsUrl;
    private List<ImgurComment> mComments= new ArrayList<ImgurComment>();
    private CommentsCallbacks mCallbacks;


    private static final long WEEK = 24 * 60 * 60 * 7;
    private static final long DAY = 24 * 60 * 60;
    private static final long HOUR = 60 * 60;
    private static final long MINUTE = 60;

    private static final String clientID = "6b43ed616be596a";
    private static final String TAG = "CommentsAdapter";

    public CommentsAdapter(MainActivity activity) {
        mActivity = activity;
        httpClient = new OkHttpClient.Builder().build();
    }

    public void setCallbacks(CommentsCallbacks callbacks) {
        mCallbacks = callbacks;
    }


    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        return new CommentsViewHolder(inflater.inflate(R.layout.comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        holder.bind(mComments.get(position));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void getComments(String pageUrl) {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
            .url(pageUrl)
            .header("Authorization", "Client-ID " + clientID)
            .header("User-Agent", "")
            .build();

        final int currSize = mComments.size();
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
                        ImgurComment comment = new ImgurComment();

                        String id = item.getString("id");
                        if (mComments.contains(id))
                            continue;

                        comment.id = id;
                        comment.imageId = item.getString("image_id");
                        comment.comment = item.getString("comment");
                        comment.author = item.getString("author");
                        comment.datetime = item.getLong("datetime");
                        comment.page = mPageCount;

                        mComments.add(comment);
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
        mComments.clear();
        notifyDataSetChanged();
    }

    public void refresh() {
        reset();
        fetchNextPage();
    }

    public void loadComments(String url) {
        reset();
        mCommentsUrl = url;
        fetchNextPage();
    }

    public void fetchNextPage() {
        getComments(mCommentsUrl + mPageCount + ".json");
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        private final CardView mCardView;
        private final TextView mCommentAuthor;
        private final TextView mCommentDatetime;
        private final TextView mComment;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView.findViewById(R.id.card_view);
            mCommentAuthor = (TextView) itemView.findViewById(R.id.comment_author);
            mCommentDatetime = (TextView) itemView.findViewById(R.id.comment_datetime);
            mComment = (TextView) itemView.findViewById(R.id.item_comment);
        }

        public void bind(ImgurComment item) {
            mCommentAuthor.setText(item.author);
            mCommentDatetime.setText(formatDateTime(new Date(item.datetime * 1000)));
            mComment.setText(item.comment);
        }

        private String formatDateTime(Date date) {
            final long timestamp = date.getTime();
            final long now = new Date().getTime();
            final long timeGap = (now - timestamp) / 1000;

            if (timeGap < MINUTE) {
                return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.SECOND_IN_MILLIS).toString();
            } else if (timeGap < HOUR) {
                return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS).toString();
            } else if (timeGap < DAY) {
                return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.HOUR_IN_MILLIS).toString();
            } else if (timeGap < WEEK) {
                return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.DAY_IN_MILLIS).toString();
            } else if (timeGap < WEEK * 4) {
                return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.WEEK_IN_MILLIS).toString();
            } else return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.YEAR_IN_MILLIS).toString();
        }

    }

    public interface CommentsCallbacks {

        public void onCompleted(boolean result);

    }

}