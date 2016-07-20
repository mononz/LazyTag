package net.mononz.lazytag;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mononz.lazytag.database.CursorRecAdapter;
import net.mononz.lazytag.database.Database;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AdapterMessages extends CursorRecAdapter<AdapterMessages.ShowViewHolder> {

    private Callback mCallback;

    public AdapterMessages(Cursor cursor, Callback callback) {
        super(cursor);
        this.mCallback = callback;
    }

    @Override
    public void onBindViewHolder(ShowViewHolder viewHolder, final Cursor cursor) {

        int idx_id = cursor.getColumnIndex(Database.Messages._id);
        int idx_label = cursor.getColumnIndex(Database.Messages.label);
        int idx_message = cursor.getColumnIndex(Database.Messages.message);

        final long _id = cursor.getLong(idx_id);
        final String label = cursor.getString(idx_label);
        final String message = cursor.getString(idx_message);

        viewHolder.vLabel.setText(label);
        viewHolder.vMessage.setText(message);
        viewHolder.vView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCallback.onDelete(_id, message);
                return true;
            }
        });
        viewHolder.vView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.saveCopy(message);
                mCallback.onSnack(message);
            }
        });
    }

    @Override
    public ShowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_message, viewGroup, false);
        return new ShowViewHolder(itemView);
    }

    public class ShowViewHolder extends RecyclerView.ViewHolder {

        View vView;
        @Bind(R.id.label) protected TextView vLabel;
        @Bind(R.id.message) protected TextView vMessage;

        public ShowViewHolder(View v) {
            super(v);
            vView = v;
            ButterKnife.bind(this, v);
        }
    }

    public interface Callback {
        void onSnack(String message);
        void onDelete(long _id, String message);
    }

}