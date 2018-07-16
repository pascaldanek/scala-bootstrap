package myproject.iam

import java.util.UUID

import myproject.common.Runtime.ec
import myproject.database.DB

import scala.concurrent.Future

object Domains {

  case class Domain(id: UUID, name: String)

  sealed trait DomainUpdate
  case class UpdateName(name: String) extends DomainUpdate

  def updateDomain(domain: Domain, updates: List[DomainUpdate]) = updates.foldLeft(domain) { case (updated, upd) => upd match {
      case UpdateName(name) => updated.copy(name = name)
    }
  }

  def newDomain(name: String) = Domain(UUID.randomUUID(), name)

  object CRUD {
    def createDomain(name: String) = DB.insert(newDomain(name))
    def getDomain(id: UUID) = DB.getDomain(id)
    def updateDomain(id: UUID, updates: List[DomainUpdate]) =
      DB.getDomain(id) map (Domains.updateDomain(_, updates)) flatMap DB.update
    def updateDomain(id: UUID, update: DomainUpdate): Future[Domain] = updateDomain(id, List(update))
    def deleteDomain(id: UUID) = DB.deleteDomain(id)
  }
}