package v3.winterFuelPayment.createAmend

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{InternalError, NinoFormatError, TaxYearFormatError}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v3.winterFuelPayment.createAmend.models.request.CreateAmendWinterFuelPaymentRequestData
import v3.winterFuelPayment.createAmend.fixture.CreateAmendWinterFuelPaymentFixtures.*

import scala.concurrent.Future

class CreateAmendWinterFuelPaymentConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    self: ConnectorTest =>

    def taxYear: TaxYear

    protected val request: CreateAmendWinterFuelPaymentRequestData= CreateAmendWinterFuelPaymentRequestData(
      nino = Nino(nino),
      taxYear = taxYear,
      body = requestBodyModel
    )

    val connector: CreateAmendWinterFuelPaymentConnector =
      new CreateAmendWinterFuelPaymentConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

  }

  "winter fuel payment" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val expected = Right(ResponseWrapper(correlationId, ()))

        willPut(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}", requestBodyModel)
          .returns(Future.successful(expected))

        await(connector.createAmend(request)) shouldBe expected
      }
    }

    "A request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        willPut(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}", requestBodyModel)
          .returns(Future.successful(expected))

        await(connector.createAmend(request)) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError, TaxYearFormatError)))

        willPut(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}", requestBodyModel)
          .returns(Future.successful(expected))

        await(connector.createAmend(request)) shouldBe expected
      }
    }
  }

}

