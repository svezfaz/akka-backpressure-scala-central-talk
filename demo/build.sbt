name := "akka-backpressure-demo"

version := "1.0"

scalaVersion := "2.11.8"

val akkaHttpV = "10.0.0"
val akkaV = "2.4.14"
val kamonV = "0.6.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"              % akkaHttpV,
  "com.typesafe.akka" %% "akka-agent"             % akkaV,
  "com.typesafe.akka" %% "akka-slf4j"             % akkaV,
  "org.slf4j"         %  "slf4j-api"              % "1.7.16"  % Runtime,
  "ch.qos.logback"    %  "logback-classic"        % "1.1.5"   % Runtime,
  "io.kamon"          %% "kamon-core"             % kamonV,
  "io.kamon"          %% "kamon-statsd"           % kamonV
)

aspectjSettings
javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj
fork in run := true
connectInput in run := true