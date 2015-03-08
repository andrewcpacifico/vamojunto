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
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.Ride;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Used to fill the {@link android.support.v7.widget.RecyclerView} of a ride list screen. These
 * lists are displayed on the user's rides administration screen, and in the groups walls.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class ListRidesRecyclerViewAdapter extends RecyclerView.Adapter<ListRidesRecyclerViewAdapter.ViewHolder> {

    /** Contém os dados que serão exibidos na lista. */
    private List<Ride> mDataset;

    private Context mContext;

    /** Utilizado para executar algo na Thread principal */
    private Handler mHandler;

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mImgMotoristaImageView;
        public TextView mNomeMotoristaTextView;
        public TextView mOrigemTextView;
        public TextView mDestinoTextView;
        public TextView mDataTextView;
        public TextView mHoraTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mImgMotoristaImageView = (CircleImageView) itemView.findViewById(R.id.user_pic);
            mNomeMotoristaTextView = (TextView) itemView.findViewById(R.id.user_name_text_view);
            mOrigemTextView = (TextView) itemView.findViewById(R.id.starting_point_text_view);
            mDestinoTextView = (TextView) itemView.findViewById(R.id.destination_text_view);
            mDataTextView = (TextView) itemView.findViewById(R.id.date_text_view);
            mHoraTextView = (TextView) itemView.findViewById(R.id.time_text_view);
        }
    }

    /**
     * Construtor da classe, inicializa os campos necessários para que a lista seja preenchida.
     */
    public ListRidesRecyclerViewAdapter(Context context, List<Ride> dataset) {
        this.mDataset = dataset;
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Atualiza o dataset do RecyclerView
     * @param dataset O novo dataset.
     */
    public void setDataset(List<Ride> dataset) {
        this.mDataset = dataset;

        // Notifica a mudança no dataset, para que a atualização visual seja imediata, esta ação
        // deve ser executada na Thread principal, por isso a utilização do handler
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Permite a adição de um item ao dataset, em seguida notifica a adição de um novo item, para
     * que o RecyclerView seja atualizado.
     * @param c A carona a ser adicionada ao dataset, e consequentemente à tela.
     */
    public void addItem(Ride c) {
        // O item novo é adicionado à primeira posição do dataset, para que seja exibido no topo.
        this.mDataset.add(0, c);

        // Mesma situação da alteração de dataset, o código precisa ser executado na Thread principal
        // para ter efeito imediato.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(0);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_ride_card, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ride c = mDataset.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mContext.getString(R.string.date_format));
        SimpleDateFormat timeFormat = new SimpleDateFormat(mContext.getString(R.string.time_format));

        holder.mNomeMotoristaTextView.setText(c.getDriver().getName());
        holder.mImgMotoristaImageView.setImageBitmap(c.getDriver().getProfileImage());
        holder.mOrigemTextView.setText(mContext.getString(R.string.de) + ": " + c.getStartingPoint().getTitulo());
        holder.mDestinoTextView.setText(mContext.getString(R.string.para) + ": " + c.getDestination().getTitulo());
        holder.mDataTextView.setText(dateFormat.format(c.getDatetime().getTime()));
        holder.mHoraTextView.setText(timeFormat.format(c.getDatetime().getTime()));
    }

    @Override
    public int getItemCount() {
        return this.mDataset.size();
    }
}
