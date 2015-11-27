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
    xmlns:h="http://www.w3.org/1999/xhtml"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xpil="http://www.together.at/2006/XPIL1.0"
    xmlns:xpdl="http://www.wfmc.org/2002/XPDL1.0"
    exclude-result-prefixes="xsl xpil xpdl h xsd"
    version="1.0">
    <xsl:import href="controls.xsl"/>
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
    
    <xsl:template match="/xpil:WorkflowProcessInstances">
        <html xml:lang="ru">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <title>
                    Список процессов
                </title>
                <xsl:call-template name="comon_css_and_js_includes">
                    <xsl:with-param name="webroot.path" select="$webroot.path"/>
                </xsl:call-template>
                <script src="{$webroot.path}/js/workflow/web/process.js" type="application/javascript">
                    <xsl:comment/>
                </script>

            </head>
            <xsl:variable name="xpilxsd" select="document('../../../../schemas/workflow/xpil.xsd')"/>
            <body>
                <table  class="pure-table pure-table-horizontal pure-table-striped">
                    <caption>Список процессов</caption>
                    <tr>
                        <th>Идентификатор</th>
                        <th>Наименование</th>
                        <th>Статус</th>
                        <th>Создан</th>
                        <th>Начат</th>
                        <th>Завершен</th>
                    </tr>            
                    <xsl:for-each select="xpil:*">                
                        <tr>
                            <td>
                                <xsl:value-of select="@Id"/>
                            </td>
                            <td>
                                <xsl:value-of select="@Name"/>
                            </td>
                            <td>
                                <xsl:variable name="state" select="@State"/>
                                <xsl:variable name="stateDesc" select="$xpilxsd/xsd:schema/xsd:complexType[@name = 'ExecutionInstance']/xsd:attribute[@name = 'State']/xsd:simpleType/xsd:restriction[@base = 'xsd:NMTOKEN']/xsd:enumeration[@value = $state]/xsd:annotation/xsd:appinfo/h:label"/>
                                <xsl:value-of select="$stateDesc"/>                                
                            </td>
                            <td>
                                <xsl:value-of select="@Created"/>
                            </td>
                            <td>
                                <xsl:value-of select="@Started"/>
                            </td>
                            <td>
                                <xsl:value-of select="@Finished"/>
                            </td>
                        </tr>

                    </xsl:for-each>
                </table>

            </body>
        </html>
    </xsl:template>


</xsl:stylesheet>
