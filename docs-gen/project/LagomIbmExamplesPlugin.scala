import sbt._
import Keys._
import com.lightbend.paradox.sbt.ParadoxPlugin

import scala.collection.immutable

object LagomIbmExamplesPlugin extends sbt.AutoPlugin {
  override def requires = ParadoxPlugin

  override def trigger = allRequirements

  object autoImport {
    val baseUrl = settingKey[String]("")
    val baseProject = settingKey[String]("")
    val templateName = settingKey[String]("")
    val bodyPrefix = settingKey[String]("")
    val bodyTransformation = settingKey[String => String]("")
  }

  import autoImport._
  import ParadoxPlugin.autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    baseUrl := "https://github.com/lagom/ibm-integration-examples/blob/master",
    crossPaths := false,
    // Copy README.md file
    sourceDirectory in(Compile, paradox) := {
      val outDir = (managedSourceDirectories in Compile).value.head / "paradox"
      val outFile = outDir / "index.md"
      val inDir = baseDirectory.value / ".." / ".." / baseProject.value
      val inFile = inDir / "README.md"
      IO.write(outFile, bodyPrefix.value + bodyTransformation.value(IO.read(inFile)))
      if ((inDir / "tutorial").exists) {
        IO.copyDirectory(inDir / "tutorial", outDir / "tutorial")
      }
      outDir
    },
    paradoxProperties += ("download_url" -> s"https://example.lightbend.com/v1/download/${templateName.value}"),
    bodyPrefix := "" ,
    bodyTransformation := { case body =>
      val r = """\[([^]]+)\]\(([^)]+)\)""".r
      r.replaceAllIn(body,
        _ match {
          case r(lbl, uri) if (uri.startsWith("#") || uri.startsWith("http")) =>
            s"[$lbl]($uri)"
          case r(lbl, uri) =>
            s"""[$lbl](${baseUrl.value}/${baseProject.value}/$uri)"""

        }
      )
    }
    ,
    templateName := baseProject.value.replaceAll("-sample-", "-samples-")
  )
}
