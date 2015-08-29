name := "futures-test"

version := "1.0"

scalaVersion := "2.11.7"

javaOptions := Seq("-Dscala.concurrent.context.numThreads=x3", "-Dscala.concurrent.context.maxThreads=x3", "-Dio.netty.eventLoopThreads=20")

fork := true

libraryDependencies ++=  {
  object Versions {
    val logbackVer = "1.1.3"
  }

  Seq(
    "com.github.mauricio" %% "postgresql-async" % "0.2.18",
    "ch.qos.logback" % "logback-classic" % Versions.logbackVer
  )
}
    