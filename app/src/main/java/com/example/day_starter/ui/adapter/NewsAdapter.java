package com.example.day_starter.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.model.news.NewsResponse.Article;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private List<Article> articles = new ArrayList<>();

    public void setArticles(List<Article> articles) {
        Log.d("NewsAdapter", "기사 설정: " + articles.size() + "개");
        this.articles = new ArrayList<>(articles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView descriptionText;

        NewsViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_news_title);
            descriptionText = itemView.findViewById(R.id.text_news_description);
        }

        void bind(Article article) {
            titleText.setText(article.getTitle());
            descriptionText.setText(article.getDescription());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(article.getUrl()));
                v.getContext().startActivity(intent);
            });
        }
    }
} 