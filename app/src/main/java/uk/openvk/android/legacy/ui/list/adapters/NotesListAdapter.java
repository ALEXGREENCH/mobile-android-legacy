package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.entities.Note;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.Holder> {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Note> objects;
    public boolean opened_sliding_menu;

    public NotesListAdapter(Context context, ArrayList<Note> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Note getItem(int position) {
        return objects.get(position);
    }

    @Override
    public NotesListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotesListAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_note,
                parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
    }

    Note getNote(int position) {
        return (getItem(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_subtext;
        public ImageView item_icon;
        public View view;
        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            item_name = (view.findViewById(R.id.nlist_item_text));
            //item_subtext = (view.findViewById(R.id.nlist_item_subtext));
            item_icon = (view.findViewById(R.id.nlist_item_photo));
        }

        void bind(final int position) {
            Note item = getItem(position);
            ((TextView) view.findViewById(R.id.nlist_item_text)).setText(
                    item.title);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).showNote(position);
                    }
                }
            });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }
    }

}

