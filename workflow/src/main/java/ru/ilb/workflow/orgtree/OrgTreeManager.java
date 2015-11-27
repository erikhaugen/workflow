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
 */package ru.ilb.workflow.orgtree;

import at.together._2006.xpil1.User;
import at.together._2006.xpil1.Users;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 *
 * @author slavb
 */
@Component
public class OrgTreeManager {

    @Resource(mappedName = "ru.bystrobank.apps.orgtree.ws")
    String orgTreeUrl;

    @Resource(mappedName = "allOfficesGroup")
    String allOfficesGroup;

    @Cacheable("orgtree")
    public List<String> getAllOrgunitMembers(String orgunitId) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(orgTreeUrl);
        target = target.path("getAllOrgunitMembers.php").queryParam("orgunitId", orgunitId);
        Invocation.Builder builder = target.request();
        //Response response = builder.get();
        Users users = builder.get(Users.class);
        List<String> usersList=new ArrayList<>();
        for(User u:users.getUsers()){
            usersList.add(u.getId());
        }
        return usersList;
    }

    public String getAllOfficesGroup() {
        return allOfficesGroup;
    }


}
