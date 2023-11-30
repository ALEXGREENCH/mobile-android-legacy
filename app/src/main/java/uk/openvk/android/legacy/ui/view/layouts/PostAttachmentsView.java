package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.CommonAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.entities.Video;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.ui.core.activities.NoteActivity;
import uk.openvk.android.legacy.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.ui.core.activities.VideoPlayerActivity;
import uk.openvk.android.legacy.ui.view.FlowLayout;

public class PostAttachmentsView extends LinearLayout {

    private TextView error_label;
    private boolean safeViewing;
    private String instance;
    private SharedPreferences global_prefs;
    private ArrayList<Attachment> attachments;
    private VideoAttachView videoView;
    private ImageView photoView;
    private PollAttachView pollView;
    private CommonAttachView commonView;
    private int resize_videoattachviews;
    private Context parent;
    private ImageLoaderConfiguration imageLoaderConfig;
    private DisplayImageOptions displayimageOptions;
    private ImageLoader imageLoader;
    public boolean isWall;
    private int photo_fail_count;

    public PostAttachmentsView(Context ctx) {
        super(ctx);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_post_attachments, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = FlowLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        instance = global_prefs.getString("current_instance", "");
        safeViewing = global_prefs.getBoolean("safeViewing", true);
        error_label = findViewById(R.id.error_label);
        photoView = findViewById(R.id.post_photo);
        videoView = findViewById(R.id.post_video);
        pollView = findViewById(R.id.post_poll);
        commonView = findViewById(R.id.post_attachment);
        parent = ctx;
        initializeImageLoader();
        error_label.setVisibility(GONE);
        photoView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        pollView.setVisibility(GONE);
        commonView.setVisibility(View.GONE);
    }

