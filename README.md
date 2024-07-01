Individuals Charges API
========================
This API allows software packages to show and provide a taxpayers' financial data for their charges. For pension charges
a developer can:

* retrieve pension charges
* create and amend pension charges
* delete pension charges

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.9.7
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development Setup

Run from the console using: `sbt run` (starts on port 9765 by default)

Start the service manager profile: `sm2 --start MTDFB_CHARGES`

## Running tests

```
sbt test
sbt it:test
```

## Viewing OAS

To view documentation locally ensure the Charges API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use the appropriate port and version:
`http://localhost:9765/api/conf/2.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [Individuals Charges Documentation](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individuals-charges-api)

### License

This code is open source software licensed under
the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).