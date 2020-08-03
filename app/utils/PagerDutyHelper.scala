/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package utils

import play.api.Logger

object PagerDutyHelper {
  val logger: Logger = Logger("PagerDutyLogger")

  object PagerDutyKeys extends Enumeration {
    val DES_INTERNAL_SERVER_ERROR: PagerDutyKeys.Value = Value
    val DES_SERVICE_UNAVAILABLE: PagerDutyKeys.Value = Value
    val DES_UNEXPECTED_RESPONSE: PagerDutyKeys.Value = Value
  }

  def pagerDutyLog(pagerDutyKey: PagerDutyKeys.Value, otherDetail: Option[String] = None): Unit = {
    logger.error(s"$pagerDutyKey ${otherDetail.getOrElse("")}")
  }
}
