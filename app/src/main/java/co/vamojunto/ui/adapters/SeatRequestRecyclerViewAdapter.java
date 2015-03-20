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

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.vamojunto.R;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for the RecyclerView on the {@link co.vamojunto.ui.fragments.SeatRequestsFragment}
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class SeatRequestRecyclerViewAdapter extends RecyclerView.Adapter<SeatRequestRecyclerViewAdapter.ViewHolder> {

    /**
     * The ViewHolder for the items on the {@link co.vamojunto.ui.fragments.SeatRequestsFragment}.
     * Each item should display the requester name, picture, and a message. And gives the user
     * the choice to approve or reject the request.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @version 1.0.0
     * @since 0.1.0
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * The View that holds the requester picture
         */
        private CircleImageView userImage;

        /**
         * The View that holds the requester name
         */
        private TextView userName;

        /**
         * The View that holds the message sent by the requester, on the requesting moment.
         */
        private TextView message;

        /**
         * Class constructor
         *
         * @param itemView The root layout for the ViewHolder
         * @param userName The requester name
         * @param message The message sent by requester
         * @param userImage The requester image
         */
//        public ViewHolder(View itemView, String userName, String message, Bitmap userImage) {
//            super(itemView);
//        }

          public ViewHolder(View itemView) {
              super(itemView);
          }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // creates the rootView for the item
        View rootItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_seat_request, parent, false);

        return new ViewHolder(rootItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 20;
    }

}
