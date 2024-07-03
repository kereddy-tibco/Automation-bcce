cd ..
mvn clean install package resources:resources dependency:copy-dependencies  -PBuildAndPackage -DoutputDirectory=framework-pkg/lib