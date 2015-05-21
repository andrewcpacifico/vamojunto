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

package co.vamojunto.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for User -> Company relation.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.3.0
 * @version 1.0.0
 */
@ParseClassName("UserCompany")
public class UserCompany extends ParseObject {

    public static final String FIELD_USER = "user";
    public static final String FIELD_COMPANY = "company";
    public static final String FIELD_STATUS = "status";

    public enum Status {
        APPROVED(1), WAITING(0), REJECTED(-1);

        private int mCode;

        private static Map<Integer, Status> mMap = new HashMap<>();

        static {
            mMap.put(1, APPROVED);
            mMap.put(-1, REJECTED);
            mMap.put(0, WAITING);
        }

        Status(int code) {
            mCode = code;
        }

        public static Status valueOf(int code) {
            return mMap.get(code);
        }

        public int getValue() {
            return mCode;
        }
    }

    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_REJECTED = -1;

    public User getUser() {
        return (User) getParseUser(FIELD_USER);
    }

    public void setUser(User user) {
        put(FIELD_USER, user);
    }

    public Company getCompany() {
        return (Company) getParseObject(FIELD_COMPANY);
    }

    public void setCompany(Company company) {
        put(FIELD_COMPANY, company);
    }

    public Status getStatus() {
        return Status.valueOf(getInt(FIELD_STATUS));
    }

}
