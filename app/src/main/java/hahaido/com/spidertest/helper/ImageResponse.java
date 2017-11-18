package hahaido.com.spidertest.helper;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageResponse {

    public boolean success;
    public int status;
    public ImgurImage data;

    public static class ImgurImage implements Parcelable {

        public String id;
        public String title;
        public String cover;
        public boolean isAlbum;
        public int cover_width;
        public int cover_height;
        public int width;
        public int height;
        public long size;
        public int views;
        public int page;

        public ImgurImage() {}

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(id);
            out.writeString(title);
            out.writeString(cover);
            out.writeByte((byte) (isAlbum ? 1 : 0));
            out.writeInt(cover_width);
            out.writeInt(cover_height);
            out.writeInt(width);
            out.writeInt(height);
            out.writeLong(size);
            out.writeInt(views);
            out.writeInt(page);
        }

        public static final Parcelable.Creator<ImgurImage> CREATOR = new Parcelable.Creator<ImgurImage>() {

            public ImgurImage createFromParcel(Parcel in) {
                return new ImgurImage(in);
            }

            public ImgurImage[] newArray(int size) {
                return new ImgurImage[size];
            }

        };

        private ImgurImage(Parcel in) {
            id = in.readString();
            title = in.readString();
            cover = in.readString();
            isAlbum = in.readByte() != 0;
            cover_width = in.readInt();
            cover_height = in.readInt();
            width = in.readInt();
            height = in.readInt();
            size = in.readLong();
            views = in.readInt();
            page = in.readInt();
        }

        @Override
        public String toString() {
            return "UploadedImage{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", cover='" + cover + '\'' +
                    ", isAlbum=" + isAlbum +
                    ", cover_width=" + cover_width +
                    ", cover_height=" + cover_height +
                    ", cover_width=" + width +
                    ", cover_height=" + height +
                    ", size=" + size +
                    ", views=" + views +
                    ", page=" + page +
                    '}';
        }

    }

    @Override
    public String toString() {
        return "ImageResponse{" +
                "success=" + success +
                ", status=" + status +
                ", data=" + data.toString() +
                '}';
    }

}
