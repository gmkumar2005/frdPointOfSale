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

import Dependencies._

val STARTYEAR = 2019
organizationName := "gmkumar2005"
startYear := Some(STARTYEAR)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

fork in runMain := true
fork in run := true
//scalaVersion := "2.12.8"
version := "0.1.0-SNAPSHOT"
organization := "com.simplePos"
organizationName := "simplePos"
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

val dependencies = {
  import Dependencies._
  Seq(
    catsCore, catsEffect, pureConfig, circeCore, circeGeneric, circeParser, circeJava8, circeOptics, circeExtras,
    blazeServer, http4sCirce, http4sDsl, blazeClient, catsRetry, catsRetryEF, logback, pureConfigIo, monocleCore,
    monocleMacro, monocleLaw, circeIteratee, iterateFiles, fs2Core, fs2IO, circefs2, jawnParser, jawnUtils)
}

lazy val simplePos = (project in file("./simplePos"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= dependencies, libraryDependencies += scalaTest % Test
  )

lazy val root = (project in file(".")).
  aggregate(simplePos)
