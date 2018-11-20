package myproject.api.functions

import java.util.UUID

import myproject.api.Serializers._
import myproject.api.{ApiFunction, ApiSummaryDoc}
import myproject.common.serialization.OpaqueData.ReifiedDataWrapper
import myproject.common.serialization.OpaqueData.ReifiedDataWrapper._
import myproject.iam.Channels.ChannelDAO
import myproject.iam.Groups.GroupDAO
import myproject.iam.Users._

trait CreateUserParameters {
  def getCommonParameters(implicit p: ReifiedDataWrapper) = (
    required(p.nonEmptyString("login")),
    required(p.nonEmptyString("password")),
    required(p.email("email")),
    required(p.email("first_name")),
    required(p.email("last_name"))
  )
}

class CreatePlatformUser(implicit authz: User => UserAccessChecker, db: UserDAO with GroupDAO with ChannelDAO) extends ApiFunction with CreateUserParameters {
  override val name = "create_platform_user"
  override val doc = ApiSummaryDoc(
    description = "create a new platform user (platform users are defined at the highest possible level which is the platform) - "
      + "a platform represents the whole application instance - it contains channels, which in turn contain groups",
    `return` = "an object containing the newly created user's data")

  override def process(implicit p: ReifiedDataWrapper, user: User) = {
    val (login, password, email, fn, ln) = getCommonParameters

    implicit val checker = authz(user)

    checkParamAndProcess(login, password, email, fn, ln) {
      val user = User(UUID.randomUUID, UserLevel.Platform, login.get, fn.get, ln.get, email.get, password.get)
      CRUD.createUser(user) map (_.serialize)
    }
  }
}

class CreateChannelUser(implicit authz: User => UserAccessChecker, db: UserDAO with GroupDAO with ChannelDAO) extends ApiFunction with CreateUserParameters {
  override val name = "create_channel_user"
  override val doc = ApiSummaryDoc(
    description = "create a new channel user (channel users are users defined at the channel level (platform>channels>groups>users)) ; "
                + "they generally represents administrators of groups of users, as a channel contains groups",
    `return` = "an object containing the newly created user's data")

  override def process(implicit p: ReifiedDataWrapper, user: User) = {
    val (login, password, email, fn, ln) = getCommonParameters
    val channelId = required(p.uuid("channel_id"))

    implicit val checker = authz(user)

    checkParamAndProcess(login, password, email, channelId, fn, ln) {
      val user = User(UUID.randomUUID, UserLevel.Channel, login.get, fn.get, ln.get, email.get, password.get, Some(channelId.get))
      CRUD.createUser(user) map (_.serialize)
    }
  }
}

class CreateGroupUser(implicit authz: User => UserAccessChecker, db: UserDAO with GroupDAO with ChannelDAO) extends ApiFunction with CreateUserParameters {
  override val name = "create_group_user"
  override val doc = ApiSummaryDoc(
    description = "create a new group user (group users are users defined at the group level (platform>channels>groups>users)) ; "
      + "they generally represents end users of the application",
    `return` = "an object containing the newly created user's data")

  override def process(implicit p: ReifiedDataWrapper, user: User) = {
    val (login, password, email, fn, ln) = getCommonParameters
    val (groupId, groupRole) = (required(p.uuid("group_id")), nullable(p.enumString("group_role", GroupRole)))

    implicit val checker = authz(user)

    checkParamAndProcess(login, email, password, groupId, groupRole, fn, ln) {
      val user = User(UUID.randomUUID, UserLevel.Group, login.get, fn.get, ln.get, email.get, password.get, None, Some(groupId.get), groupRole.get)
      CRUD.createUser(user) map (_.serialize)
    }
  }
}

class CreateSimpleUser(implicit authz: User => UserAccessChecker, db: UserDAO with GroupDAO with ChannelDAO) extends ApiFunction with CreateUserParameters {
  override val name = "create_simple_user"
  override val doc = ApiSummaryDoc(
    description = "create a new simple user (TBD)",
    `return` = "an object containing the newly created user's data")

  override def process(implicit p: ReifiedDataWrapper, user: User) = {
    val (login, password, email, fn, ln) = getCommonParameters

    implicit val checker = authz(user)

    checkParamAndProcess(login, password, email, fn, ln) {
      val user = User(UUID.randomUUID, UserLevel.NoLevel, login.get, fn.get, ln.get, email.get, password.get)
      CRUD.createUser(user) map (_.serialize)
    }
  }
}
