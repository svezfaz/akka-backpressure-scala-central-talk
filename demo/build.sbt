name := "akka-backpressure-demo"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"            % "2.5.9",
  "com.typesafe.akka" %% "akka-slf4j"             % "2.5.9",
  "org.slf4j"         %  "slf4j-api"              % "1.7.16"  % Runtime,
  "ch.qos.logback"    %  "logback-classic"        % "1.1.5"   % Runtime,
  "io.kamon"          %% "kamon-core"             % "0.6.7",
  "io.kamon"          %% "kamon-statsd"           % "0.6.7"
)

fork in run := true