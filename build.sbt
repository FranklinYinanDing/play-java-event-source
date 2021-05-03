name := """play-java-seed"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayNettyServer).disablePlugins(PlayAkkaHttpServer)

scalaVersion := "2.11.12"

libraryDependencies += guice
