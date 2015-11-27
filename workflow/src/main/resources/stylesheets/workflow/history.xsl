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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    xmlns:xpil="http://www.together.at/2006/XPIL1.0"
                    xmlns:xpdl="http://www.wfmc.org/2002/XPDL1.0" 
                    exclude-result-prefixes="xsl xpil xpdl"
                    xmlns:langutils="org.enhydra.shark.webclient.business.SharkUtils" 
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
    <xsl:param name="order"/>
    <xsl:variable name="orderDirect">
        <xsl:choose>
            <xsl:when test="$order">
                <xsl:value-of select="$order"/>
            </xsl:when>
            <xsl:otherwise>descending</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:param name="auth_type" select="FORM" />
    <xsl:param name="username" select="u" />
    <xsl:param name="lang" select="en_GB"/>
    <xsl:param name="from_element" select="11" />
    <xsl:param name="to_element" select="20" />
    <xsl:param name="sortCriterion" select="20" />
    <xsl:param name="sortAsc" select="true" />
    <xsl:param name="accessionMap" select="e" />
    <xsl:param name="total_number" select="200" />
    <xsl:param name="fromPage" select="p" />
    <xsl:param name="running_toolagent_activities" select="false" />
    <!--xsl:param name="apptitle" select="Together Workflow Server Web Client" /-->
    <xsl:param name="version" select="4.0" />
    <xsl:param name="release" select="1" />
    <xsl:param name="navig_url" select="ProcessActivitiesHandlerPO.po" />
    <xsl:param name="howManyVisible" select="11"/>

    <!--xsl:include xmlns:xsl="http://www.w3.org/1999/XSL/Transform" href="properties.xsl" />
    <xsl:include xmlns:xsl="http://www.w3.org/1999/XSL/Transform" href="menu.xsl" />
    <xsl:include xmlns:xsl="http://www.w3.org/1999/XSL/Transform" href="footer.xsl" />
    <xsl:include xmlns:xsl="http://www.w3.org/1999/XSL/Transform" href="utils.xsl" /-->

    <xsl:template match="xpil:ExtendedWorkflowFacilityInstance">

        <html xmlns="http://www.w3.org/1999/xhtml">

            <head>
                <meta http-equiv="content-type" content="text/html; charset='UTF-8'"/>
                <!--title>
                    <xsl:value-of select="concat($apptitle,' ',$version,'-',$release)"/>
                </title-->
                <title>
                    <xsl:value-of select="'История процесса'"/>
                </title>
                <xsl:call-template name="comon_css_and_js_includes">
                    <xsl:with-param name="webroot.path" select="$webroot.path"/>
                </xsl:call-template>

                <!--script type="text/javascript" xmlns:prop="java.util.Properties" xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xpdl="http://www.wfmc.org/2002/XPDL1.0">

                    function submitForm(formId, actionAttr) {
                    var formTag = document.getElementById(formId);
                    formTag.action=actionAttr;
                    formTag.submit();
                    }

                    function submitFormEvent(formId, actionAttr,event) {
                    var formTag = document.getElementById(formId);
                    formTag.action=actionAttr;
                    formTag.event.value=event;
                    formTag.submit();
                    }

                    function openNewDialogWindow(pageName,windowName,parameters) {
                    window.open(pageName,windowName,parameters); 
                    }

                    function submitContentFormWithAction(formId,actionAttr,event, txt) {
                    if(confirm(txt)) {
                    alreadySubmited = true;
                    if(window.event != null)
                    window.event.returnValue = false;
                    var formTag = document.getElementById(formId);
                    formTag.action=actionAttr;
                    formTag.event.value=event;
                    formTag.submit();
                    }
                    }
	
                </script-->
            </head>
            <body>
                <form action="javascript: void 0" id="mainForm" method="post" name="mainForm">
                    <input name="event" type="hidden"/>
                    <input id="rememberArrowId" type="hidden"/>
                    <input id="rememberArrowStyle" type="hidden"/>
                    <div id="w0">

                        <!-- page -->
                        <div id="pw0">
                            <div id="pw">

                                <!-- header -->
                                <div id="ph">
                                    <div id="phw">

                                        <!--xsl:call-template name="Prop">
                                            <xsl:with-param name="string" select="substring($accessionMap, 2, string-length($accessionMap) - 2)" />
                                            <xsl:with-param name="delimiter" select="', '" />
                                        </xsl:call-template-->


                                        <!-- header menu -->
                                        <!--xsl:call-template name="Menu">
                                            <xsl:with-param name="prop_repository" select="prop:getProperty('repository','true')" />
                                            <xsl:with-param name="prop_packagelist" select="prop:getProperty('packagelist','true')" />
                                            <xsl:with-param name="prop_processinst" select="prop:getProperty('processinst','true')" />
                                            <xsl:with-param name="prop_processmonitor" select="prop:getProperty('processmonitor','true')" />
                                            <xsl:with-param name="prop_usermanag" select="prop:getProperty('usermanag','true')" />
                                            <xsl:with-param name="prop_worklist" select="prop:getProperty('worklist','true')" />
                                            <xsl:with-param name="prop_processlist" select="prop:getProperty('processlist','true')" />
                                            <xsl:with-param name="prop_cache" select="prop:getProperty('cache','true')" />
                                            <xsl:with-param name="prop_application" select="prop:getProperty('application','true')" />
                                            <xsl:with-param name="prop_participant" select="prop:getProperty('participant','true')" />
                                            <xsl:with-param name="prop_groupmanag" select="prop:getProperty('groupmanag','true')" />
                                            <xsl:with-param name="howManyVisible" select="$howManyVisible" />
                                        </xsl:call-template-->
                                        <!-- header menu end -->	
                    
                                        <!-- toolbar -->
                                        <div id="cnthdr">
                                            <div id="cnthdrw0">
                                                <xsl:if test="$howManyVisible=1">
                                                    <div id="logosmall"/>
                                                </xsl:if>
                                                <div id="cnthdrw">
                                                    <div class="h01">
                                                        <xsl:variable name="process_id">
                                                            <xsl:value-of select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[1]/@Value"/>
                                                        </xsl:variable>
                                                        <xsl:variable name="act_id">
                                                            <xsl:value-of select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[2]/@Value"/>
                                                        </xsl:variable>
                                                        <xsl:if test="$act_id!='NaN'">
                                                            <h1>Activity History</h1>
                                                            <span class="tmdata">
                                                                <b>Process Id : </b> 
                                                                <xsl:value-of select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[1]/@Value"/>
                                                            </span>
                                                            <span class="tmdata">
                                                                <b>Activity Id : </b> 
                                                                <xsl:value-of select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[2]/@Value"/>
                                                            </span>
                                                        </xsl:if>
                                                        <xsl:if test="$act_id='NaN'">
                                                            <h1>История процесса</h1>
                                                            <span class="tmdata">
                                                                <b>Процесс : </b> 
                                                                <xsl:value-of select="xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[1]/@Value"/>
                                                            </span>
                                                        </xsl:if>
                                                    </div>
                                                    <xsl:variable name="cancelUrl">javascript:submitFormEvent('mainForm','HistoryHandlerPO.po?from=<xsl:value-of select='$from_element' />&#38;sortCriterion=<xsl:value-of select="$sortCriterion" />&#38;sortAsc=<xsl:value-of select="$sortAsc" />&#38;running_toolagent_activities=<xsl:value-of select="$running_toolagent_activities" />&#38;fromPage=<xsl:value-of select="$fromPage" />&#38;pro_id=<xsl:value-of select="/xpil:ExtendedWorkflowFacilityInstance/xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[1]/@Value" />','cancel')</xsl:variable>
                                                    <div class="cnttbw">
                                                        <div class="toolbar">
                                                            <div class="toolbarw0">
                                                                <div class="toolbarw">
                                                                    <ul class="toollset toollsetsa">
                                                                        <li>
                                                                            <a class="back" href="{$cancelUrl}" title="Back">
                                                                                <span></span>
                                                                            </a>
                                                                        </li>
                                                                    </ul>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>	
                                                </div>
                                            </div>
                                        </div>
                                        <!-- toolbar end -->
                                    </div>
                                </div>
                                <!-- header end-->

                                <!-- frame -->
                                <!--xsl:variable name="cntframewdivid">
                                    <xsl:call-template name="getCntFrameDivId">
                                        <xsl:with-param name="hideFooter" select="$hideFooter"/>
                                        <xsl:with-param name="howManyVisible" select="$howManyVisible"/>
                                    </xsl:call-template>
                                </xsl:variable-->

                                <!--div id="{$cntframewdivid}"-->
                                <div id="1">
                                    <div id="cntframe">
                                        <div id="cnt01">
                                            <table class="pure-table pure-table-horizontal pure-table-striped" id="prlistdata" cellspacing="0">
                                                <thead id="table01head">
                                                    <tr>
                                                        <th title="Time"><a id="headTime" href="javascript:void(0)">Time
                                                            <xsl:choose>
                                                                <xsl:when test="$orderDirect='ascending'">
                                                                    &#8593;
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    &#8595;
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </a>
                                                        </th>
                                                        <th title="Type">Type</th>
                                                        <th title="Package Id">Package Id</th>
                                                        <th title="Process Definition Id">Process Definition Id</th>
                                                        <th title="Version" style="width: 3%;">Version</th>
                                                        <th title="Process Name">Process Name</th>
                                                        <th title="Activity Definition Id">Activity Definition Id</th>
                                                        <xsl:if test="/xpil:ExtendedWorkflowFacilityInstance/xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[2]/@Value='NaN'">
                                                            <th title="Activity Id">Activity Id</th>
                                                        </xsl:if>          
                                                        <th title="Activity Name">Activity Name</th>
                                                        <th title="Additional Information" style="min-width:300px;">Additional Information</th>
                                                    </tr>
                                                </thead>

                                                <tbody class="table01body">
                                                    <xsl:variable name="Attr1" select="//xpil:EventAudits/xpil:*"/>
                                                    <xsl:for-each select="$Attr1[local-name()='StateEventAudit' or local-name()='DataEventAudit' or local-name()='AssignmentEventAudit' or local-name()='CreateProcessEventAudit']">
                                                        <xsl:sort select="@Created" order="{$orderDirect}"/>
                                                        <tr>
                                                            <td title="{concat(substring(@Created,0,11),' ',substring(@Created,12,12))}" class="usualtable">
                                                                <xsl:value-of select="concat(substring(@Created,0,11),' ',substring(@Created,12,12))" />
                                                            </td>
                                                            <td title="{@Type}">
                                                                <xsl:value-of select="@Type" />
                                                            </td>
                                                            <td title="{@PackageId}">
                                                                <xsl:value-of select="@PackageId" />
                                                            </td>
                                                            <td title="{@ProcessDefinitionId}">
                                                                <xsl:value-of select="@ProcessDefinitionId" />
                                                            </td>
                                                            <td title="{@WorkflowProcessFactoryVersion}" style="width: 3%;">
                                                                <xsl:value-of select="@WorkflowProcessFactoryVersion" />
                                                            </td>
                                                            <td title="{@WorkflowProcessInstanceName}">
                                                                <xsl:value-of select="@WorkflowProcessInstanceName" />
                                                            </td>
                                                            <xsl:choose>
                                                                <xsl:when test="@ActivityDefinitionId">
                                                                    <td title="{@ActivityDefinitionId}">
                                                                        <xsl:value-of select="@ActivityDefinitionId" />
                                                                    </td>
                                                                    <xsl:if test="/xpil:ExtendedWorkflowFacilityInstance/xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[2]/@Value='NaN'">
                                                                        <td title="{@ActivityInstanceId}">
                                                                            <xsl:value-of select="@ActivityInstanceId" />
                                                                        </td>
                                                                    </xsl:if>
                                                                    <td title="{@ActivityInstanceName}">
                                                                        <xsl:value-of select="@ActivityInstanceName" />
                                                                    </td>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <td title=" - ">
                                                                        -
                                                                    </td>
                                                                    <xsl:if test="/xpil:ExtendedWorkflowFacilityInstance/xpil:InstanceExtendedAttributes/xpil:InstanceExtendedAttribute[2]/@Value='NaN'">
                                                                        <td title=" - ">
                                                                            -
                                                                        </td>
                                                                    </xsl:if>
                                                                    <td title=" - ">
                                                                        -
                                                                    </td>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                            <xsl:variable name="cnt">
                                                                <xsl:if test="local-name()='StateEventAudit'">
                                                                    <xsl:if test="@OldState!=''">Old state: <xsl:value-of select="@OldState" />&#160;&#10;</xsl:if>New state: <xsl:value-of select="@NewState" />
                                                                </xsl:if>
                                                                <xsl:if test="local-name()='AssignmentEventAudit'">
                                                                    <xsl:if test="@OldResourceKey!=''">Old assignee: <xsl:value-of select="@OldResourceKey" />&#160;&#10;</xsl:if>New assignee: <xsl:value-of select="@NewResourceKey" />
                                                                    Is accepted: <xsl:value-of select="@IsAccepted" />
                                                                </xsl:if>
                                                                <xsl:if test="local-name()='DataEventAudit'">Variable changes: 
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:StringDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:SchemaDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:DoubleDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:LongDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:BooleanDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:DateDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:DateTimeDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="concat(substring(@Value,0,11),' ',substring(@Value,12,8))"/>&#160;
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:TimeDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <xsl:value-of select="@Id"/>: <xsl:value-of select="@Value"/>&#160;
                                                                    </xsl:for-each>
                                                                </xsl:if>
                                                                <xsl:if test="local-name()='CreateProcessEventAudit'">
                                                                    <xsl:if test="@PWorkflowProcessInstanceId!=''">Parent Package id: <xsl:value-of select="@PPackageId" />
                                                                        Parent Process definition id: <xsl:value-of select="@PProcessDefinitionId" />
                                                                        Parent version: <xsl:value-of select="@PWorkflowProcessFactoryVersion" />
                                                                        Parent process id: <xsl:value-of select="@PWorkflowProcessInstanceId" />
                                                                        Parent process name: <xsl:value-of select="@PWorkflowProcessInstanceName" />
                                                                        Parent activity definition id: <xsl:value-of select="@PActivityDefinitionId" />		
                                                                        Parent activity id: <xsl:value-of select="@PActivityInstanceId" />
                                                                    </xsl:if>
                                                                </xsl:if>
                                                            </xsl:variable>
                                                            <td title="{$cnt}">
                                                                <xsl:if test="local-name()='StateEventAudit'">
                                                                    <xsl:if test="@OldState!=''">
                                                                        <b>Old state: </b>
                                                                        <xsl:value-of select="@OldState" />
                                                                        <br/>
                                                                    </xsl:if>
                                                                    <b>New state: </b>
                                                                    <xsl:value-of select="@NewState" />
                                                                </xsl:if>
                                                                <xsl:if test="local-name()='AssignmentEventAudit'">
                                                                    <xsl:if test="@OldResourceKey!=''">
                                                                        <b>Old assignee: </b>
                                                                        <xsl:value-of select="@OldResourceKey" />
                                                                        <br/>
                                                                    </xsl:if>
                                                                    <b>New assignee: </b>
                                                                    <xsl:value-of select="@NewResourceKey" />
                                                                    <br/>
                                                                    <b>Is accepted: </b>
                                                                    <xsl:value-of select="@IsAccepted" />
                                                                </xsl:if>
                                                                <xsl:if test="local-name()='DataEventAudit'">
                                                                    <b>Variable changes: </b>
                                                                    <br/>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:StringDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:SchemaDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:DoubleDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:LongDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:BooleanDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:DateDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:DateTimeDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="concat(substring(@Value,0,11),' ',substring(@Value,12,8))"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                    <xsl:for-each select="xpil:NewEventData/xpil:TimeDataInstance">
                                                                        <xsl:text> - </xsl:text>
                                                                        <b>
                                                                            <xsl:value-of select="@Id"/>: </b>
                                                                        <xsl:value-of select="@Value"/>
                                                                        <br/>
                                                                    </xsl:for-each>
                                                                </xsl:if>
                                                                <xsl:if test="local-name()='CreateProcessEventAudit'">
                                                                    <xsl:if test="@PWorkflowProcessInstanceId!=''">
                                                                        <b>Parent Package id: </b>
                                                                        <xsl:value-of select="@PPackageId" />
                                                                        <br/>
                                                                        <b>Parent Process definition id: </b>
                                                                        <xsl:value-of select="@PProcessDefinitionId" />
                                                                        <br/>
                                                                        <b>Parent version: </b>
                                                                        <xsl:value-of select="@PWorkflowProcessFactoryVersion" />
                                                                        <br/>
                                                                        <b>Parent process id: </b>
                                                                        <xsl:value-of select="@PWorkflowProcessInstanceId" />
                                                                        <br/>
                                                                        <b>Parent process name: </b>
                                                                        <xsl:value-of select="@PWorkflowProcessInstanceName" />
                                                                        <br/>
                                                                        <b>Parent activity definition id: </b>
                                                                        <xsl:value-of select="@PActivityDefinitionId" />		
                                                                        <br/>
                                                                        <b>Parent activity id: </b>
                                                                        <xsl:value-of select="@PActivityInstanceId" />
                                                                    </xsl:if>
                                                                </xsl:if>
                                                            </td>
                                                        </tr>
                                                    </xsl:for-each>
                                                </tbody>
                                            </table>

                                        </div>
                                    </div>
                                </div>
                                <!-- frame end -->    

                                <!-- footer -->
                                <!--xsl:call-template name="Footer"></xsl:call-template-->
                                <!-- footer end -->

                            </div>
                        </div>
                        <!-- page end -->

                    </div>

                    <script type="text/javascript">
                        //<![CDATA[

dynStyleSet(['tbodyFrame', 'thHighlight', 'tatdHighlight']);

//]]>
                    </script>
                    <script src="{$webroot.path}/js/workflow/web/history.js" type="application/javascript"/>

                </form>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>