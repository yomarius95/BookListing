package com.andrei.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

class BookAdapter extends ArrayAdapter<Book> {

    private ArrayList<Book> mBooks;

    BookAdapter (Context context, ArrayList<Book> books) {
        super(context, 0, books);
        mBooks = books;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        ViewHolder holder;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

            holder = new ViewHolder(listItemView);
            listItemView.setTag(holder);
        } else {
            holder = (ViewHolder) listItemView.getTag();
        }

        final Book currentBook = getItem(position);

        if (currentBook != null) {
            ArrayList<String> authorList = currentBook.getAuthor();
            StringBuilder authorString = new StringBuilder();
            authorString.append(authorList.get(0));
            if (authorList.size() > 1) {
                for (int i = 1; i < authorList.size(); i++) {
                    authorString.append(", ").append(authorList.get(i));
                }
            }
            holder.authorTextView.setText(authorString);

            holder.titleTextView.setText(currentBook.getTitle());

        }

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (currentBook != null) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentBook.getUrl()));
                }
                getContext().startActivity(intent);
            }
        });

        return listItemView;
    }

    ArrayList<Book> getAllItems() {
        return mBooks;
    }

    static class ViewHolder {
        @BindView(R.id.title) TextView titleTextView;
        @BindView(R.id.author) TextView authorTextView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
