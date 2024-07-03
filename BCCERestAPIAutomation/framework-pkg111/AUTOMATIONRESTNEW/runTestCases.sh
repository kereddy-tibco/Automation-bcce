export timestamp=$(date +'%Y_%m_%d_%H_%M')
export configFile=$1

if [$cofigFile == ""] 
	then
		java -Dresources.dir=resources -Dorg.uncommons.reportng.escape-output=false -cp "lib/*" org.testng.TestNG test-config-files/config.xml -d "test-output/$timestamp"
		java -Dmail.output.dir="./test-output/$timestamp" -cp "lib/*" com.tibco.rest.common.GenerateXMLAndSendMail "test-output/$timestamp/xml"
	else
		echo cofigFile = $configFile
		java -Dresources.dir=resources -Dorg.uncommons.reportng.escape-output=false -cp "lib/*" org.testng.TestNG $configFile -d "test-output/$timestamp"
		java -Dmail.output.dir="./test-output/$timestamp" -cp "lib/*" com.tibco.rest.common.GenerateXMLAndSendMail "test-output/$timestamp/xml"
fi