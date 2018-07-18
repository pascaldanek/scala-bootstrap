package myproject.iam.dao

import java.util.UUID

import myproject.common.Done
import myproject.common.Runtime.ec
import myproject.database.DAO
import myproject.iam.Groups.Group

trait GroupDAO extends DAO { self: ChannelDAO =>

  import api._

  protected class GroupsTable(tag: Tag) extends Table[Group](tag, "GROUPS") {
    def id = column[UUID]("GROUP_ID", O.PrimaryKey, O.SqlType("UUID"))
    def name = column[String]("NAME")
    def channelId = column[UUID]("CHANNEL_ID", O.SqlType("UUID"))
    def channel = foreignKey("CHANNEL_FK", channelId, channels)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def * = (id, name, channelId) <> (Group.tupled, Group.unapply)
  }

  protected val groups = TableQuery[GroupsTable]

  def getGroup(id: UUID) = db.run(groups.filter(_.id===id).result) map (_.headOption)
  def insert(group: Group) = db.run(groups += group) map (_ => group)
  def update(group: Group) = db.run(groups.filter(_.id===group.id).update(group)) map (_ => group)
  def deleteGroup(id: UUID) = db.run(groups.filter(_.id===id).delete) map (_ => Done)
}