    public PostAttachmentsView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_post_attachments, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = FlowLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        instance = global_prefs.getString("current_instance", "");
        safeViewing = global_prefs.getBoolean("safeViewing", true);
        error_label = findViewById(R.id.error_label);
        photoView = findViewById(R.id.post_photo);
        videoView = findViewById(R.id.post_video);
        pollView = findViewById(R.id.post_poll);
        commonView = findViewById(R.id.post_attachment);
        parent = ctx;
        initializeImageLoader();
        error_label.setVisibility(GONE);
        photoView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        pollView.setVisibility(GONE);
        commonView.setVisibility(View.GONE);
    }

    private void initializeImageLoader() {
        this.displayimageOptions =
                new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_8888).build();
        this.imageLoaderConfig =
                new ImageLoaderConfiguration.Builder(parent.getApplicationContext()).
                        defaultDisplayImageOptions(displayimageOptions)
                        .memoryCacheSize(16777216) // 16 MB memory cache
                        .writeDebugLogs()
                        .build();
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(imageLoaderConfig);
    }

    public void loadAttachments(ArrayList<WallPost> posts,
                                final WallPost post,
                                ArrayList<Attachment> attachments,
                                int position, boolean isWall) {
        this.attachments = attachments;
        if(!post.is_explicit || !safeViewing) {
            for (int i = 0; i < post.attachments.size(); i++) {
                String type = post.attachments.get(i).type;
                switch (type) {
                    case "photo":
                        photoView.setVisibility(View.VISIBLE);
                        Photo photo = (Photo) post.attachments.get(i).getContent();
                        loadPhotoPlaceholder(post, photo, photoView);
                        loadPhotoAttachment(photo, post.owner_id, post.post_id, photoView, isWall);
                        break;
                    case "video":
                        if (post.attachments.get(i).getContent() != null) {
                            final Video videoAttachment = (Video)
                                    post.attachments.get(i).getContent();
                            videoView.setAttachment(videoAttachment);
                            videoView.setVisibility(View.VISIBLE);
                            videoView.setThumbnail(post.owner_id);
                            if (resize_videoattachviews < 1) {
                                videoView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        float widescreen_aspect_ratio = videoView.getMeasuredWidth() / 16;
                                        float attachment_height = widescreen_aspect_ratio * 9;
                                        LinearLayout.LayoutParams lp = (LayoutParams) videoView.getLayoutParams();
                                        lp.height = (int) attachment_height;
                                        videoView.setLayoutParams(lp);
                                    }
                                });
                                resize_videoattachviews++;
                            }
                            videoView.getViewTreeObserver().addOnGlobalLayoutListener(
                                    new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            float widescreen_aspect_ratio = videoView.getMeasuredWidth() / 16;
                                            float attachment_height = widescreen_aspect_ratio * 9;
                                            videoView.getLayoutParams().height = (int) attachment_height;
                                        }
                                    });
                            videoView.findViewById(R.id.video_att_view).setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            playVideo(post, videoAttachment);
                                        }
                                    }
                            );
                        }
                        break;
                    case "poll":
                        if (post.attachments.get(i).getContent() != null) {
                            PollAttachment pollAttachment = ((PollAttachment)
                                    post.attachments.get(i).getContent());
                            pollView.createAdapter(parent, position, posts, post,
                                    pollAttachment.answers, pollAttachment.multiple,
                                    pollAttachment.user_votes, pollAttachment.votes);
                            pollView.setPollInfo(pollAttachment.question, pollAttachment.anonymous,
                                    pollAttachment.end_date);
                            pollView.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "note":
                        if (post.attachments.get(i).getContent() != null) {
                            CommonAttachment commonAttachment = ((CommonAttachment)
                                    post.attachments.get(i).getContent());
                            commonView.setAttachment(post.attachments.get(i));
                            viewNoteAttachment(commonAttachment, post);
                            commonView.setVisibility(VISIBLE);
                        }
                        break;
                }
                if(!type.equals("note")) {
                    switch (post.attachments.get(i).status) {
                        case "not_supported":
                            error_label.setText(
                                    parent.getResources().getString(R.string.not_supported)
                            );
                            error_label.setVisibility(View.VISIBLE);
                            break;
                        case "error":
                            error_label.setText(
                                    parent.getResources().getString(R.string.attachment_load_err)
                            );
                            error_label.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            }
        }
        setVisibility(VISIBLE);
    }

    private void loadPhotoPlaceholder(final WallPost post, Photo photo, ImageView view) {
        Drawable drawable = parent.getResources().getDrawable(R.drawable.photo_placeholder);
        Canvas canvas = new Canvas();
        try {
            Bitmap bitmap = Bitmap.createBitmap(
                    photo.size[0], photo.size[1], Bitmap.Config.ARGB_8888
            );
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, photo.size[0], photo.size[1]);
            drawable.draw(canvas);
            view.setImageBitmap(bitmap);
        } catch (OutOfMemoryError ignored) {
            imageLoader.clearMemoryCache();
            imageLoader.clearDiskCache();
            // Retrying again
            if(photo_fail_count < 5) {
                photo_fail_count++;
                loadPhotoPlaceholder(post, photo, view);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPhotoAttachment(post);
            }
        });
    }

    private void loadPhotoAttachment(Photo photo,
                                     long owner_id, long post_id, ImageView view, boolean isWall) {
        try {
            String full_filename = "file://" + parent.getCacheDir()
                    + "/" + instance + "/photos_cache/newsfeed_photo_attachments/" +
                    photo.filename;
            if (isWall) {
                full_filename = "file://" + parent.getCacheDir()
                        + "/" + instance + "/photos_cache/wall_photo_attachments/" +
                        photo.filename;
            }

            Bitmap bitmap = imageLoader.loadImageSync(full_filename);
            if(bitmap != null) {
                view.setImageBitmap(bitmap);
            }
        } catch (OutOfMemoryError oom) {
            imageLoader.clearMemoryCache();
            imageLoader.clearDiskCache();
            // Retrying again
            if(photo_fail_count < 5) {
                photo_fail_count++;
                loadPhotoAttachment(photo, owner_id, post_id, view, isWall);
            }
        }
    }

    private void viewNoteAttachment(CommonAttachment attachment, WallPost post) {
        Intent intent = new Intent(parent, NoteActivity.class);
        intent.putExtra("title", attachment.title);
        intent.putExtra("content", attachment.text);
        intent.putExtra("author", post.name);
        commonView.setIntent(intent);
        commonView.setVisibility(View.VISIBLE);
    }

    private void playVideo(WallPost item, Video video) {
        Intent intent = new Intent(parent, VideoPlayerActivity.class);
        intent.putExtra("title", video.title);
        intent.putExtra("attachment", video);
        intent.putExtra("files", video.files);
        intent.putExtra("owner_id", item.owner_id);
        parent.startActivity(intent);
    }

    public void viewPhotoAttachment(WallPost post) {
        WallPost item;
        Intent intent = new Intent(parent.getApplicationContext(), PhotoViewerActivity.class);
        if (isWall) {
            intent.putExtra("where", "wall");
        } else {
            intent.putExtra("where", "newsfeed");
        }
        try {
            if (isWall) {
                intent.putExtra("local_photo_addr",
                        String.format("%s/wall_photo_attachments/wall_attachment_o%sp%s",
                                parent.getCacheDir(),
                                post.owner_id, post.post_id));
            } else {
                intent.putExtra("local_photo_addr",
                        String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s",
                                parent.getCacheDir(),
                                post.owner_id, post.post_id));
            }
            if(post.attachments != null) {
                for(int i = 0; i < post.attachments.size(); i++) {
                    if(post.attachments.get(i).type.equals("photo")) {
                        Photo photo = ((Photo) post.attachments.get(i).
                                getContent());
                        intent.putExtra("original_link", photo.original_url);
                        intent.putExtra("author_id", post.author_id);
                        intent.putExtra("photo_id", photo.id);
                    }
                }
            }
            parent.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
