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
    exclude-result-prefixes="xsl xpil xpdl"
    extension-element-prefixes="str"
    version="1.0">

    <xsl:strip-space elements="*" />

    <xsl:template match="xpil:*" mode="controlLabel">
        <xsl:variable name="i">
            <xsl:value-of select="@Id"/>
        </xsl:variable>
        <xsl:variable name="n">
            <xsl:value-of select="xpdl:DataField/@Name"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$n!=''">
                <xsl:value-of select="$n"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$i"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xpil:*" mode="controlGroup">
        <xsl:variable name="localname" select="local-name(.)"/>
        <xsl:variable name="how_to_handle_marker" select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value"/>
        <xsl:if test="$how_to_handle_marker!=-1">
            <div class="pure-control-group">
                <label>
                    <xsl:apply-templates select="." mode="controlLabel"/>
                </label>
                <xsl:apply-templates select="." mode="control">
                    <xsl:with-param name="position" select="count(preceding-sibling::*[local-name()=$localname and xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=$how_to_handle_marker])"/>
                </xsl:apply-templates>
            </div>
        </xsl:if>
        <xsl:if test="not ($how_to_handle_marker)">
            <div class="pure-control-group">
                <label>
                    <xsl:apply-templates select="." mode="controlLabel"/>
                </label>
                <xsl:apply-templates select="." mode="control">
                    <xsl:with-param name="position" select="count(preceding-sibling::*[local-name()=$localname])"/>
                </xsl:apply-templates>
            </div>
        </xsl:if>

    </xsl:template>
    
    <xsl:template match="xpil:*" mode="controlToolTip">
        <xsl:variable name="localname" select="local-name(.)"/>
        <xsl:variable name="how_to_handle_marker" select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value"/>
        <xsl:if test="$how_to_handle_marker!=-1">
            <div class="pure-control-group pure-u tooltip instance {local-name()} {@Id}">
                <xsl:apply-templates select="." mode="control">
                    <xsl:with-param name="position" select="count(preceding-sibling::*[local-name()=$localname and xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=$how_to_handle_marker])"/>
                </xsl:apply-templates>
            </div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="xpil:DateDataInstance" mode="control">
        <xsl:param name="position"/>
        <xsl:choose>
            <!-- VariableToProcess_UPDATE -->
            <xsl:when test="not (xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']) or xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=1">
                <input type="hidden" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Id]" value="{@Id}"/>
                <input is="dynarch-calendar" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]" value="{@Value}" lang="ru"/>
            </xsl:when>
            <!-- VariableToProcess_VIEW -->
            <xsl:when test="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=0">
                <span>
                    <xsl:attribute name="title">
                        <xsl:apply-templates select="." mode="controlLabel"/>
                    </xsl:attribute>
                    <xsl:value-of select="@Value"/>
                </span>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xpil:DateTimeDataInstance" mode="control">
        <xsl:param name="position"/>
        <xsl:choose>
            <!-- VariableToProcess_UPDATE -->
            <xsl:when test="not (xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']) or xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=1">
                <input type="hidden" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Id]" value="{@Id}"/>
                <input name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]" value="{@Value}" is="dynarch-calendar" shows-time="" if-format="%Y-%m-%dT%H:%M:%S"/>
            </xsl:when>
            <!-- VariableToProcess_VIEW -->
            <xsl:when test="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=0">
                <span>
                    <xsl:attribute name="title">
                        <xsl:apply-templates select="." mode="controlLabel"/>
                    </xsl:attribute>
                    <xsl:value-of select="@Value"/>
                </span>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xpil:StringArrayDataInstance" mode="control">
        <xsl:param name="position"/>
        <input type="hidden" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Id]" value="{@Id}"/>
        <xsl:choose>
            <!-- VariableToProcess_UPDATE -->
            <xsl:when test="not (xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']) or xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=1">
                <button class="pure-button add-element" type="button">+</button>
                <ol>
                    <xsl:for-each select="xpil:StringValue">
                        <li>
                            <input type="text" name="{local-name(../../..)}[DataInstances][{local-name(..)}][{$position}][{local-name(.)}][{position()-1}][@Value]" value="{@Value}"  class="pure-input-1-2"/>
                            <button class="pure-button remove-element" type="button" >-</button>
                        </li>
                    </xsl:for-each>
                </ol>
            </xsl:when>
            <!-- VariableToProcess_VIEW -->
            <xsl:when test="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=0">
                <ol>
                    <xsl:for-each select="xpil:StringValue">
                        <li>
                            <xsl:value-of select="@Value"/>
                        </li>
                    </xsl:for-each>
                </ol>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="xpil:BooleanDataInstance" mode="control">
        <xsl:param name="position"/>
        <input type="hidden" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Id]" value="{@Id}"/>
        <select name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]">
            <xsl:if test="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=0">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <option value="">
                <xsl:if test="@Value=''">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:text>--</xsl:text>
            </option>
            <option value="false">
                <xsl:if test="@Value='false'">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:text>Нет</xsl:text>
            </option>
            <option value="true">
                <xsl:if test="@Value='true'">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:text>Да</xsl:text>
            </option>
        </select>
    </xsl:template>

    <xsl:template match="xpil:StringDataInstance | xpil:DoubleDataInstance | xpil:LongDataInstance | xpil:TimeDataInstance" mode="control">
        <xsl:param name="position" />
        <xsl:choose>
            <!-- VariableToProcess_UPDATE -->
            <xsl:when test="not (xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']) or xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=1">
                <input type="hidden" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Id]" value="{@Id}"/>
                <xsl:choose>
                    <!-- LARGE SIZE -->
                    <xsl:when test="contains(xpdl:DataField/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[@Name='RENDERING_HINT']/@Value,'Size=L') or local-name()='SchemaDataInstance'">
                        <textarea name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]">
                            <xsl:attribute name="title">
                                <xsl:apply-templates select="." mode="controlLabel"/>
                            </xsl:attribute>

                            <xsl:value-of select="@Value"/>
                        </textarea>
                    </xsl:when>
                    <xsl:when test="xpdl:DataField/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[@Name='ENUM']">
                        <select name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]">
                            <xsl:attribute name="title">
                                <xsl:apply-templates select="." mode="controlLabel"/>
                            </xsl:attribute>

                            <xsl:call-template name="tokenizedOptions">
                                <xsl:with-param name="enumValues" select="xpdl:DataField/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[@Name='ENUM']/@Value"/>
                                <xsl:with-param name="value" select="@Value"/>
                            </xsl:call-template>
                        </select>
                    </xsl:when>
                    <xsl:otherwise>
                        <input name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]" value="{@Value}" class="pure-input-1-2">
                            <xsl:for-each select="xpdl:DataField/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[starts-with(@Name,'WORKFLOW_INPUT_')]">
                                <xsl:attribute name="{translate(substring-after(@Name,'WORKFLOW_INPUT_'),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')}">
                                    <xsl:value-of select="@Value"/>
                                </xsl:attribute>
                            </xsl:for-each>
                        </input>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!-- VariableToProcess_VIEW -->
            <xsl:when test="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=0">
                <span>
                    <xsl:attribute name="title">
                        <xsl:apply-templates select="." mode="controlLabel"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="xpdl:DataField/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[@Name='ENUM']">
                            <xsl:call-template name="tokenizedValue">
                                <xsl:with-param name="enumValues" select="xpdl:DataField/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[@Name='ENUM']/@Value"/>
                                <xsl:with-param name="value" select="@Value"/>
                            </xsl:call-template>                            
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@Value"/>
                        </xsl:otherwise>
                    </xsl:choose>                    
                </span>
            </xsl:when>
            <!-- VariableToProcess_FETCH -->
            <xsl:when test="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='how_to_handle_marker']/@Value=2">
                <!-- determine source variable Id -->
                <xsl:variable name="variableId" select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='fetch_marker']/@Value" />
                <xsl:variable name="variableIdKeys" select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[@Name='fetch_marker_keys']/@Value" />
                <!-- fetch element with id -->
                <xsl:variable name="variableElement" select="../xpil:*[@Id=$variableId]" />
                <input type="hidden" name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Id]" value="{@Id}"/>
                <xsl:choose>
                    <xsl:when test="local-name($variableElement)='StringDataInstance'">
                        <!-- fetch value for specified id -->
                        <xsl:variable name="variableValue" select="$variableElement/@Value" />
                        <select name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]">
                            <xsl:attribute name="title">
                                <xsl:apply-templates select="." mode="controlLabel"/>
                            </xsl:attribute>
                            <xsl:call-template name="tokenizedOptions">
                                <xsl:with-param name="enumValues" select="$variableValue"/>
                                <xsl:with-param name="value" select="@Value"/>
                            </xsl:call-template>
                        </select>
                    </xsl:when>
                    <xsl:when test="local-name($variableElement)='StringArrayDataInstance'">
                        <!-- fetch value for specified id -->
                        <xsl:variable name="variableValues" select="$variableElement/xpil:StringValue/@Value" />
                        <xsl:variable name="variableKeys" select="../xpil:*[@Id=$variableIdKeys]/xpil:StringValue/@Value" />

                        <select name="{local-name(../..)}[DataInstances][{local-name(.)}][{$position}][@Value]">
                            <xsl:attribute name="title">
                                <xsl:apply-templates select="." mode="controlLabel"/>
                            </xsl:attribute>
                            <xsl:call-template name="arrayOptions">
                                <xsl:with-param name="enumValues" select="$variableValues"/>
                                <xsl:with-param name="enumKeys" select="$variableKeys"/>
                                <xsl:with-param name="value" select="@Value"/>
                            </xsl:call-template>
                        </select>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="tokenizedOptions">
        <xsl:param name="enumValues"/>
        <xsl:param name="value"/>
        <xsl:for-each select="str:tokenize($enumValues, '&amp;')">
            <xsl:choose>
                <xsl:when test="contains(.,'=')">
                    <option value="{substring-before(.,'=')}">
                        <xsl:if test="$value=substring-before(.,'=')">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="substring-after(.,'=')"/>
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option>
                        <xsl:if test="$value=.">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="."/>
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="tokenizedValue">
        <xsl:param name="enumValues"/>
        <xsl:param name="value"/>
        <xsl:for-each select="str:tokenize($enumValues, '&amp;')">
            <xsl:if test="contains(.,'=')">
                    <xsl:if test="$value=substring-before(.,'=')">
                        <xsl:value-of select="substring-after(.,'=')"/>
                    </xsl:if>                    
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="arrayOptions">
        <xsl:param name="enumValues"/>
        <xsl:param name="enumKeys"/>
        <xsl:param name="value"/>
        <xsl:for-each select="$enumValues">
            <xsl:variable name="pos" select="position()"/>
            <xsl:variable name="key" select="$enumKeys[position()=$pos]"/>
            <option value="{$key}">
                <xsl:if test="$value=$key">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="."/>
            </option>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
