/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.Place;
import co.vamojunto.widgets.OnRecyclerViewItemClickListener;

import static co.vamojunto.R.color.black;

/**
 * Adapter referente a lista de locais exibidos durante a busca do usuário.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class SearchPlaceAdapter extends RecyclerView.Adapter<SearchPlaceAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTituloTextView;
        public TextView mDescTextView;

        public ViewHolder(final View itemView) {
            super(itemView);

            mTituloTextView = (TextView) itemView.findViewById(R.id.txt_titulo);
            mDescTextView = (TextView) itemView.findViewById(R.id.txt_descricao);
        }

        public ViewHolder(final View itemView, final OnRecyclerViewItemClickListener onClickListener) {
            this(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.OnItemClicked(itemView, getPosition());
                }
            });
        }
    }

    private List<Place> mDataset;

    private OnRecyclerViewItemClickListener mOnItemClickListener;

    private Context mContext;

    public SearchPlaceAdapter(Context c, List<Place> dataset) {
        this.mDataset = dataset;
        this.mContext = c;
        this.mOnItemClickListener = null;
    }

    public SearchPlaceAdapter(Context c, List<Place> dataset,
                              OnRecyclerViewItemClickListener onItemClickListener) {
        this.mDataset = dataset;
        this.mContext = c;
        this.mOnItemClickListener = onItemClickListener;
    }

    public Place getItem(int position) {
        if (this.mDataset == null)
            return null;

        return this.mDataset.get(position);
    }

    public void setDataset(List<Place> dataset) {
        mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_place_list_row, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh;
        if ( mOnItemClickListener == null )
            vh = new ViewHolder(v);
        else
            vh = new ViewHolder(v, mOnItemClickListener);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTituloTextView.setText(mDataset.get(position).getTitulo());

        String endereco = mDataset.get(position).getmEndereco();

        holder.mDescTextView.setText(endereco);
    }

    @Override
    public int getItemCount() {
        if ( mDataset == null )
            return 0;

        return mDataset.size();
    }

}
