lazy val `lagom-cloud-object-storage-example` = project
  .enablePlugins(LagomIbmExamplesPlugin)
  .settings(
    name        := "Lagom integration with IBM Cloud Object Storage",
    baseProject := "lagom-cloud-object-storage-example"
  )

lazy val `lagom-jpa-db2-example` = project
  .enablePlugins(LagomIbmExamplesPlugin)
  .settings(
    name        := "Lagom integration with IBM Db2 and JPA",
    baseProject := "lagom-jpa-db2-example"
  )

lazy val `lagom-message-hub-example` = project
  .enablePlugins(LagomIbmExamplesPlugin)
  .settings(
    name        := "Lagom integration with IBM Message Hub",
    baseProject := "lagom-message-hub-example"
  )

lazy val `lagom-message-hub-liberty-integration-example` = project
  .enablePlugins(LagomIbmExamplesPlugin)
  .settings(
    name        := "Lagom integration with IBM Message Hub and WebSphere Liberty",
    baseProject := "lagom-message-hub-liberty-integration-example"
  )
