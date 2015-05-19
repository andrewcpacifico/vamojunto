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

/**
 * Created by Andrew C. Pacifico <andrewcpacifico@gmail.com> on 18/05/15.
 */
@ParseClassName("UserCompany")
public class UserCompany extends ParseObject {

    public static final String FIELD_USER = "user";
    public static final String FIELD_COMPANY = "company";

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

}
