package myproject

import com.typesafe.config.ConfigFactory

object Config {

  private val config = ConfigFactory.load()

  // server
  object server {
    private val srvConfig = config.getConfig("server")
    val interface = srvConfig.getString("interface")
    val port = srvConfig.getInt("port")
  }

  //jwt
  val secret = config.getString("secret")

}