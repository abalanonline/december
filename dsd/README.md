# December Stream Driver

Start Date: 2021-05-19

### Background

In the last 0.6 version the content generation routines were coupled with the content delivery in the same service.
So we have to stop the service to make changes. Result - no continuous delivery.

### Goal

To build an application that will broadcast the audio content in real time.

### Objectives

* 24/7 live radio broadcast
* Stream to Amazon Alexa, Google Assistant, YouTube Live

### Roadmap

* v0.1 - test signal to Alexa
* v0.2 - time-synced playlist
* v0.3 - other destinations
* v0.4 - docker version
* v1.0 - bugfixes
