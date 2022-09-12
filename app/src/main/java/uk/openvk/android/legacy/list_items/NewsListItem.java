package uk.openvk.android.legacy.list_items;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.items.NewsItemCountersInfo;
import uk.openvk.android.legacy.items.RepostInfo;

public class NewsListItem {

    public String name;
    public RepostInfo repost;
    public String info;
    public String text;
    public int owner_id;
    public int post_id;
    public NewsItemCountersInfo counters;
    public Bitmap avatar;
    public Bitmap photo;

    public NewsListItem(String author, int dt_sec, RepostInfo repostInfo, String post_text, NewsItemCountersInfo nICI, Bitmap author_avatar, Bitmap post_photo, int o_id, int p_id, Context ctx) {
        name = author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        if((System.currentTimeMillis() - (dt_sec * 1000)) < 86400000) {
            info = ctx.getResources().getString(R.string.today_at) + " " + new SimpleDateFormat("HH:mm").format(dt);
        } if((System.currentTimeMillis() - (dt_sec * 1000)) < (86400000 * 2)) {
            info = ctx.getResources().getString(R.string.yesterday_at) + " " + new SimpleDateFormat("HH:mm").format(dt);
        } else {
            info = new SimpleDateFormat("dd MMMM yyyy").format(dt) + " " + ctx.getResources().getString(R.string.date_at) + " " + new SimpleDateFormat("HH:mm").format(dt);
        }
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        photo = post_photo;
        avatar = author_avatar;
        owner_id = o_id;
        post_id = p_id;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public Bitmap getAvatar() {
        return avatar;
    }
}
