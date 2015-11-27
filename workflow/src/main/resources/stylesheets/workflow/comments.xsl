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
    <xsl:import href="controls.xsl"/>
    <xsl:output
        media-type="application/xhtml+xml"
        method="xml"
        encoding="UTF-8"
        indent="yes"
        omit-xml-declaration="yes"
    />

    <xsl:strip-space elements="*" />
    <xsl:param name="base.path"/>
    <xsl:param name="webroot.path" select="substring-before($base.path,'/web/')"/>

    <xsl:template match="/xpil:MainWorkflowProcessInstance">
        <xsl:apply-templates select="." mode="commentsTable"/>
    </xsl:template>
    
    <xsl:template match="xpil:MainWorkflowProcessInstance" mode="commentsTable">
        <xsl:variable name="pr" select="."/>
        <div>
            <form id="commentsForm" action="{$webroot.path}/web/processes/{$pr/@Id}/comments" enctype="application/json" is="form-ajax" method="post" class="pure-form pure-form-aligned">
                <input type="hidden" name="StringValue[@xmlns][$]" value="http://www.together.at/2006/XPIL1.0"/>
                <input name="StringValue[@Value]" value="" class="pure-input-1-2"/>
                <button type="submit" class="pure-button">Добавить комментарий</button>
            </form>
            <table  class="pure-table pure-table-horizontal pure-table-striped">
                <caption>Комментарии к процессу</caption>
                <tr>
                    <td>Дата/Время</td>
                    <td>Пользователь</td>
                    <td>Комментарий</td>
                </tr>
                <xsl:for-each select="$pr//xpil:DataInstances/xpil:StringArrayDataInstance[@Id='comments']/xpil:StringValue">
                    <xsl:sort select="position()" data-type="number" order="descending"/>
                    <xsl:variable name="com" select="@Value"/>
                    <xsl:variable name="date" select="substring(substring-before($com,']'),2)"/>
                    <xsl:variable name="tail" select="substring-after($com,'] ')"/>
                    <xsl:variable name="author" select="substring-before($tail,' ')"/>
                    <xsl:variable name="comment" select="substring-after($tail,' ')"/>
                    <tr>
                        <td>
                            <xsl:value-of select="$date"/>
                        </td>
                        <td>
                            <xsl:value-of select="$author"/>
                        </td>
                        <td>
                            <xsl:value-of select="$comment"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

</xsl:stylesheet>
