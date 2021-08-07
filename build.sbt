
name := """WebRTC"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"


resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += Resolver.jcenterRepo


libraryDependencies ++= Seq(
  cache,
  filters,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "font-awesome" % "4.3.0",
  "org.webjars" % "angularjs" % "1.3.8",
  "com.ning" % "async-http-client" % "1.8.14",
  "jp.t2v" %% "play2-auth" % "0.13.2",
  //"org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play23-M2",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.11",
  "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
  "org.webjars" %% "webjars-play" % "2.5.0-2",
  "org.webjars" % "angular-ui-bootstrap" % "0.13.0",
  "org.webjars.bower" % "angular-websocket" % "1.0.9",
  "org.webjars.bower" % "angular-http-auth" % "1.2.2",
  "org.webjars.bower" % "angular-ui-router" % "0.2.15",
  "org.webjars.bower" % "angular-growl-v2" % "0.7.5",
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.iheart" %% "ficus" % "1.2.3",
  "commons-io" % "commons-io" % "2.5"
)


libraryDependencies ++= Seq(
  cache
)


resolvers += Resolver.jcenterRepo
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += Resolver.sonatypeRepo("snapshots")

//routesGenerator := InjectedRoutesGenerator

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


routesGenerator := InjectedRoutesGenerator

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

// enable improved (experimental) incremental compilation algorithm called "name hashing"
incOptions := incOptions.value.withNameHashing(true)

//pipelineStages := Seq(rjs, uglify, digest, gzip)

//javaOptions in Test ++= Seq("-Dconfig.file=conf/application-test.conf", "-Dlogger.resource=test-logger.xml")
//javaOptions ++= Seq("-Xms512m -Xmx512m")
//javaOptions in run := Seq("-Xms512m", "-Xmx128m")


