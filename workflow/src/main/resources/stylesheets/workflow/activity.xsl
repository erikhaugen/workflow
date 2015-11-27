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
        omit-xml-declaration="no"
        doctype-public="-//W3C//DTD XHTML 1.1//EN"
        doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" />
    <xsl:include href="common.xsl"/>
    <xsl:include href="comments.xsl"/>

    <xsl:strip-space elements="*" />
    <xsl:param name="base.path"/>
    <xsl:param name="webroot.path" select="substring-before($base.path,'/web/')"/>
    
    <xsl:template match="/">
        <html xml:lang="ru">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <title>
                    <xsl:value-of select="@Id"/>
                </title>
                <xsl:call-template name="comon_css_and_js_includes">
                    <xsl:with-param name="webroot.path" select="$webroot.path"/>
                </xsl:call-template>
            </head>
            <body>
                <xsl:apply-templates select="xpil:*" mode="form"/>
                <xsl:apply-templates select="xpil:*" mode="comments"/>
                <script src="{$webroot.path}/js/workflow/web/activity.js" type="application/javascript">
                    <xsl:comment/>
                </script>

            </body>
        </html>
    </xsl:template>
    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="form">
        <xsl:variable name="pr" select="."/>
        <xsl:variable name="ac" select="xpil:ActivityInstances/xpil:*"/>
        <form id="activityForm" action="{$webroot.path}/web/processes/{$pr/@Id}/activities/{$ac/@Id}" enctype="application/json" is="form-ajax" method="put" class="pure-form pure-form-aligned">
            <!-- <form action="{$ac/@Id}" xenctype="multipart/form-data" method="post" class="pure-form pure-form-aligned"> -->
            <fieldset>
                <input type="hidden" name="{local-name($ac)}[@xmlns][$]" value="http://www.together.at/2006/XPIL1.0"/>
                <!--
                <div class="pure-control-group">
                    <label>Название:</label>
                    <span>
                        <xsl:value-of select="$ac/@Name"/>
                    </span>
                </div>
                <div class="pure-control-group">
                    <label>Состояние:</label>
                    <span>
                        <xsl:value-of select="$ac/@State"/>
                    </span>
                </div>
                <div class="pure-control-group">
                    <label>Описание:</label>
                    <span>
                        <xsl:value-of select="$ac/xpil:InstanceDescription"/>
                    </span>
                </div>
                <div class="pure-control-group">
                    <label>Очередность:</label>
                    <span>
                        <xsl:value-of select="$ac/xpil:InstancePriority"/>
                    </span>
                </div>
                -->
                <xsl:variable name="authorisedUser" xmlns:auth="xalan://ru.ilb.workflow.web.AuthorizationHandler" select="auth:getAuthorisedUser()"/>
                <xsl:variable name="ai_accepted" select="$ac/xpil:AssignmentInstances/xpil:AssignmentInstance[@IsAccepted='true']"/>
                <xsl:choose>
                    <xsl:when test="count($ai_accepted) &gt; 0 and count($ai_accepted[@Username=$authorisedUser]) = 0">
                        Этап заблокирован пользователем <xsl:value-of select="$ai_accepted/@Username"/>
                        <br/>
                        <br/>
                        <button class="pure-button" name="{local-name($ac)}[@State]" value="open.not_running.not_started" type="submit">Разблокировать</button>
                    </xsl:when>
                    <xsl:when test="substring($ac/@State,1,4)!='open'">
                        Этап завершен, выполлнение работы невозможно
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="." mode="form_header"/>
                        <xsl:apply-templates select="." mode="form_controls"/>
                        <xsl:apply-templates select="." mode="form_buttons"/>
                        <xsl:apply-templates select="." mode="form_footer"/>
                    </xsl:otherwise>
                </xsl:choose>
            </fieldset>
        </form>
    </xsl:template>

    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="form_controls">
        <xsl:variable name="ac" select="xpil:ActivityInstances/xpil:*"/>
        <xsl:apply-templates select="$ac/xpil:DataInstances/xpil:*" mode="controlGroup"/>
    </xsl:template>
    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="form_buttons">
        <xsl:variable name="pr" select="."/>
        <xsl:variable name="ac" select="xpil:ActivityInstances/xpil:*"/>
        <button id="buttonSave" class="pure-button" type="submit">Сохранить</button>
        <button id="buttonCompleted" class="pure-button" name="{local-name($ac)}[@State]" value="closed.completed" type="submit">
            <xsl:variable name="enableComplete" select="//xpil:BooleanDataInstance[@Id='enableComplete']" />
            <xsl:if test="$enableComplete/@Value = 'false'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            Завершить
        </button>
        <xsl:choose>
            <xsl:when test="$ac/@State='open.not_running.not_started'">
                <button class="pure-button" name="{local-name($ac)}[@State]" value="open.running" type="submit">Блокировать</button>
            </xsl:when>
            <xsl:when test="$ac/@State='open.running'">
                <button class="pure-button" name="{local-name($ac)}[@State]" value="open.not_running.not_started" type="submit">Разблокировать</button>
            </xsl:when>
        </xsl:choose>
        
    </xsl:template>
    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="form_footer">
        
    </xsl:template>
    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="form_header">
        <xsl:variable name="pr" select="."/>
        <xsl:variable name="ac" select="xpil:ActivityInstances/xpil:*"/>
        <div class="pure-control-group">
            <label>
                Процесс: 
            </label>
            <span>
                <xsl:value-of select="$pr/@Name"/> / <xsl:value-of select="$ac/@Name"/>
            </span>
        </div>
    </xsl:template>

    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="comments">
        <xsl:variable name="pr" select="."/>
        <xsl:variable name="ac" select="xpil:ActivityInstances/xpil:*"/>
        <xsl:if test="$ac/xpdl:Activity/xpdl:ExtendedAttributes/xpdl:ExtendedAttribute[@Name='ENABLE_COMMENTS']/@Value='true'">
            <x-panel id="commentsPanel">
                <xsl:apply-templates select="." mode="commentsTable"/>
            </x-panel>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
