package hahaido.com.spidertest.helper;

public class CommentResponse {

    public boolean success;
    public int status;
    public ImgurComment data;

    public static class ImgurComment{
        public String id;
        public String imageId;
        public String comment;
        public String author;
        public long datetime;
        public int page;

        @Override public String toString() {
            return "UploadedImage{" +
                    "id='" + id + '\'' +
                    ", imageId='" + imageId + '\'' +
                    ", comment='" + comment + '\'' +
                    ", author='" + author + '\'' +
                    ", datetime=" + datetime +
                    ", page=" + page +
                    '}';
        }
    }

    @Override public String toString() {
        return "ImageResponse{" +
                "success=" + success +
                ", status=" + status +
                ", data=" + data.toString() +
                '}';
    }
}
