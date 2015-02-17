/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    /** Array de Strings contendo os títulos de cada um dos itens exibidos no menu */
    private String mNavTitles[];
    /** Array de inteiros, contendos os ids dos ícones que serão exibidos em cada um dos itens do menu */
    private int mIcons[];

    /** Nome do usuário autenticado no sistema para ser exibido no cabeçalho */
    private String mNomeUsuario;
    /** Bitmap da imagem de perfil do usuário */
    private Bitmap mImgUsuario;
    /** Email do usuário autenticado para ser exibido no cabeçalho */
    private String mEmailUsuario;

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
        public ViewHolder(View itemView, int viewType) {
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
        }
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
    public NavigationDrawerAdapter(Context context, String nomeUsuario, String emailUsuario, Bitmap imgUsuario) {
        /* Inicializa os dados do usuário */
        mNomeUsuario = nomeUsuario;
        mEmailUsuario = emailUsuario;
        mImgUsuario = imgUsuario;

        /* Inicializa os dados dos itens do menu */
        mNavTitles = context.getResources().getStringArray(R.array.nav_drawer_items);
    }

    /**
     * Sobrescrita do método onCreateViewHolder que é chamado quando o ViewHolder é criado.
     *
     * Neste médodo o layout drawer_list_row.xml é inflado se o viewType for igual a Type_ITEM, ou então
     * o layout drawer_header.xml é inflado caso o viewType seja igual a TYPE_HEADER. Então o View
     * carregado é passado para o ViewHolder.
     *
     * @param parent
     * @param viewType Define o tipo de View que está sendo criado.
     *
     * @return O ViewHolder criado no método, ou null em caso de erro.
     */
    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.drawer_list_row, parent, false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created
        }
        return null;

    }

    /**
     * Sobrescrita do método onBindViewHolder, que é chamado quando um item em uma linha vai ser exibido,
     *
     * @param holder ViewHolder a ser vinculado à informação que será exibida.
     * @param position Posição do item que será exibido.
     */
    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder holder, int position) {
        // Verifica se o tipo do holder que vai ser vinculado é um item, ou cabeçalho.
        if (holder.mHolderType == TYPE_ITEM) {
            holder.mTituloView.setText(mNavTitles[position - 1]);
            holder.mImagemView.setImageResource(R.drawable.ic_launcher);
        } else {
            holder.mImgUsuarioView.setImageBitmap(mImgUsuario);
            holder.mNomeUsuarioView.setText(mNomeUsuario);
            holder.mEmailView.setText(mEmailUsuario);
        }
    }

    /**
     * Utilizado para saber a quantidade de itens existentes na lista. O tamanho do array de títulos
     * representa o total de itens de menu, então o total de itens é igual ao tamanho + 1, por conta
     * do cabeçalho.
     *
     * @return A quantidade de itens da lista.
     */
    @Override
    public int getItemCount() {
        return mNavTitles.length + 1;
    }

    /**
     * Indica o tipo de View, do item que será exibido na lista. Neste caso o único diferente é o
     * primeiro item da lista (posição 0) que contém o cabeçalho.
     *
     * @param position Posição do item na lista.
     * @return O ViewType correspondente.
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

}
