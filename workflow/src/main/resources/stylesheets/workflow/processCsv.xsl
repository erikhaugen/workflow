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
    xmlns:str="http://exslt.org/strings"
    exclude-result-prefixes="xsl xpil xpdl str"
    extension-element-prefixes="str"

    version="1.0">
    <xsl:output
        media-type="text/csv"
        method="text"
        indent="no"
        encoding="UTF-8" />

    <xsl:strip-space elements="*" />
    <xsl:param name="DataInstances"/>
    <xsl:param name="csvseparator" select="';'"/>
    <xsl:param name="csvlineseparator" select="'&#10;'"/>

    <xsl:template match="/">
        <xsl:value-of select="concat('Id',$csvseparator,'Started',$csvseparator,'Finished')"/>
        <xsl:for-each select="str:tokenize($DataInstances, ',')">
            <xsl:value-of select="concat($csvseparator,.)"/>
        </xsl:for-each>
        <xsl:value-of select="concat($csvseparator,'ActivityCreated',$csvseparator,'ActivityAssigned')"/>
        <xsl:value-of select="$csvlineseparator"/>
        <xsl:apply-templates select="/xpil:WorkflowProcessInstances/xpil:*"/>
    </xsl:template>

    <xsl:template match="xpil:MainWorkflowProcessInstance|xpil:SubWorkflowProcessInstance">
        <xsl:variable name="pr" select="."/>
        <xsl:variable name="xpath" select="xpil:DataInstances/xpil:*[1]/@Value"/>
        <xsl:value-of select="concat($pr/@Id,$csvseparator,$pr/@Started,$csvseparator,$pr/@Finished)"/>
        <xsl:for-each select="str:tokenize($DataInstances, ',')">
            <xsl:variable name="varName" select="."/>
            <xsl:value-of select="concat($csvseparator,$pr/xpil:DataInstances/xpil:*[@Id=$varName]/@Value)"/>
        </xsl:for-each>
        <xsl:for-each select="$pr/xpil:ActivityInstances/xpil:ManualActivityInstance[@DefinitionId='outgoingcall_session']">
            <xsl:value-of select="concat($csvseparator,@Created,$csvseparator)"/>
            <xsl:for-each select="xpil:AssignmentInstances/xpil:AssignmentInstance">
                <xsl:if test="position() &gt; 1">,</xsl:if>
                <xsl:value-of select="@Username"/>
                <xsl:if test="@IsAccepted='true'">*</xsl:if>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:value-of select="$csvlineseparator"/>
    </xsl:template>
</xsl:stylesheet>
