package myproject.iam

import java.util.UUID

import myproject.common.FutureImplicits._
import myproject.common.security.JWT
import myproject.common.{AuthenticationFailedException, ObjectNotFoundException}
import myproject.iam.Channels.Channel
import myproject.iam.Groups.Group
import myproject.iam.Users.CRUD._
import myproject.iam.Users.{User, UserRole}
import org.scalatest.DoNotDiscover
import test.DatabaseSpec
import uk.gov.hmrc.emailaddress.EmailAddress

@DoNotDiscover
class UserSpecs extends DatabaseSpec {

  val channel = Channel(UUID.randomUUID, "TEST")
  val group = Group(UUID.randomUUID, "ACME", channel.id)
  val jdoe = User(UUID.randomUUID, "user-specs", "Kondor_123", None, Some(group.id), UserRole.GroupUser, EmailAddress("user-specs@tests.com"))

  it should "create a user" in {
    createUser(jdoe).futureValue.login shouldBe jdoe.login
  }

  it should "get the created user by id" in {
    getUser(jdoe.id).futureValue.login shouldBe jdoe.login
  }

  it should "not log in the user with incorrect password" in {
    a [AuthenticationFailedException] shouldBe thrownBy(
      loginPassword(jdoe.login, "incorrect").futureValue
    )
  }

  it should "not log in a non existent user" in {
    a [ObjectNotFoundException] shouldBe thrownBy(
      loginPassword("non-existent", "Kondor_123").futureValue
    )
  }

  it should "log in the user" in {
    val (user, token) = loginPassword(jdoe.login,jdoe.password).futureValue
    user.id shouldBe jdoe.id
    JWT.extractToken(token).right.get.uid shouldBe jdoe.id
  }

  it should "update the user" in {
    updateUser(jdoe.copy(login = "smith")).futureValue
    getUser(jdoe.id).futureValue.login shouldBe "smith"
  }

  it should "delete the user" in {
    deleteUser(jdoe.id).futureValue
    a [ObjectNotFoundException] shouldBe thrownBy(getUser(jdoe.id).futureValue)
  }
}
