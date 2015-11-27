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
                <script src="{$webroot.path}/js/workflow/web/process.js" type="application/javascript">
                    <xsl:comment/>
                </script>

            </head>
            <body>
                <xsl:apply-templates select="xpil:*" mode="form"/>

            </body>
        </html>
    </xsl:template>
    <xsl:template match="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance" mode="form">
        <xsl:variable name="pr" select="."/>

        <form action="{$pr/@Id}" enctype="application/json" is="form-ajax" method="put" class="pure-form pure-form-aligned">
            <fieldset>
                <input type="hidden" name="{local-name(.)}[@xmlns][$]" value="http://www.together.at/2006/XPIL1.0"/>
                <div class="pure-control-group">
                    <label>Название:</label>
                    <span>
                        <xsl:value-of select="$pr/@Name"/>
                    </span>
                </div>
                <div class="pure-control-group">
                    <label>Состояние:</label>
                    <span>
                        <xsl:value-of select="$pr/@State"/>
                    </span>
                </div>
                <div class="pure-control-group">
                    <label>Описание:</label>
                    <span>
                        <xsl:value-of select="$pr/xpil:InstanceDescription"/>
                    </span>
                </div>
                <div class="pure-control-group">
                    <label>Очередность:</label>
                    <span>
                        <xsl:value-of select="$pr/xpil:InstancePriority"/>
                    </span>
                </div>
                <xsl:apply-templates select="$pr/xpil:DataInstances/xpil:*" mode="controlGroup"/>
                <button class="pure-button" type="submit">Сохранить</button>
                <a class="pure-button" href="{$pr/@Id}/graph">
                    схема
                </a>
                <a class="pure-button" href="{$pr/@Id}/history">
                    история
                </a>
                <button id="reevaluateAssignmentsBtn" class="pure-button" type="button">Пересчет назначений</button>
                <button id="migrateProcessBtn" class="pure-button" type="button">Обновить версию</button>
                <button id="checkProceessDeadlinesBtn" class="pure-button" type="button">Проверить дедайны</button>

            </fieldset>
        </form>
        <table  class="pure-table pure-table-horizontal pure-table-striped">
            <caption>Активити</caption>
            <tr>
                <th>Активити</th>
                <th>Состояние</th>
                <th>Создано</th>
                <th>Назначено</th>
            </tr>
            <xsl:for-each select="xpil:ActivityInstances/xpil:*">
                <xsl:sort select="@Created"/>
                <xsl:variable name="ac" select="."/>
                <tr>
                    <td>
                        <a href="{$pr/@Id}/activities/{$ac/@Id}">
                            <xsl:value-of select="$ac/@Name"/>
                        </a>
                    </td>
                    <td>
                        <xsl:value-of select="$ac/@State"/>
                    </td>
                    <td>
                        <xsl:value-of select="$ac/@Created"/>
                    </td>
                    <td>
                        <xsl:for-each select="xpil:AssignmentInstances/xpil:AssignmentInstance">
                            <xsl:value-of select="@Username"/>
                            <xsl:if test="position()!=last()">
                                <xsl:text>,</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </td>
                </tr>

            </xsl:for-each>
        </table>
        <table  class="pure-table pure-table-horizontal pure-table-striped">
            <caption>Дедлайны</caption>
            <tr>
                <th>Активити</th>
                <th>Исполнен</th>
                <th>Синхронный</th>
                <th>Дедлайн</th>
                <th>Дата</th>
            </tr>
            <xsl:for-each select="xpil:ActivityInstances/xpil:*">
                <xsl:sort select="@Created"/>
                <xsl:variable name="ac" select="."/>
                <xsl:for-each select="xpil:DeadlineInstances/xpil:DeadlineInstance">
                    <xsl:sort select="@TimeLimit"/>
                    <xsl:variable name="d" select="."/>
                    <tr>
                        <td>
                            <xsl:value-of select="$ac/@Name"/>
                        </td>
                        <td>
                            <xsl:value-of select="$d/@IsExecuted"/>
                        </td>
                        <td>
                            <xsl:value-of select="$d/@IsSynchronous"/>
                        </td>
                        <td>
                            <xsl:value-of select="$d/@ExceptionName"/>
                        </td>
                        <td>
                            <xsl:value-of select="$d/@TimeLimit"/>
                        </td>
                    </tr>
                </xsl:for-each>

            </xsl:for-each>
        </table>

    </xsl:template>

</xsl:stylesheet>
