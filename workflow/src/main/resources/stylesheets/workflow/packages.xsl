<?xml version="1.0" encoding="UTF-8"?>
<!--
    Copyright (C) 2015 Bystrobank, JSC

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xpil="http://www.together.at/2006/XPIL1.0"
    xmlns:xpdl="http://www.wfmc.org/2002/XPDL1.0"
    exclude-result-prefixes="xsl xpil xpdl"
    version="1.0">

    <xsl:output
        media-type="application/xhtml+xml"
        method="xml"
        encoding="UTF-8"
        indent="yes"
        omit-xml-declaration="no"
        doctype-public="-//W3C//DTD XHTML 1.1//EN"
        doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" />

    <xsl:include href="common.xsl"/>

    <xsl:strip-space elements="*" />
    <xsl:param name="base.path"/>
    <xsl:param name="webroot.path" select="substring-before($base.path,'/web/')"/>

    <xsl:param name="state"/>
    <xsl:param name="assignment"/>
    <xsl:param name="DataInstances.clientUUID"/>

    <xsl:template match="/xpil:PackageInstances">
        <html xml:lang="ru">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <title>
                    <xsl:value-of select="'Пакеты'"/>
                </title>
                <xsl:call-template name="comon_css_and_js_includes">
                    <xsl:with-param name="webroot.path" select="$webroot.path"/>
                </xsl:call-template>
            </head>
            <body>
                <form id="packagesForm" action="{$webroot.path}/web/processes" enctype="application/json" is="form-ajax" method="post">
                    <input type="hidden" name="MainWorkflowProcessInstance[@xmlns][$]" value="http://www.together.at/2006/XPIL1.0"/>
                    <table  class="pure-table pure-table-horizontal pure-table-striped">
                        <caption>
                            <xsl:value-of select="'Пакеты'"/>
                        </caption>
                        <tr>
                            <th>Пакет</th>
                            <th>Версия</th>
                            <th>Загружен</th>
                            <th>Процесс</th>
                            <th>Действия</th>
                        </tr>

                        <xsl:for-each select=".//xpil:WorkflowProcessFactoryInstance">
                            <xsl:variable name="fac" select="."/>
                            <xsl:variable name="pkg" select="../.."/>
                            <tr>
                                <td>
                                    <xsl:value-of select="$pkg/@Id"/>
                                </td>
                                <td>
                                    <xsl:value-of select="$pkg/@Created"/>
                                </td>
                                <td>
                                    <xsl:value-of select="$pkg/@Version"/>
                                </td>
                                <td>
                                    <xsl:value-of select="$fac/@Id"/>
                                </td>
                                <td>
                                    <button type="submit" name="MainWorkflowProcessInstance[@FactoryId]" value="{$fac/@Id}">Запустить</button>
                                </td>

                            </tr>
                        </xsl:for-each>
                    </table>
                </form>
                <script src="{$webroot.path}/js/workflow/web/packages.js" type="application/javascript">
                    <xsl:comment/>
                </script>

            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
