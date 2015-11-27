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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://www.w3.org/1999/xhtml">

    <xsl:template name="comon_css_and_js_includes">
        <xsl:param name="webroot.path"/>
        <link rel="stylesheet" type="text/css" href="{$webroot.path}/css/bower_components.css"/>
        <script type="text/javascript">self.fetch=null;</script>
        <script src="{$webroot.path}/js/bower_components.js" type="application/javascript">
            <xsl:comment/>
        </script>
        <script src="{$webroot.path}/js/workflow/api/ProcessResource.js" type="application/javascript">
            <xsl:comment/>
        </script>
        <script src="{$webroot.path}/js/workflow/lib/Common.js" type="application/javascript">
            <xsl:comment/>
        </script>
        <script src="{$webroot.path}/js/workflow/web/controls.js" type="application/javascript">
            <xsl:comment/>
        </script>

    </xsl:template>
</xsl:stylesheet>
