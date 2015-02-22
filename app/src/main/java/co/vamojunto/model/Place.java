/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.model;

/**
 * Modelo de um local geográfico.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class Place {
    private String mTitulo;
    private String mEndereco;

    public Place(String titulo, String endereco) {
        this.mTitulo = titulo;
        this.mEndereco = endereco;
    }

    public String getTitulo() {
        return mTitulo;
    }

    public void setTitulo(String titulo) {
        this.mTitulo = titulo;
    }

    public String getmEndereco() {
        return mEndereco;
    }

    public void setmEndereco(String mEndereco) {
        this.mEndereco = mEndereco;
    }
}
