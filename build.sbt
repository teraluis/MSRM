import sbtsonar.SonarPlugin.autoImport.sonarUseExternalConfig

name := """crm"""
organization := "io.fabrick"

version := "0.9.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.apache.commons" % "commons-text" % "1.8",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "io.minio" % "minio" % "6.0.11",
  "org.elasticsearch.client" % "transport" % "7.6.1",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.6.1",
  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",
  "com.jcraft" % "jsch" % "0.1.54",
  "commons-net" % "commons-net" % "3.6",
  "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
  "org.codehaus.janino" % "janino" % "3.1.2",
  "ch.qos.logback.contrib" % "logback-jackson" % "0.1.5",
  "ch.qos.logback.contrib" % "logback-json-classic" % "0.1.5",
  "com.itextpdf" % "itextpdf" % "5.5.13.2",
  "com.opencsv" % "opencsv" % "5.3",
  "nl.garvelink.oss" % "iban" % "1.2.0",
  javaJdbc, guice, ws, evolutions, javaForms, javaJpa)

packageName in Universal := "crm"