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
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.vamojunto.R;

/**
 * Adapter utilizado para preencher os dados do NavigationDrawer
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    /** Utilizado para indicar que o View que está sendo construído pelo RecyclerView é o cabeçalho */
    private static final int TYPE_HEADER = 0;
    /** Utilizado para indicar que o View que está sendo construído pelo RecyclerView é um item da lista */
    private static final int TYPE_ITEM = 1;

    /**
     * The number of items on navigation drawer menu.
     *
     * @since 0.1.0
     */
    private static final int MENU_ITEM_COUNT = 2;

    /** Array de Strings contendo os títulos de cada um dos itens exibidos no menu */
    private String mNavTitles[];

    /**
     * Array containing the icons to display on each item of the navigation drawer
     *
     * @since 0.1.0
     */
    private static int mIcons[] = {
            R.drawable.ic_ride_hand,
            R.drawable.ic_menu_friends
    };

    /** Nome do usuário autenticado no sistema para ser exibido no cabeçalho */
    private String mNomeUsuario;
    /** Bitmap da imagem de perfil do usuário */
    private Bitmap mImgUsuario;
    /** Email do usuário autenticado para ser exibido no cabeçalho */
    private String mEmailUsuario;

    private OnItemClickListener mItemClickListener;

    /**
     * Sobrescrita do {@link android.support.v7.widget.RecyclerView.ViewHolder}
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private int mHolderType;

        // Views dos itens do menu
        public TextView mTituloView;
        public ImageView mImagemView;

        // Views do cabeçalho
        public ImageView mImgUsuarioView;
        public TextView mNomeUsuarioView;
        public TextView mEmailView;

        /**
         * Construtor do ViewHolder, carrega os Views correspondentes, de acordo com o tipo passado
         * como parâmetro (item ou cabeçalho).
         *
         * @param itemView Layout inflado
         * @param viewType Tipo de ViewHolder a ser carregado (Item de menu ou cabeçalho).
         */
        public ViewHolder(View itemView, int viewType, final OnItemClickListener clickListener) {
            super(itemView);

            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created

            // Carrega os views apropriados, de acordo com o tipo de view passado quando o ViewHolder
            // foi criado.
            if (viewType == TYPE_ITEM) {
                mTituloView = (TextView) itemView.findViewById(R.id.rowText);
                mImagemView = (ImageView) itemView.findViewById(R.id.rowIcon);
                mHolderType = TYPE_ITEM;
            } else {
                mNomeUsuarioView = (TextView) itemView.findViewById(R.id.name);
                mEmailView = (TextView) itemView.findViewById(R.id.email);
                mImgUsuarioView = (ImageView) itemView.findViewById(R.id.circleView);
                mHolderType = TYPE_HEADER;
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.OnItemClick(v, getPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        public void OnItemClick(View v, int position);
    }


    /**
     * Construtor da classe NavigationDrawerAdapter, inicializa os dados do usuário com os valores
     * informados por parâmetro. Inicializa os títulos e ícones de cada um dos itens do menu
     * com valores pré-definidos.
     *
     * @param nomeUsuario Nome do usuário que está autenticado para ser exibido no cabeçalho do Navigation mDrawerLayout
     * @param emailUsuario Email do usuário que está autenticado para ser exibido no cabeçalho do Navigation mDrawerLayout
     * @param imgUsuario Bitmap da imagem de perfil do usuário que está autenticado para ser exibido no cabeçalho do Navigation mDrawerLayout
     */
    public NavigationDrawerAdapter(Context context, String nomeUsuario, String emailUsuario, Bitmap imgUsuario,
                                   NavigationDrawerAdapter.OnItemClickListener clickListener) {
        /* Inicializa os dados do usuário */
        mNomeUsuario = nomeUsuario;
        mEmailUsuario = emailUsuario;
        mImgUsuario = imgUsuario;

        /* initialize the menu data */
        mNavTitles = context.getResources().getStringArray(R.array.nav_drawer_items);

        mItemClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.drawer_list_row, parent, false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v, viewType, mItemClickListener); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v, viewType, mItemClickListener); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created
        }
        return null;

    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder holder, int position) {
        // Verifica se o tipo do holder que vai ser vinculado é um item, ou cabeçalho.
        if (holder.mHolderType == TYPE_ITEM) {
            holder.mTituloView.setText(mNavTitles[position - 1]);
            holder.mImagemView.setImageResource(mIcons[position - 1]);
        } else {
            // Caso o usuário tenha se inscrito direto pelo aplicativo, e não tenha feito upload
            // de uma imagem de perfil.
            if ( mImgUsuario != null )
                holder.mImgUsuarioView.setImageBitmap(mImgUsuario);

            holder.mNomeUsuarioView.setText(mNomeUsuario);
            holder.mEmailView.setText(mEmailUsuario);
        }
    }

    @Override
    public int getItemCount() {
        return MENU_ITEM_COUNT + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

}
