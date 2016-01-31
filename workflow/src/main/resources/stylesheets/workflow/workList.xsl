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

    <xsl:template match="/xpil:WorkflowProcessInstances">
        <html xml:lang="ru">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <title>
                    <xsl:value-of select="'Рабочий лист'"/>
                </title>
                <xsl:call-template name="comon_css_and_js_includes">
                    <xsl:with-param name="webroot.path" select="$webroot.path"/>
                </xsl:call-template>
            </head>
            <body>
                <xsl:call-template name="common_menu"/>
                <form action="#" method="get" class="pure-form pure-form-aligned">
                    <fieldset>
                        <input type="hidden" name="filter" value="plain"/>
                        <input type="hidden" name="state" value="open*"/>
                        <!--
                        <div class="pure-control-group">
                            <label>Состояние</label>
                            <select name="state" required="required">
                                <option value="open*">
                                    <xsl:if test="$state='open*'">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                    </xsl:if>
                                    Работающие
                                </option>
                                <option value="*">
                                    <xsl:if test="$state='*'">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                    </xsl:if>
                                    Все
                                </option>
                            </select>
                        </div>
                        -->
                        <div class="pure-control-group">
                            <label>Назначение</label>
                            <select name="assignment" required="required">
                                <option value="ACCEPTED_AND_NON_ACCEPTED">
                                    <xsl:if test="$assignment='ACCEPTED_AND_NON_ACCEPTED'">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                    </xsl:if>
                                    Все назначенные мне
                                </option>
                                <option value="ONLY_NON_ACCEPTED">
                                    <xsl:if test="$assignment='ONLY_NON_ACCEPTED'">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                    </xsl:if>
                                    Только не принятые
                                </option>
                                <option value="ONLY_ACCEPTED">
                                    <xsl:if test="$assignment='ONLY_ACCEPTED'">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                    </xsl:if>
                                    Только принятые
                                </option>
                                <option value="*">
                                    <xsl:if test="$assignment='*'">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                    </xsl:if>
                                    Все
                                </option>
                            </select>
                        </div>
                        <div class="pure-control-group">
                            <label>UUID клиента</label>
                            <input type="text" name="DataInstances.clientUUID" value="{$DataInstances.clientUUID}"/>
                        </div>
                        <input type="hidden" name="limit" value="100"/>
                        <input type="hidden" name="order" value="activatedTime desc"/>
                        <!--
                        <div class="pure-control-group">
                            <label>Запрос поиска</label>
                            <textarea name="search">
                            </textarea>
                        </div>
                        -->
                        <div class="pure-control-group">
                            <button class="pure-button" type="submit">Обновить</button>
                            <a class="pure-button" href="../admin/checkDeadlines">Провертить дедлайны</a>
                            <a class="pure-button" href="../admin/reevaluateAssignments">Пересчитать назначения</a>
                        </div>
                    </fieldset>
                </form>

                <table  class="pure-table pure-table-horizontal pure-table-striped">
                    <caption>
                        <xsl:value-of select="'Рабочий лист'"/>
                    </caption>
                    <tr>
                        <th>Процесс</th>
                        <th>ID</th>
                        <th>Этап</th>
                        <th>П</th>
                        <th>Состояние</th>
                        <th>Принято</th>
                        <th>Создано</th>
                        <th>Завершено</th>
                    </tr>

                    <xsl:for-each select="xpil:MainWorkflowProcessInstance | xpil:SubWorkflowProcessInstance">
                        <xsl:variable name="pr" select="."/>
                        <xsl:for-each select="xpil:ActivityInstances/xpil:*">
                            <xsl:variable name="ac" select="."/>
                            <tr>
                                <td>
                                    <xsl:value-of select="$pr/@Name"/>
                                    <br/>
                                    <xsl:value-of select="$pr/xpil:InstanceDescription"/>
                                </td>
                                <td>
                                    <a href="{$pr/@Id}?xpilprop=FILL_PROCESS_VARIABLES&amp;xpilprop=FILL_RUNNING_ACTIVITIES&amp;xpilprop=FILL_ACTIVITY_DEADLINE_INFO">
                                        <xsl:value-of select="$pr/@Id"/>
                                    </a>
                                </td>
                                <td>
                                    <a href="{$pr/@Id}/activities/{$ac/@Id}">
                                        <xsl:value-of select="$ac/@Name"/>
                                    </a>
                                </td>
                                <td>
                                    <xsl:value-of select="$ac/xpil:InstancePriority"/>
                                </td>
                                <td>
                                    <xsl:value-of select="$ac/@State"/>
                                </td>
                                <td>
                                    <xsl:for-each select="xpil:AssignmentInstances/xpil:AssignmentInstance[@IsAccepted='true']">
                                        <xsl:value-of select="@Username"/>
                                    </xsl:for-each>
                                </td>
                                <td>
                                    <xsl:value-of select="$ac/@Created"/>
                                </td>
                                <td>
                                    <xsl:value-of select="$ac/@Finished"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
