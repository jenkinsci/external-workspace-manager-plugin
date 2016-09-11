# External Workspace Manager Plugin

[![Join the chat at https://gitter.im/jenkinsci/external-workspace-manager-plugin](https://badges.gitter.im/jenkinsci/external-workspace-manager-plugin.svg)](https://gitter.im/jenkinsci/external-workspace-manager-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/external-workspace-manager-plugin/master)](https://ci.jenkins.io/job/Plugins/job/external-workspace-manager-plugin/job/master/)

This plugin provides an external workspace management system.
It facilitates workspace share and reuse across multiple Jenkins jobs.
It eliminates the need to copy, archive or move files.

## Table of contents
1. [Changelog](CHANGELOG.md)
1. [Design document](#design-document)
1. [Prerequisites](doc/PREREQUISITES.md)
1. [Pipeline examples](doc/PIPELINE_EXAMPLES.md)
    2. [Workspace reuse in same job](doc/PIPELINE_EXAMPLES.md#workspace-reuse-in-same-job)
    2. [Workspace reuse in two different jobs](doc/PIPELINE_EXAMPLES.md#workspace-reuse-in-two-different-jobs)
1. [Workspace cleanup](doc/WORKSPACE_CLEANUP.md)
1. [Custom workspace path](doc/CUSTOM_WORKSPACE_PATH.md)
1. [Disk Pool restriction](doc/DISK_POOL_RESTRICTION.md)
1. [Disk allocation strategies](doc/ALLOCATION_STRATEGIES.md)

## Design document

The design document may be accessed [here](https://docs.google.com/document/d/1yiisnsR7qg3XEEvch8vocWbitSUCZcoQ-pfzEVFg1eA/edit?usp=sharing).
