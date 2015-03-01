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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;

import java.text.SimpleDateFormat;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.Carona;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter utilizado para popular uma lista de itens de caronas, essas listas
 * são exibidas na tela de administração das caronas do usuário, e nos feeds dos grupos.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class ListaCaronasRecyclerViewAdapter extends RecyclerView.Adapter<ListaCaronasRecyclerViewAdapter.ViewHolder> {

    /** Contém os dados que serão exibidos na lista. */
    private List<Carona> mDataset;

    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mImgMotoristaImageView;
        public TextView mNomeMotoristaTextView;
        public TextView mOrigemTextView;
        public TextView mDestinoTextView;
        public TextView mDataTextView;
        public TextView mHoraTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mImgMotoristaImageView = (CircleImageView) itemView.findViewById(R.id.img_motorista);
            mNomeMotoristaTextView = (TextView) itemView.findViewById(R.id.nome_motorista_text_view);
            mOrigemTextView = (TextView) itemView.findViewById(R.id.origem_text_view);
            mDestinoTextView = (TextView) itemView.findViewById(R.id.destino_text_view);
            mDataTextView = (TextView) itemView.findViewById(R.id.data_text_view);
            mHoraTextView = (TextView) itemView.findViewById(R.id.hora_text_view);
        }
    }

    /**
     * Construtor da classe, inicializa os campos necessários para que a lista seja preenchida.
     */
    public ListaCaronasRecyclerViewAdapter(Context context, List<Carona> dataset) {
        this.mDataset = dataset;
        this.mContext = context;
    }

    public void setDataset(List<Carona> dataset) {
        this.mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lista_caronas_list_row, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Carona c = mDataset.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mContext.getString(R.string.date_format));
        SimpleDateFormat timeFormat = new SimpleDateFormat(mContext.getString(R.string.time_format));

        holder.mNomeMotoristaTextView.setText(c.getMotorista().getNome());
        holder.mImgMotoristaImageView.setImageBitmap(c.getMotorista().getImage());
        holder.mOrigemTextView.setText(mContext.getString(R.string.de) + ": " + c.getOrigem().getTitulo());
        holder.mDestinoTextView.setText(mContext.getString(R.string.para) + ": " + c.getDestino().getTitulo());
        holder.mDataTextView.setText(dateFormat.format(c.getDataHora().getTime()));
        holder.mHoraTextView.setText(timeFormat.format(c.getDataHora().getTime()));
    }

    @Override
    public int getItemCount() {
        return this.mDataset.size();
    }
}
