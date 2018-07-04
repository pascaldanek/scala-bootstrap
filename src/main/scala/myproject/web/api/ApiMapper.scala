package myproject.web.api

import myproject.audit.{AuditData, AuditUserInfo}
import myproject.common.serialization.ReifiedDataWrapper
import myproject.common.{AccessRefusedException, AuthenticationNeededException, DefaultExecutionContext, ObjectNotFoundException}
import myproject.modules.iam.pure.AccessControl
import myproject.modules.iam.{Guest, User}

import scala.concurrent.Future

trait ApiMapper extends {} with DefaultExecutionContext with AccessControl {

  def dispatchRequest(
    realUser: User,
    effectiveUserOpt: Option[User],
    functionName: String,
    clientIp: Option[String])(implicit params: ReifiedDataWrapper): Future[Any] = {

    def processInsecure(function: ApiFunction): Future[Any] = function.process(params, AuditData(clientIp, None))

    def processSecure(function: ApiFunction): Future[Any] = realUser match {

      case Guest() =>
        Future.failed(AuthenticationNeededException(s"Access to function `${function.name}` requires valid authentication"))

      case rUser =>
        effectiveUserOpt map { effectiveUser =>
          canLogin(effectiveUser) flatMap { _ =>
            canImpersonate(rUser, effectiveUserOpt.get)
          } match {
            case Right(_) => Future.successful(effectiveUser)
            case Left(msg) => Future.failed(AccessRefusedException(msg))
          }
        } getOrElse Future.successful(rUser) flatMap { implicit eUser =>
          function.process(params, eUser, AuditData(clientIp, Some(AuditUserInfo(rUser, eUser))))
        }
    }

    /* We search a function with the corresponding name and execute it */
    ApiFunctionsRegistry.Functions.find(_.name.trim == functionName) match {
      case None => Future.failed(ObjectNotFoundException(s"Function with name `$functionName` was not found"))
      case Some(method) if !method.secured=>
        processInsecure(method)
      case Some(method) =>
        processSecure(method)
    }
  }
}
