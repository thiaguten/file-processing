enablePlugins(JavaAppPackaging)

organization := "br.com.thiaguten"
name := "file-processing"
version := "0.0.1"
scalaVersion := "2.12.10"
maintainer := "Thiago Gutenberg thiaguten@gmail.com"

val targetJvm = "1.8"

javacOptions ++= Seq(
  "-source", targetJvm,
  "-target", targetJvm,
  "-encoding", "UTF-8"
)

scalacOptions ++= Seq(
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  s"-target:jvm-$targetJvm"
)

//resolvers ++= Seq(
//  DefaultMavenRepository,
//  Resolver sonatypeRepo "public",
//  Resolver typesafeRepo "releases",
//  Resolver typesafeIvyRepo "releases",
//  Resolver sbtPluginRepo "releases",
//  Resolver bintrayRepo("owner", "repo"),
//  Resolver jcenterRepo
//)

scriptClasspath := Seq("*")

mappings in Universal ++= {
  val resourcesDir = (resourceDirectory in Compile).value
  (resourcesDir ** "*" get) map (x => x -> ("conf/" + x.getName))
}

val excludeFileRegex = """(.*?)\.(conf|xml)$""".r

mappings in(Compile, packageBin) ~= {
  ms: Seq[(File, String)] =>
    ms filterNot {
      case (file, _) => excludeFileRegex.pattern.matcher(file.getName).matches
    }
}

bashScriptExtraDefines ++= Seq(
  """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
  """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
)

val slf4jVersion = "1.7.25"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)

excludeDependencies ++= Seq(
  // commons-logging is replaced by jcl-over-slf4j
  ExclusionRule("commons-logging", "commons-logging"),
  // slf4j-log4j12 is replaced by log4j-over-slf4j
  ExclusionRule("org.slf4j", "slf4j-log4j12")
)
