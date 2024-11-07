package computerdatabase


import io.gatling.core.Predef._
import io.gatling.http.Predef._


class RegistrSimulation extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/png,image/svg+xml,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/128.0")

  private val headers_0 = Map(
    "Priority" -> "u=0, i",
    "Sec-Fetch-Dest" -> "document",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1"
  )

  val userFeeder = csv("users.csv").circular()


  val registry = scenario("registry")
    .exec (
      http("index")
        .get("/")
        .headers(headers_0)
        .check(
          status.is(200),
          responseTimeInMillis.lte(100),
        ))
    .pause(2)
    .feed(userFeeder)
    .exec(
      http("login")
        .get("/login")
        .headers(headers_0)
        .check(
          css(".mdc-card > form:nth-child(2) > input:nth-child(1)").saveAs("_csrf"),
          status.is(200),
          responseTimeInMillis.lte(100),
        ))
//    .exec { session => println(session); session}
//    .exec { session => println(session("responseBody").as[String]); session}
    .exec(
      http("login")
        .post("/login")
        .headers(headers_0)
        .formParam("_csrf", "#{_csrf}")
        .formParam("username", "#{username}")
        .formParam("password", "#{password}")
        .check(
          status.is(200),
          responseTimeInMillis.lte(100),
        ))
    .pause(2)
    .exec(
      http("set_account")
        .post("/account/add")
        .headers(headers_0)
        .formParam("_csrf", "#{_csrf}")
        .formParam("customerName", "#{username}")
        .formParam("customerEmail", "dfg@dfg.ru")
        .formParam("accountType", "test")
        .formParam("balance", "3")
    )
    .exec(
      http("logout")
        .post("/logout")
        .headers(headers_0)
        .check(
          status.is(200),
          responseTimeInMillis.lte(100)
        )
    )
//  setUp(registry.inject(atOnceUsers(1))).protocols(httpProtocol)
  setUp(registry.inject(constantUsersPerSec(20).during(15)).protocols(httpProtocol))
}