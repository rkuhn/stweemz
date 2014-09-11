name := "stweemz"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % "0.5"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

libraryDependencies += "org.hamcrest" % "hamcrest-all" % "1.3"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")
