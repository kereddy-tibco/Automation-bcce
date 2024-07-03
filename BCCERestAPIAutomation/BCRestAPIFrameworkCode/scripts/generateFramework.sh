pwd=$(pwd)
cd $pwd/../
mvn package validate resources:resources dependency:copy-dependencies -PBuildAndPackage -DoutputDirectory=framework-pkg/lib