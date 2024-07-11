<xsl:stylesheet version="1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:redirect="http://xml.apache.org/xalan/redirect">
	<xsl:output method="html" indent="yes" encoding="UTF-8" />
	<xsl:decimal-format decimal-separator="."
						grouping-separator="," />


	<xsl:param name="output.dir" select="'.'" />
	<xsl:param name="title" />
	<xsl:param name="platform_name" />
	<xsl:param name="build_name" />
	<xsl:param name="data_base" />
	<xsl:param name="reason_for_execution" />
	<xsl:param name="report_url" />
	<xsl:param name="failure_report_url" />
	<xsl:param name="test_compare_report_url" />
	<!--xsl:param name="unit_test_report_url" /-->
	<!--xsl:param name="reportFile" /-->
	<xsl:param name="configuration_details" />
	<xsl:param name="param_more_info_link" />
	<!--xsl:param name="dev_html_report_url" /-->


	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:value-of select="$title" />
				</title>
			</head>
			<body
					style="font-family:verdana,arial,helvetica; font-size:68%; text-align:center">
				<h1>
					<xsl:value-of select="$title" />
				</h1>
				<table cellpadding="5" cellspacing="5"
					   style="text-align: left; width: 700px; font-size:16px;" align="center">
					<tr width="100%" style="background:#eeeee0;">
						<td>Platform </td>
						<td>
							<xsl:value-of select="$platform_name" />
						</td>
					</tr>
					<tr width="100%" style="background:#eeeee0;">
						<td>Build </td>
						<td>
							<xsl:value-of select="$build_name" />
						</td>
					</tr>
					<tr width="100%" style="background:#eeeee0;">
						<td>DataBase </td>
						<td>
							<xsl:value-of select="$data_base" />
						</td>
					</tr>
					<tr width="100%" style="background:#eeeee0;">
						<td>Reason For Execution </td>
						<td>
							<xsl:value-of select="$reason_for_execution" />
						</td>
					</tr>
				</table>
				<br/>
				<xsl:choose>
					<xsl:when test="$configuration_details">
						<table cellpadding="5" cellspacing="5"
							   style="text-align: left; width: 700px; font-size:16px;" align="center">
							<tr width="100%" style="background:#eeeee0;">
								<td>Configuration Details </td>
								<td>
									<xsl:value-of select="$configuration_details" disable-output-escaping="yes" />
								</td>
							</tr>
							<xsl:choose>
								<xsl:when test="contains($param_more_info_link,'http')">
									<tr width="100%" style="background:#eeeee0;">
										<td colspan='3' style="font-size:12px;" align="right"><a href="{$param_more_info_link}" target="_blank">More Configuration Details...</a></td>
									</tr>
								</xsl:when>
							</xsl:choose>
						</table>
					</xsl:when>
				</xsl:choose>
				<br/>
				<div style="text-align: center;">
					<h2>Test Report Summary</h2>
				</div>

				<!-- Add extra fields here using table. Use : <table cellpadding="5" 
					cellspacing="5" style="text-align: left; width: 700px; font-size:16px;" align="center"> 
					<tr style="background:#eeeee0;"> </tr> </table> -->

				<table cellpadding="5" cellspacing="5"
					   style="text-align: left; width: 700px; font-size:16px;" align="center">
					<xsl:key name="distinctPackage" match="testsuite" use="@name" />
					<xsl:for-each
							select="//testsuite[generate-id() = generate-id(key('distinctPackage', @name)[1])]">
						<xsl:sort select="testsuites//testsuite" order="ascending" />
						<xsl:call-template name="addtestsuite">
							<xsl:with-param name="projectname" select="@name" />
							<!--xsl:with-param name="reportFile" select="$reportFile" /-->
						</xsl:call-template>
					</xsl:for-each>
					<tr>
						<td>
							<br />
						</td>
					</tr>
					<xsl:variable name="testCount" select="sum(//testsuite/@tests)" />
					<xsl:variable name="errorCount" select="sum(//testsuite/@errors)" />
					<xsl:variable name="failureCount" select="sum(//testsuite/@failures)" />
					<xsl:variable name="failureUnknownCount" select="count(//testcase/failure[@type = 'Unknown'])" />
					<xsl:choose>
						<xsl:when test="//testsuite[1]/@skipped">

							<xsl:variable name="skippedCount" select="sum(//testsuite/@skipped)" />

							<tr style="background:#eeeee0;">
								<td>
									<b>Total</b>
								</td>
								<td>
									<xsl:value-of select="$testCount" />
								</td>
								<td style="background:#EBFFD6;">
									<xsl:value-of
											select="$testCount + $failureUnknownCount - $failureCount" />
								</td>
								<td>
									<a href="{$failure_report_url}" target="_blank">
										<xsl:value-of select="$failureCount - $failureUnknownCount" /></a>
								</td>
								<td style="background:#FFDCDC;">
									<xsl:value-of select="$skippedCount" />
								</td>
							</tr>
						</xsl:when>
						<xsl:otherwise>
							<!--xsl:variable name="testCount" select="sum(//testsuite/@tests)" />
							<xsl:variable name="errorCount" select="sum(//testsuite/@errors)" />
							<xsl:variable name="failureCount" select="sum(//testsuite/@failures)" />
							<xsl:variable name="failureUnknownCount" select="sum(//testsuite/@failure[@type='Unknown'])" /-->

							<tr style="background:#eeeee0;">
								<td>
									<b>Total</b>
								</td>
								<td>
									<xsl:value-of select="$testCount" />
								</td>
								<td style="background:#EBFFD6;">

									<xsl:value-of select="$testCount + $failureUnknownCount - $failureCount" />
								</td>
								<xsl:choose>
									<!--xsl:when test="($failureCount + $errorCount) &gt; 1"-->
									<xsl:when test="($failureCount - $failureUnknownCount) &gt; 1">
										<td style="background:#FFDCDC;">
											<a href="{$failure_report_url}" target="_blank">
												<xsl:value-of select="$failureCount - $failureUnknownCount" /></a>
										</td>
									</xsl:when>
									<xsl:otherwise>
										<td style="background:#FFDCDC;">
											<!--xsl:value-of select="$failureCount + $errorCount" /-->
											<xsl:value-of select="$failureCount - $failureUnknownCount" />
										</td>
									</xsl:otherwise>
								</xsl:choose>
								<td style="background:#FFDCDC;">
									<xsl:value-of select="0" />
								</td>
							</tr>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="contains($report_url,'http')">
							<tr style="background:#eeeee0;">
								<td>
									Report Link
								</td>
								<td colspan="4">
									<a href="{$report_url}" target="_blank">Report Link</a>
								</td>
							</tr>
						</xsl:when>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="contains($test_compare_report_url,'http')">
							<tr style="background:#eeeee0;">
								<td>
									Test Compare Report Link
								</td>
								<td colspan="4">
									<a href="{$test_compare_report_url}" target="_blank">Build Compare Report Link</a>
								</td>
							</tr>
						</xsl:when>
					</xsl:choose>
				</table>
				<br />
			</body>
		</html>
	</xsl:template>

	<xsl:template name="addtestsuite">
		<!--xsl:param name="totalsuite" /-->
		<!--xsl:param name="reportFile" /-->
		<xsl:param name="projectname" />

		<tr>
			<th colspan="5">
				Project :
				<xsl:value-of select="@name" />
			</th>
		</tr>
		<tr style="font-weight: bold; text-align:left; background:#a6caf0;">
			<th align="left" scope="col" style="width: 55%;">
				<b>Module Name</b>
			</th>
			<th>
				Total
			</th>
			<th>
				Pass
			</th>
			<th>
				Fail
			</th>
			<th>
				Skip
			</th>
		</tr>

		<xsl:for-each select="//testsuite">
			<xsl:choose>
				<xsl:when test="@name = $projectname">
					<xsl:call-template name="fillValues">
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="fillValues">
		<tr style="background:#eeeee0;">
			<td>
				<xsl:value-of select="@name" />
			</td>
			<td>
				<xsl:value-of select="@tests" />
			</td>
			<xsl:variable name="unknown" select="count(//testcase/failure[@type = 'Unknown'])"/>
			<xsl:choose>
				<xsl:when test="@skipped">
					<td style="background:#EBFFD6;">
						<xsl:value-of select="@tests + $unknown - @failures - @skipped" />
					</td>
					<td style="background:#FFDCDC;">
						<!--xsl:value-of select="@failures+@errors" /-->
						<xsl:value-of select="@failures - $unknown" />
					</td>
					<td style="background:#FFDCDC;">
						<xsl:value-of select="@skipped" />
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td style="background:#EBFFD6;">
						<!--xsl:value-of select="@tests - @failures - @errors" /-->
						<xsl:value-of select="@tests - @failures" />
					</td>
					<td style="background:#FFDCDC;">
						<!--xsl:value-of select="@failures+@errors" /-->
						<xsl:value-of select="@failures - $unknown" />
					</td>
					<td style="background:#FFDCDC;">
						0
					</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</xsl:template>
</xsl:stylesheet>
