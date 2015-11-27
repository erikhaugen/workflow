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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import ru.ilb.workflow.utils.InitUtils;

/**
 *
 * @author slavb
 */
@WebListener
public class ServletInit implements ServletContextListener {
    //private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ServletInit.class);

    private static String contextPath;

    public static String getContextPath() {
        return contextPath;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        contextPath = sce.getServletContext().getRealPath("/");
        InitUtils.initializeEngine(contextPath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        InitUtils.stopEngine();
    }

}
