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

package shared.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.models.errors.{CountryCodeFormatError, MtdError, RuleCountryCodeError}

object ResolveParsedCountryCode {

  private[resolvers] val permittedCodes = Set(
    "ALB",
    "DZA",
    "ATG",
    "ARG",
    "ARM",
    "AUS",
    "AUT",
    "AZE",
    "BHR",
    "BGD",
    "BRB",
    "BLR",
    "BEL",
    "BLZ",
    "BOL",
    "BIH",
    "BWA",
    "VGB",
    "BRN",
    "BGR",
    "MMR",
    "CAN",
    "CYM",
    "CHL",
    "CHN",
    "CXR",
    "CCK",
    "CIV",
    "HRV",
    "CYP",
    "CZE",
    "DNK",
    "EGY",
    "EST",
    "ETH",
    "FLK",
    "FRO",
    "FJI",
    "FIN",
    "FRA",
    "GUF",
    "GMB",
    "GEO",
    "DEU",
    "GHA",
    "GRC",
    "GRD",
    "GLP",
    "GGY",
    "GUY",
    "HKG",
    "HUN",
    "ISL",
    "IND",
    "IDN",
    "IRL",
    "IMN",
    "ISR",
    "ITA",
    "JAM",
    "JPN",
    "JEY",
    "JOR",
    "KAZ",
    "KEN",
    "KIR",
    "XKX",
    "KWT",
    "LVA",
    "LSO",
    "LBY",
    "LIE",
    "LTU",
    "LUX",
    "MKD",
    "MWI",
    "MYS",
    "MLT",
    "MTQ",
    "MUS",
    "MEX",
    "MDA",
    "MNG",
    "MNE",
    "MSR",
    "MAR",
    "NAM",
    "NLD",
    "NZL",
    "NGA",
    "NFK",
    "NOR",
    "OMN",
    "PAK",
    "PAN",
    "PNG",
    "PHL",
    "POL",
    "PRT",
    "QAT",
    "REU",
    "ROU",
    "RUS",
    "KNA",
    "SAU",
    "SEN",
    "SRB",
    "SLE",
    "SGP",
    "SVK",
    "SVN",
    "SLB",
    "ZAF",
    "KOR",
    "ESP",
    "LKA",
    "SDN",
    "SWZ",
    "SWE",
    "CHE",
    "TWN",
    "TJK",
    "THA",
    "TTO",
    "TUN",
    "TUR",
    "TKM",
    "TUV",
    "UGA",
    "UKR",
    "ARE",
    "USA",
    "URY",
    "UZB",
    "VEN",
    "VNM",
    "ZMB",
    "ZWE"
  )
  
  def apply(value: String, path: String): Validated[List[MtdError], String] = {
    if (value.length != 3) {
      Invalid(List(CountryCodeFormatError.withPath(path)))
    } else if (permittedCodes.contains(value)) {
      Valid(value)
    } else {
      Invalid(List(RuleCountryCodeError.withPath(path)))
    }
  }

  def apply(maybeValue: Option[String], path: String): Validated[List[MtdError], Option[String]] = {
    maybeValue match {
      case Some(value) => apply(value, path).map(Option(_))
      case None        => Valid(None)
    }
  }
}
