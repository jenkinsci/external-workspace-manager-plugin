# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## 1.2.2 - 2020-01-19
### Changed
 - Upgrade to use JCasC Test Harness [#73](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/73)

## 1.2.1 - 2019-11-01
### Changed
 - Supported Jenkins version is 2.150.3 or newer
 - Use `FileDynamicPathContext` for compatibility with Jenkins 2.150.x+

## 1.2.0 - 2019-08-09
### Added
 - [[JENKINS-57927]](https://issues.jenkins-ci.org/browse/JENKINS-57927) JCasC support in the plugin [#68](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/68)

## 1.1.2 - 2017-07-30
### Fixed
- Add delete disk pool button to template and nodes
[#49](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/49)

## 1.1.1 - 2016-11-08
### Fixed
- [[JENKINS-39198]](https://issues.jenkins-ci.org/browse/JENKINS-39198) Fix workspace allocation in parallel test 
execution [#48](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/48)

## 1.1.0 - 2016-09-10
### Added
- [[JENKINS-37773]](https://issues.jenkins-ci.org/browse/JENKINS-37773) Browse the workspace directory from the 
_Allocated Workspaces_ view [#44](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/44)
- [[JENKINS-36413]](https://issues.jenkins-ci.org/browse/JENKINS-36413) Integrate Fingerprints Engine; view each 
external workspace in which other jobs/builds has been used 
[#46](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/46)

### Changed
- Rename _Local root path_ to _Node mount point_ from External Workspace Node property
[#45](https://github.com/jenkinsci/external-workspace-manager-plugin/pull/45)

## 1.0.0 - 2016-08-21

- Initial release, see documentation for all the available features.
