import sbt._

class DepLoaderProject(info: ProjectInfo) extends DefaultProject(info) {
    val scalaQuery = "org.scalaquery" %% "scalaquery" % "0.9.0"
}

