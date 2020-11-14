# December - the business coach chatbot

### Purpose
* Providing insights on setting the business goals and increasing profit
* Assisting in personal development

### Application design v1
* Artificial intelligence engine configured by AIML
* Natural language processing with speech recognition
* Pluggable text-to-speech engines
* Customizable voices
* Hardware devices connected by Alexa Skills API

### v0.1 "december repeat"
Testing speech recognition, Amazon lambda echo response, speech synthesis with Alexa default voice. ([video](https://youtu.be/mIq34kkp_8I))

### v0.2 "select voice"
Testing text-to-speech voices of Kimberly and Matthew (Amazon Polly) and James (IBM Watson) ([video](https://youtu.be/NnLe39vKsyU))

![v0.2](https://img.youtube.com/vi/NnLe39vKsyU/0.jpg)

### Alexa skill configuration

Intent | Slot | Utterance | Type
------ | ---- | --------- | ----
repeat | value | december {value} | AMAZON.SearchQuery
voice | number | voice {number} | AMAZON.NUMBER

### Roadmap
* v0.1 - echo skill with aws lambda
* v0.2 - external speech synthesis
* v0.3 - weather radio (AWOS/NOAA/CMB style)
