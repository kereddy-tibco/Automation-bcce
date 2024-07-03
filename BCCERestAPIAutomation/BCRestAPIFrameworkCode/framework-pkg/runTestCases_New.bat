@echo off

set year=%date:~-4,4%
set month=%date:~-3,2%
set day=%date:~-7,2%
set hour=%time:~-11,2%
set minute=%time:~-8,2%
set timestamp=%year%_%month%_%day%_%hour%_%minute%


set arg1=%1
set arg2=%2
Set output=".\test-output\%timestamp%"

java -Dresources.dir=resources -Dorg.uncommons.reportng.escape-output=false -cp "lib\*" org.testng.TestNG test-config-files/config.xml -d "test-output\%timestamp%"
java -DdatabaseType=%arg1% -DbuildNumber=%arg2% -Dmail.output.dir=output -cp "lib\*" com.tibco.rest.common.GenerateXMLAndSendMail "test-output\%timestamp%\xml"
goto end

:end
