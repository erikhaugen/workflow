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
 */package ru.ilb.workflow.toolagent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.enhydra.shark.admin.repositorymanagement.RepositoryManager;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.LoggerFactory;

/**
 *
 * @author slavb
 */
public class JSUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JSUtil.class);

    public static org.slf4j.Logger getLogger() {
        return logger;
    }

    public static void include(Scriptable scope, String path) throws IOException, Exception {
        String xpdlRepository = RepositoryManager.getInstance().getPathToXPDLRepositoryFolder();
        byte[] encoded = Files.readAllBytes(Paths.get(xpdlRepository, path));
        String source = new String(encoded, StandardCharsets.UTF_8);
        try {
            Context context = Context.enter();
            context.evaluateString(scope, source, path, 1, null);
        } finally {
            Context.exit();
        }
    }

}
