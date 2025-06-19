/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.pensionCharges.createAmend

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v3.pensionCharges.createAmend.CreateAmendPensionChargesValidatorFactory
import v3.pensionCharges.createAmend.model.request.CreateAmendPensionChargesRequestData

trait MockCreateAmendPensionChargesValidatorFactory extends TestSuite with MockFactory {

  val mockAmendPensionChargesValidatorFactory: CreateAmendPensionChargesValidatorFactory =
    mock[CreateAmendPensionChargesValidatorFactory]

  object MockedAmendPensionChargesValidatorFactory {

    def validator(): CallHandler[Validator[CreateAmendPensionChargesRequestData]] =
      (mockAmendPensionChargesValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[CreateAmendPensionChargesRequestData]): CallHandler[Validator[CreateAmendPensionChargesRequestData]] = {
    MockedAmendPensionChargesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateAmendPensionChargesRequestData): Validator[CreateAmendPensionChargesRequestData] =
    new Validator[CreateAmendPensionChargesRequestData] {
      def validate: Validated[Seq[MtdError], CreateAmendPensionChargesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateAmendPensionChargesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateAmendPensionChargesRequestData] =
    new Validator[CreateAmendPensionChargesRequestData] {
      def validate: Validated[Seq[MtdError], CreateAmendPensionChargesRequestData] = Invalid(result)
    }

}
