/*
 * Copyright 2019 gmkumar2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import sbt._


object Dependencies {
  val circeVersion = "0.11.0"
  val catsVersion = "1.6.0"
  val catsEffectVersion = "1.2.0"
  val elastic4sVersion = "6.2.3"
  val http4sVersion = "0.20.0-M7"
  val catsRetryVersion = "0.2.5"
  val monocleVersion = "1.5.0"
  lazy val fs2Version = "0.9.7"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val catsCore = "org.typelevel" %% "cats-core" % catsVersion
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
  lazy val circeJava8 = "io.circe" %% "circe-java8" % circeVersion
  lazy val circeOptics = "io.circe" %% "circe-optics" % circeVersion
  lazy val circeExtras = "io.circe" %% "circe-generic-extras" % circeVersion
  lazy val circefs2 = "io.circe" %% "circe-fs2" % circeVersion
  lazy val elastic4score = "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion
  lazy val elastic4shttp = "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion
  lazy val elastic4sstream = "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion
  lazy val elastic4scirce = "com.sksamuel.elastic4s" % "elastic4s-circe_2.12" % elastic4sVersion
  lazy val blazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  lazy val blazeClient = "org.http4s" %% "http4s-blaze-client" % http4sVersion
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.10.2"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val pureConfigIo = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.10.2"
  lazy val catsRetry = "com.github.cb372" %% "cats-retry-core" % catsRetryVersion
  lazy val catsRetryEF = "com.github.cb372" %% "cats-retry-cats-effect" % catsRetryVersion
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "1.0.4"
  lazy val fs2IO = "co.fs2" %% "fs2-io" % "1.0.4"
  lazy val fs2Reactive = "co.fs2" %% "fs2-reactive-streams" % "1.1.4"
  lazy val jawnAst = "org.jsawn" %% "jawn-ast" % "0.8.4"
  lazy val monocleCore = "com.github.julien-truffaut" %% "monocle-core" % monocleVersion
  lazy val monocleMacro = "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion
  lazy val monocleLaw = "com.github.julien-truffaut" %% "monocle-law" % monocleVersion % "test"
  lazy val circeIteratee = "io.circe" %% "circe-iteratee" % "0.12.0"
  lazy val iterateFiles = "io.iteratee" %% "iteratee-files" % "0.18.0"
  lazy val jawnParser ="org.typelevel" %% "jawn-parser" % "0.14.1"
  lazy val jawnUtils =  "org.typelevel" %% "jawn-ast" % "0.14.1"

 
}
