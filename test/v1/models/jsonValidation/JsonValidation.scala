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

package v1.models.jsonValidation

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import play.api.mvc.AnyContentAsJson

trait JsonValidation {
  def isValidateJsonAccordingToJsonSchema(inputDoc: AnyContentAsJson, schemaDoc: String): Boolean = {
    try {
      val schemaFileInputStream = getClass.getResourceAsStream(schemaDoc)
      val schemaString = scala.io.Source.fromInputStream(schemaFileInputStream).getLines().mkString("\n")
      val mapper: ObjectMapper = new ObjectMapper()
      val inputJson: JsonNode = mapper.readTree(inputDoc.asJson.get.toString())
      val jsonSchema: JsonNode = mapper.readTree(schemaString)
      val factory = JsonSchemaFactory.byDefault()
      val validator: JsonValidator = factory.getValidator
      val report: ProcessingReport = validator.validate(jsonSchema, inputJson)
      report.isSuccess
    } catch {
      case e: Exception => false
    }
  }
}
