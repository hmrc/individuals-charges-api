/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.models.request

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import config.FeatureSwitches

/** Opaque representation of a tax year.
  *
  * @param value
  *   A single-year representation, e.g. "2024" represents the tax year 2023-24.
  */
final case class TaxYear private (private val value: String) {

  /** The tax year as a number, e.g. for "2023-24" this will be 2024.
    */
  val year: Int = value.toInt

  /** The tax year in MTD (vendor-facing) format, e.g. "2023-24".
    */
  val asMtd: String = {
    val prefix  = value.take(2)
    val yearTwo = value.drop(2)
    val yearOne = (yearTwo.toInt - 1).toString
    prefix + yearOne + "-" + yearTwo
  }

  /** The tax year in the pre-TYS downstream format, e.g. "2024".
    */
  val asDownstream: String = value

  /** The tax year in the Tax Year Specific downstream format, e.g. "23-24".
    */
  val asTysDownstream: String = {
    val year2 = value.toInt - 2000
    val year1 = year2 - 1
    s"${year1}-$year2"
  }

  /** Use this for downstream API endpoints that are known to be TYS.
    */
  def useTaxYearSpecificApi(implicit featureSwitches: FeatureSwitches): Boolean = featureSwitches.isTaxYearSpecificApiEnabled && year >= 2024

  override def toString: String = s"TaxYear($value)"
}

//
///** Represents a tax year for DES
//  *
//  * @param value
//  *   the tax year string (2017-18)
//  */
//case class TaxYear(value: String) extends AnyVal {
//  override def toString: String = value
//}
//
//object TaxYear {
//
//  val startOfYear      = 2
//  val startYearAndDash = 5
//
//  // TODO MOVE TO VALIDATION ONLY
//  def toYearYYYY(taxYear: String): TaxYear = TaxYear(taxYear.take(startOfYear) + taxYear.drop(startYearAndDash))
//
//  /** Converts YYYY year to MTD year YYYY-YY. E.g. 2018 -> 2017-18
//    *
//    * @param taxYear
//    *   the tax year string (2018)
//    */
//  def toMTDYear(taxYear: String): TaxYear =
//    TaxYear((taxYear.toInt - 1) + "-" + taxYear.drop(startOfYear))
//
//  // TODO UPDATE IF NEEDED TO USE 2017-18 FORMAT
//  def mostRecentTaxYear(date: LocalDate = LocalDate.now()): TaxYear = {
//    val limit = LocalDate.parse(s"${date.getYear}-04-05", DateTimeFormatter.ISO_DATE)
//    if (date.isBefore(limit)) {
//      TaxYear(s"${date.getYear - 1}")
//    } else {
//      TaxYear(s"${date.getYear}")
//    }
//  }
//
//}

object TaxYear {

  /** @param taxYear
    *   tax year in MTD format (e.g. 2017-18)
    */
  def fromMtd(taxYear: String): TaxYear =
    new TaxYear(taxYear.take(2) + taxYear.drop(5))

  def fromDownstream(taxYear: String): TaxYear =
    new TaxYear(taxYear)

  def fromDownstreamInt(taxYear: Int): TaxYear =
    new TaxYear(taxYear.toString)

  def mostRecentTaxYear(date: LocalDate = LocalDate.now()): TaxYear = {
    val limit = LocalDate.parse(s"${date.getYear}-04-05", DateTimeFormatter.ISO_DATE)
    if (date.isBefore(limit)) {
      TaxYear(s"${date.getYear - 1}")
    } else {
      TaxYear(s"${date.getYear}")
    }
  }

}
