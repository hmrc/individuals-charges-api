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

package v1.models.request.AmendPensionCharges

import play.api.libs.json.Json
import support.UnitSpec
import v1.data.AmendPensionChargesData._

class PensionChargesSpec extends UnitSpec {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        fullJson.as[PensionCharges] shouldBe pensionChargesCl102FieldsInTaxCharges
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(pensionChargesCl102FieldsInTaxCharges) shouldBe fullJson
      }
    }
  }

  "withCl102Changes" when {
    "fields are present in tax charges and contributions object exists" should {
      "move fields successfully" in {
        pensionChargesCl102FieldsInTaxCharges.withCl102Changes shouldBe Some(pensionChargesCl102FieldsInPensionContributions)
      }
    }

    /** The pensionContributions object contains other mandatory fields. If it is not present, we cannot create it ourselves as we have no values for
      * these mandatory fields. This means we cannot move the CL102 fields from tax charges. Instead, return None, this will translate to a 500
      * Internal Server Error for vendors.
      */
    "tax charges object exists but contributions object does not exist" should {
      "return None" in {
        pensionChargesPensionContributionsMissing.withCl102Changes shouldBe None
      }
    }

    "tax charges does not exist" should {
      "return model as-is" in {
        val requestModel: PensionCharges = PensionCharges(
          None,
          Some(pensionOverseasTransfer),
          Some(pensionUnauthorisedPayments),
          Some(pensionContributionsWithoutCl102Fields),
          Some(overseasPensionContributions)
        )

        requestModel.withCl102Changes shouldBe Some(requestModel)
      }
    }
  }

}
