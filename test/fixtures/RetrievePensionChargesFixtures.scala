/*
 * Copyright 2020 HM Revenue & Customs
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

package fixtures

import v1.models.des._

object RetrievePensionChargesFixtures {

  val pensionSavingsCharge: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    Seq("00123456RA","00123456RA"),
    Some(LifetimeAllowance(100.00, 100.00)),
    Some(LifetimeAllowance(100.00, 100.00)),
    Some(true),
    Some(true),
    Some(true),
  )
  val overseasSchemeProvider: OverseasSchemeProvider =  OverseasSchemeProvider(
    "name",
    "address",
    "postcode",
    Seq("Q123456")
  )
  val pensionOverseasTransfer : PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    Seq(overseasSchemeProvider),
    100.00,
    100.00
  )
  val pensionUnauthorisedPayments : PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(
    Seq("00123456RA", "00123456RA"),
    Some(Charge(100.00, 100.00)),
    Some(Charge(100.00, 100.00))
  )
  val pensionContributions: PensionContributions = PensionContributions(
    Seq("00123456RA", "00123456RA"),
    100.00,
    100.00
  )
  val overseasPensionContributions : OverseasPensionContributions = OverseasPensionContributions (
    overseasSchemeProvider,
    100.00,
    100.00
  )
  val retrieveResponse: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
    Some(pensionSavingsCharge),
    Some(pensionOverseasTransfer),
    Some(pensionUnauthorisedPayments),
    Some(pensionContributions),
    Some(overseasPensionContributions)
  )

}
