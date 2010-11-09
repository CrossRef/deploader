import sbt._

class DepLoaderProject(info: ProjectInfo) extends DefaultProject(info) {
    val liftWebkit = "net.liftweb" %% "lift-webkit" % "2.1"
    val liftMapper = "net.liftweb" %% "lift-mapper" % "2.1"

    override def fork = Some(new ForkScalaRun {
        override def runJVMOptions = super.runJVMOptions ++ Seq("-Xmx2048m")
        override def scalaJars = Seq(buildLibraryJar.asFile, buildCompilerJar.asFile)
    })

}

