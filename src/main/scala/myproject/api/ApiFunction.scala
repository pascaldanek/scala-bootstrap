package myproject.api

import myproject.audit.Audit.AuditData
import myproject.common.serialization.OpaqueData.ReifiedDataWrapper
import myproject.common.{InvalidContextException, NotImplementedException, Runtime}
import myproject.iam.Users.UserGeneric

import scala.concurrent.Future
import scala.language.implicitConversions

trait ApiFunction {
  val name: String
  val description: String
  val secured: Boolean = true

  protected implicit val ec = Runtime.ec

  def process(implicit p: ReifiedDataWrapper, effectiveUser: UserGeneric, auditData: AuditData): Future[Any] = {
    if(!secured)
      Future.failed(InvalidContextException("This function does not need a valid authentication and should probably be called using the simple signature"))

    Future.failed(NotImplementedException("This function is not implemented"))
  }

  def process(implicit p: ReifiedDataWrapper, auditData: AuditData): Future[Any] = {

    if(secured)
      Future.failed(InvalidContextException("This function needs a valid authentication and should probably be called using the secured signature"))

    Future.failed(NotImplementedException("This function is not implemented"))
  }
}