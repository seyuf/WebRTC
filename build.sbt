name := """WebRTC"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "font-awesome" % "4.3.0",
  "org.webjars" % "angularjs" % "1.3.8",
  "com.ning" % "async-http-client" % "1.8.14",
  "jp.t2v" %% "play2-auth" % "0.13.2",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play23-M2",
  "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
  "org.webjars" % "angular-ui-bootstrap" % "0.13.0",
  "org.webjars.bower" % "angular-websocket" % "1.0.9",
  "org.webjars.bower" % "angular-http-auth" % "1.2.2",
  "org.webjars.bower" % "angular-ui-router" % "0.2.15",
  "org.webjars.bower" % "angular-growl-v2" % "0.7.5"
)


