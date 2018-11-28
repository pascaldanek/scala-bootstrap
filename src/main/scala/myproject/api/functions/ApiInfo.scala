package myproject.api.functions

import java.time.{Instant, LocalDateTime, ZoneId}

import buildmeta.BuildInfo
import myproject.api.{ApiFunction, ApiSummaryDoc}
import myproject.common.serialization.OpaqueData

import scala.concurrent.Future

class ApiInfo extends ApiFunction {
  override val name = "api_info"
  override val doc = ApiSummaryDoc("get the current API metadata", "an object containing the api metadata")
  override val secured = false

  override def process(implicit p: OpaqueData.ReifiedDataWrapper) = {
    Future.successful(
      Map(
      "name" -> BuildInfo.name,
      "version" -> BuildInfo.version,
      "build_number" -> BuildInfo.buildInfoBuildNumber,
      "build_at" -> (LocalDateTime.ofInstant(Instant.ofEpochMilli(BuildInfo.builtAtMillis), ZoneId.of("GMT")).toString + "Z"))
    )
  }
}
