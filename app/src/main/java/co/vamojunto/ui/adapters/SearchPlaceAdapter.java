/*
 * Copyright (c) 2015 Vamo Junto. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Vamo Junto
 * ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Vamo Junto.
 *
 * VAMO JUNTO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. VAMO JUNTO SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * See LICENSE.txt
 */

package co.vamojunto.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.Place;

/**
 * Adapter referente a lista de locais exibidos durante a busca do usuário.
 *
 * Utiliza um objeto {@link List}<{@link Place}> como dataset para exibição.Foi um implementado um
 * método chamado setDataset responsável por alterar o dataset, e exibir novos dados. Caso o dataset
 * informado seja null, nada é exibido na tela. Um dataset contendo uma lista vazia, é visto como
 * uma consulta que não retornou nenhum resultado, neste caso uma mensagem é exibida ao usuário.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class SearchPlaceAdapter extends RecyclerView.Adapter<SearchPlaceAdapter.ViewHolder> {

    private static final String TAG = "SearchPlaceAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTituloTextView;
        public TextView mDescTextView;

        public ViewHolder(final View itemView) {
            super(itemView);

            mTituloTextView = (TextView) itemView.findViewById(R.id.txt_titulo);
            mDescTextView = (TextView) itemView.findViewById(R.id.txt_descricao);
        }

        public ViewHolder(final View itemView, final OnItemClickListener onClickListener) {
            this(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.OnItemClicked(itemView, getPosition());
                }
            });
        }
    }

    public static interface OnItemClickListener {
        public void OnItemClicked(View v, int position);
    }

    /** Lista de locais utilizada como dataset para o RecyclerView */
    private List<Place> mDataset;

    private OnItemClickListener mOnItemClickListener;

    private Context mContext;

    public SearchPlaceAdapter(Context c, List<Place> dataset) {
        this.mDataset = dataset;
        this.mContext = c;
        this.mOnItemClickListener = null;
    }

    public SearchPlaceAdapter(Context c, List<Place> dataset,
                              OnItemClickListener onItemClickListener) {
        this.mDataset = dataset;
        this.mContext = c;
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * Retorna a instância de um determinado local no dataset.
     * @param position posição do local no dataset.
     * @return Uma instância de {@link Place}, ou null, caso o dataset seja menor que a posição desejada.
     */
    public Place getItem(int position) {
        if (this.mDataset == null)
            return null;

        Place p = null;
        try {
            p = this.mDataset.get(position);
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "[getItem] Erro ao obter local do dataset " + e);
        }

        return p;
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
        // Verifica se o dataset possui algum item para ser exibido, caso contrário, a mensagem
        // de erro é exibida ao usuário.
        if ( mDataset.size() > 0) {
            holder.mTituloTextView.setText(mDataset.get(position).getTitulo());

            String endereco = mDataset.get(position).getEndereco();
            holder.mDescTextView.setText(endereco);
        } else {
            holder.mTituloTextView.setText(R.string.no_results_found);
            holder.mDescTextView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        if ( mDataset == null )
            return 0;

        // Caso o dataset esteja vazio, o tamanho 1 é retornado, para que seja exibido um item
        // com a mensagem de erro.
        if ( mDataset.size() == 0 )
            return 1;

        return mDataset.size();
    }

}
