package hahaido.com.spidertest;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import hahaido.com.spidertest.helper.ImageResponse.ImgurImage;

public class MainActivity extends AppCompatActivity {

    private ImageListFragment mImageListFragment;

    private static final String IMAGE_LIST_TAG = "ImageListFragment";
    private static final String IMAGE_INFO_TAG = "ImageInfoFragment";
    private static final String FRAGMENT_TRANSACTION = "FRAGMENT_TRANSACTION";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        if (savedInstanceState == null) {
            mImageListFragment = new ImageListFragment();
            changeFragment(0, null);
        } else {
            mImageListFragment = (ImageListFragment) getSupportFragmentManager().findFragmentByTag(IMAGE_LIST_TAG);
        }
    }

    public void changeFragment(int frNumber, ImgurImage image) {
        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();

        switch (frNumber) {
            case 0:
                fTrans.add(R.id.fragment_container, mImageListFragment, IMAGE_LIST_TAG);
            break;
            case 1: {
                fTrans.replace(R.id.fragment_container, ImageInfoFragment.newInstance(image), IMAGE_INFO_TAG);
                fTrans.addToBackStack(FRAGMENT_TRANSACTION);
            }
        }

        fTrans.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
