package com.andrei.booklisting;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

class Book implements Parcelable {

    private ArrayList<String> mAuthor;
    private String mTitle;
    private String mUrl;

    Book (ArrayList<String> author, String title, String url) {
        mAuthor = author;
        mTitle = title;
        mUrl = url;
    }

    ArrayList<String> getAuthor() {
        return mAuthor;
    }

    String getTitle() {
        return mTitle;
    }

    String getUrl() {
        return mUrl;
    }

    private Book(Parcel in) {
        if (in.readByte() == 0x01) {
            mAuthor = new ArrayList<>();
            in.readList(mAuthor, String.class.getClassLoader());
        } else {
            mAuthor = null;
        }
        mTitle = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mAuthor == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mAuthor);
        }
        dest.writeString(mTitle);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
