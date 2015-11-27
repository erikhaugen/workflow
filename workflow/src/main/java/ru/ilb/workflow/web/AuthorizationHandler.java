/**
 * Copyright (C) 2015 Bystrobank, JSC
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses
 */
package ru.ilb.workflow.web;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author slavb
 */
@Provider
public class AuthorizationHandler implements ContainerRequestFilter {

    private static final ThreadLocal authorisedUser = new ThreadLocal();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String userName = requestContext.getSecurityContext().getUserPrincipal().getName();
        String xremoteUserName = requestContext.getHeaderString("X-Remote-User");
        if (xremoteUserName == null) {
            MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();
            xremoteUserName = queryParams.getFirst("x-remote-user");
        }
        //TODO ограничить X-Remote-User
        authorisedUser.set(xremoteUserName != null ? xremoteUserName : userName);
    }

    public static String getAuthorisedUser() {
        return (String) authorisedUser.get();
    }

}
