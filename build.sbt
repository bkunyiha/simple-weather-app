val CirceVersion = "0.14.5"
val Http4sVersion = "0.23.23"
val LogbackVersion = "1.4.11"
val MunitCatsEffectVersion = "1.0.7"
val MunitVersion = "0.7.29"
val ScalaMockVersion = "5.1.0"
val ScalaTestVersion = "3.2.15"

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "simple-weather-app",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.11",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server"  % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client"  % Http4sVersion,
      "org.http4s"            %% "http4s-circe"         % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
      "io.circe"              %% "circe-generic"        % CirceVersion,
      "io.circe"              %% "circe-parser"         % CirceVersion,
      "org.scalameta"         %% "svm-subs"             % "20.2.0",
      "org.fusesource.jansi"  % "jansi"                 % "2.4.0",
      "ch.qos.logback"        %  "logback-classic"      % LogbackVersion         % Runtime,
      "org.scalameta"         %% "munit"                % MunitVersion           % Test,
      "org.scalatest"         %% "scalatest"            % ScalaTestVersion       % Test,
      "org.typelevel"         %% "munit-cats-effect-3"  % MunitCatsEffectVersion % Test,
      "org.scalamock"         %% "scalamock"            % ScalaMockVersion       % Test,
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )
