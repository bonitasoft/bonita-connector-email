# Email Connector

[![Build](https://github.com/bonitasoft/bonita-connector-email/actions/workflows/build.yml/badge.svg)](https://github.com/bonitasoft/bonita-connector-email/actions/workflows/build.yml)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=bonitasoft_bonita-connector-email&metric=alert_status)](https://sonarcloud.io/dashboard?id=bonitasoft_bonita-connector-email)
[![GitHub release](https://img.shields.io/github/v/release/bonitasoft/bonita-connector-email?color=blue&label=Release)](https://github.com/bonitasoft/bonita-connector-email/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.bonitasoft.connectors/bonita-connector-email.svg?label=Maven%20Central&color=orange)](https://search.maven.org/search?q=g:%22org.bonitasoft.connectors%22%20AND%20a:%22bonita-connector-email%22)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

## Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`./mvnw clean package`

## Release

In order to create a new release: 
- On the release branch, make sure to update the pom version (remove the -SNAPSHOT)
- Run the 'Release' workflow from the release branch, set the version to release as parameter
- When the action is completed, do not forget to manage the release on the nexus (close and release)
- Update the `master` with the next SNAPSHOT version.

## Contributing

We would love you to contribute, pull requests are welcome! Please see the [CONTRIBUTING.md](CONTRIBUTING.md) for more information.

## License

The sources and documentation in this project are released under the [GPLv2 License](LICENSE)

