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

package v3.pensionCharges.delete

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v3.pensionCharges.delete.model.request.DeletePensionChargesRequestData

trait MockDeletePensionChargesValidatorFactory extends TestSuite with MockFactory {

  val mockDeletePensionChargesValidatorFactory: DeletePensionChargesValidatorFactory = mock[DeletePensionChargesValidatorFactory]

  object MockedDeletePensionChargesValidatorFactory {

    def validator(): CallHandler[Validator[DeletePensionChargesRequestData]] =
      (mockDeletePensionChargesValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeletePensionChargesRequestData]): CallHandler[Validator[DeletePensionChargesRequestData]] = {
    MockedDeletePensionChargesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeletePensionChargesRequestData): Validator[DeletePensionChargesRequestData] =
    new Validator[DeletePensionChargesRequestData] {
      def validate: Validated[Seq[MtdError], DeletePensionChargesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeletePensionChargesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeletePensionChargesRequestData] =
    new Validator[DeletePensionChargesRequestData] {
      def validate: Validated[Seq[MtdError], DeletePensionChargesRequestData] = Invalid(result)
    }

}
