<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>
    <xsl:template name="chart-pv">
        <xsl:param name="name"/>
        <xsl:param name="resourcename"/>
        <xsl:param name="chart-data"/>
        <xsl:for-each select="$chart-data/pv">
            <xsl:value-of select="$name"/>
            <xsl:text>;</xsl:text>
            <xsl:choose>
                <xsl:when test="$chart-data/@resourcename">
                    <xsl:value-of select="upper-case($chart-data/@resourcename)"/>
                </xsl:when>
                <xsl:when test="not($chart-data/@resourcename)">
                    <xsl:value-of select="upper-case($chart-data/@id)"/>
                </xsl:when>
            </xsl:choose>
            <xsl:text>;</xsl:text>
            <xsl:value-of select="@date"/>
            <xsl:text>;</xsl:text>
            <xsl:value-of select="@response_times"/>
            <xsl:text>&#10;</xsl:text>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="//sapinstance">
        <xsl:variable name="sapinstance-name" select="@name"/>
        <xsl:for-each select="performance/chart">
            <xsl:call-template name="chart-pv">
                <xsl:with-param name="name" select="$sapinstance-name"/>
                <xsl:with-param name="chart-data" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
