# Changelog
All notable changes to `knotx-stack` will be documented in this file.

## Unreleased
List of changes that are finished but not yet released in any final version.
- [PR-72](https://github.com/Knotx/knotx-stack/pull/72) - knotx/knotx-fragments#79 Configuration changes & functional tests..
- [PR-69](https://github.com/Knotx/knotx-stack/pull/69) - Circuit breaker: `_fallback` transition && fallback on failure strategy

## 2.1.0
- [PR-68](https://github.com/Knotx/knotx-stack/pull/68) - Fix unit tests with custom wiremock instance.
- [PR-62](https://github.com/Knotx/knotx-stack/pull/62) - Configure task and node factories in Fragments Handler module.
- [PR-64](https://github.com/Knotx/knotx-stack/pull/64) - Add Unit tests for default stack configuration files.
- [PR-59](https://github.com/Knotx/knotx-stack/pull/59) - Remove cookie handler, it is not required from Vert.x 3.8.1.

## 2.0.0
- [PR-55](https://github.com/Knotx/knotx-stack/pull/55) - Update `fragmentsProviderHtmlSplitter` to `htmlFragmentsSupplier` and `fragmentsAssemblerHandler` to `fragmentsAssembler`.
- [PR-53](https://github.com/Knotx/knotx-stack/pull/53) - Enable [Configurable Integrations](http://knotx.io/blog/configurable-integrations/) for API Gateway.
- [PR-52](https://github.com/Knotx/knotx-stack/pull/52) - Switch to [Knot.x Gradle Plugins](https://github.com/Knotx/knotx-gradle-plugins).
- [PR-51](https://github.com/Knotx/knotx-stack/pull/51) - Moving Splitter and Assembler to [Fragments](https://github.com/Knotx/knotx-fragments) repository.
- [PR-49](https://github.com/Knotx/knotx-stack/pull/49) - Operations configuration file name updated.
- [PR-48](https://github.com/Knotx/knotx-stack/pull/48) - Build the Knot.x distribution with Gradle.
- [PR-47](https://github.com/Knotx/knotx-stack/pull/47) - Apply Gradle composite builds.
- [PR-46](https://github.com/Knotx/knotx-stack/pull/46) - Adjust to new TE configuration entries.
- [PR-45](https://github.com/Knotx/knotx-stack/pull/45) - Integration tests for `inline-body` and `inline-payload` actions, circuit breaker timeout validation.
- [PR-43](https://github.com/Knotx/knotx-stack/pull/43) - Integration tests that use Tasks & Actions and the new Data Bridge HTTP Action.
- [PR-41](https://github.com/Knotx/knotx-stack/pull/41) - Migrate to [Fragments Handler](https://github.com/Knotx/knotx-fragments) implementation.
- [PR-39](https://github.com/Knotx/knotx-stack/pull/39) - Knot.x 2.0 Stack & Unit tests.
- [PR-30](https://github.com/Knotx/knotx-stack/pull/30) - Milestone OpenAPI 3.0.

## 1.5.0
- [PR-48](https://github.com/Knotx/knotx-stack/pull/48) - Build the Knot.x distribution with Gradle.
- [PR-36](https://github.com/Knotx/knotx-stack/pull/36) - Cleanup integration tests
- [PR-35](https://github.com/Knotx/knotx-stack/pull/35) - implementation of [fallback handling](https://github.com/Cognifide/knotx/issues/466) - integration tests
- [PR-32](https://github.com/Knotx/knotx-stack/pull/32) - Extract assembler and splitter EB addresses to globals.
- [PR-33](https://github.com/Knotx/knotx-stack/pull/33) - Knot.x Template Engine introduced instead of HBS Knot
- [PR-468](https://github.com/Knotx/knotx/pull/468) - Fragment processing failure handling (configurable fallback)
- [PR-465](https://github.com/Knotx/knotx/pull/465) - Action Knot functionality moved to [Knot.x Forms](https://github.com/Knotx/knotx-forms).
- [PR-467](https://github.com/Knotx/knotx/pull/467) - fixed typo in logger format (URI already contains leading slash)
- [PR-473](https://github.com/Knotx/knotx/pull/473) - mark Handlebars Knot as deprecated on behalf of [Knot.x Template Engine](https://github.com/Knotx/knotx-template-engine).

See http://knotx.io/blog/release-1_5_0/ for more information about other modules.

## 1.4.0
- [PR-5](https://github.com/Knotx/knotx-stack/pull/5) - Knot.x Data Bridge introduced instead of Service Knot
- [PR-18](https://github.com/Knotx/knotx-stack/pull/18) - Introduced Junit5 and integration tests module
- [PR-25](https://github.com/Knotx/knotx-stack/pull/25) - Fixed http repo headers conifg
- [PR-427](https://github.com/Knotx/knotx/pull/427) - HttpRepositoryConnectorProxyImpl logging improvements
- [PR-422](https://github.com/Knotx/knotx/pull/422) - Configurable Handlebars delimiters
- [PR-428](https://github.com/Knotx/knotx/pull/428) - Mark all Service Knot related classes deprecated.
- [PR-432](https://github.com/Knotx/knotx/pull/432) - Port unit and integration tests to JUnit 5
- [PR-440](https://github.com/Knotx/knotx/pull/440) - Enable different Vert.x Config stores types fix.
- [PR-443](https://github.com/Knotx/knotx/pull/443) - Update maven plugins versions.
- [PR-445](https://github.com/Knotx/knotx/pull/445) - Vert.x version upgrade to 3.5.3
- [PR-458](https://github.com/Knotx/knotx/pull/458) - Remove unused StringToPattern function

## 1.3.0
Initial open source release.
- [PR-376](https://github.com/Knotx/knotx/pull/376) - Knot.x configurations refactor - Changed the way how configurations and it's defaults are build.
- [PR-384](https://github.com/Knotx/knotx/pull/384) - Introduce Knot.x server backpressure mechanism
- [PR-397](https://github.com/Knotx/knotx/pull/397) - Introduce vertx-config module to enable configuration modularization and auto-reload. Thanks to this change, Knot.x instance Auto-redeploy itself
 after the configuration is changed, multiple configuration files format is supported (with favouring the [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) and supporting nested configurations (includes).
- [PR-399](https://github.com/Knotx/knotx/pull/399) - Make knotx-core more concise and merge base Knot.x concepts into a single module
- [PR-404](https://github.com/Knotx/knotx/pull/404) - Refactoring of Knot.x Launcher, get rid of some messy hacks. That enables cleaner way to start Knot.x and extend launcher with custom commands.
- [PR-405](https://github.com/Knotx/knotx/pull/405) - Switched to BOM style dependencies: [`knotx-dependencies`](https://github.com/Knotx/knotx-dependencies) that define all common dependencies and their versions.
- [PR-406](https://github.com/Knotx/knotx/pull/406) - `standalone` module is not conceptually a part of Knot.x `core`. It was extracted to separate concept and will be available from now in [`knotx-stack`](https://github.com/Knotx/knotx-stack) repository.
- [PR-407](https://github.com/Knotx/knotx/pull/407) - Added vertx hooks to properly terminate instance on fatal failure, like missing configurations etc.
- [PR-411](https://github.com/Knotx/knotx/pull/411) - `example` module is not conceptually a part of Knot.x `core` and having it in the core repository was misleading. `integration-tests` module introduced here.
- [PR-412](https://github.com/Knotx/knotx/pull/412) - Knot.x `core` modules will use dependencies in `provided` scope, all dependencies will be provided by [`knotx-stack`](https://github.com/Knotx/knotx-stack) setup.
- [PR-415](https://github.com/Knotx/knotx/pull/415) - bugfix: headers configurations (e.g. `allowedHeaders`) are now case insensitive
- [PR-418](https://github.com/Knotx/knotx/pull/418) - Update to Vert.x 3.5.1
- [PR-419](https://github.com/Knotx/knotx/pull/419) - Knotx snippets parameters prefix is now customizable.
- [PR-421](https://github.com/Knotx/knotx/pull/421) - Support for system properties injection in HOCON config file
