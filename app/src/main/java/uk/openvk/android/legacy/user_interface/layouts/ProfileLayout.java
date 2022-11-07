package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.models.User;

public class ProfileLayout extends LinearLayout {
    public ProfileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_layout, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void updateLayout(User user) {
        ProfileHeader header = (ProfileHeader) findViewById(R.id.profile_header);
        header.setProfileName(String.format("%s %s  ", user.first_name, user.last_name));
        header.setOnline(user.online);
        header.setStatus(user.status);
        header.setLastSeen(user.ls_date);
        header.setVerified(user.verified, getContext());
        ((ProfileCounterLayout) findViewById(R.id.photos_counter)).setCounter(0, Arrays.asList(getResources().getStringArray(R.array.profile_photos)).get(2), "");
        ((ProfileCounterLayout) findViewById(R.id.photos_counter)).setOnCounterClickListener();
        ((ProfileCounterLayout) findViewById(R.id.friends_counter)).setCounter(0, Arrays.asList(getResources().getStringArray(R.array.profile_friends)).get(2), "openvk://friends/id" + user.id);
        ((ProfileCounterLayout) findViewById(R.id.friends_counter)).setOnCounterClickListener();
        ((ProfileCounterLayout) findViewById(R.id.mutual_counter)).setCounter(0, Arrays.asList(getResources().getStringArray(R.array.profile_mutual_friends)).get(2), "");
        ((ProfileCounterLayout) findViewById(R.id.mutual_counter)).setOnCounterClickListener();
        ((AboutProfileLayout) findViewById(R.id.about_profile_layout)).setBirthdate("");
        ((AboutProfileLayout) findViewById(R.id.about_profile_layout)).setInterests(user.interests, user.music, user.movies, user.tv, user.books);
        ((AboutProfileLayout) findViewById(R.id.about_profile_layout)).setContacts(user.city);
        header.findViewById(R.id.profile_head_highlight).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutProfileLayout aboutProfile = ((AboutProfileLayout) findViewById(R.id.about_profile_layout));
                if(aboutProfile.getVisibility() == GONE) {
                    aboutProfile.setVisibility(VISIBLE);
                } else {
                    aboutProfile.setVisibility(GONE);
                }
            }
        });
        ((ProfileWallSelector) findViewById(R.id.wall_selector)).setUserName(user.first_name);
    }

    public void setDMButtonListener(final Context ctx, final int peer_id) {
        if(!((OvkApplication) ctx.getApplicationContext()).isTablet) {
            ((Button) findViewById(R.id.send_direct_msg)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).getConversationById(peer_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).getConversationById(peer_id);
                    }
                }
            });
        } else {
            ((ImageButton) findViewById(R.id.send_direct_msg)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).getConversationById(peer_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).getConversationById(peer_id);
                    }
                }
            });
        }
    }

    public void setAddToFriendsButtonListener(final Context ctx, final int user_id, final User user) {
        TextView friend_status = ((TextView) findViewById(R.id.friend_status));
        ImageButton add_to_friends_btn = ((ImageButton) findViewById(R.id.add_to_friends));
        if(user.friends_status == 0) {
            friend_status.setVisibility(GONE);
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_add));
        } else if(user.friends_status == 1) {
            friend_status.setText(getResources().getString(R.string.friend_status_req_sent, user.first_name));
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_cancel));
        } else if(user.friends_status == 2) {
            friend_status.setText(getResources().getString(R.string.friend_status_req_recv_m, user.first_name));
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_add));
        } else if(user.friends_status == 3){
            friend_status.setText(getResources().getString(R.string.friend_status_friend, user.first_name));
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_cancel));
        }
        add_to_friends_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.friends_status == 0 || user.friends_status == 2) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).addToFriends(user_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).addToFriends(user_id);
                    }
                } else {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).deleteFromFriends(user_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).deleteFromFriends(user_id);
                    }
                }
            }
        });
    }

    public void loadAvatar(User user, String quality) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/profile_avatars/avatar_%s", getContext().getCacheDir(), user.id), options);
            if(quality.equals("medium")) {
                if (bitmap != null) {
                    user.avatar = bitmap;
                } else if (user.avatar_msize_url.length() > 0) {
                    user.avatar = null;
                } else {
                    user.avatar = null;
                }
            } else if(quality.equals("high")) {
                if (bitmap != null) {
                    user.avatar = bitmap;
                } else if (user.avatar_hsize_url.length() > 0) {
                    user.avatar = null;
                } else {
                    user.avatar = null;
                }
            } else {
                if (bitmap != null) {
                    user.avatar = bitmap;
                } else if (user.avatar_osize_url.length() > 0) {
                    user.avatar = null;
                } else {
                    user.avatar = null;
                }
            }
            if (user.avatar != null)
                ((ImageView) findViewById(R.id.profile_photo)).setImageBitmap(user.avatar);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
    }

    public void setCounter(User user, String where, int count) {
        if(where.equals("friends")) {
            ((ProfileCounterLayout) findViewById(R.id.friends_counter)).setCounter(count, Arrays.asList(getResources().getStringArray(R.array.profile_friends)).get(2), "openvk://friends/id" + user.id);
        }
    }

    public void hideHeaderButtons(Context ctx) {
        if(!((OvkApplication) ctx.getApplicationContext()).isTablet) {
            ((Button) findViewById(R.id.send_direct_msg)).setVisibility(GONE);
        } else {
            ((ImageButton) findViewById(R.id.send_direct_msg)).setVisibility(GONE);
        }
        ((ImageButton) findViewById(R.id.add_to_friends)).setVisibility(GONE);
    }
}