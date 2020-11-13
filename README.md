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
Testing speech recognition, Amazon lambda echo response, speech synthesis with Alexa default voice.

[![v0.1](https://img.youtube.com/vi/mIq34kkp_8I/0.jpg)](https://youtu.be/mIq34kkp_8I)

### Alexa skill configuration

Intent | Slot | Utterance | Type
------ | ---- | --------- | ----
repeat | value | december {value} | AMAZON.SearchQuery
voice | number | voice {number} | AMAZON.NUMBER

### Roadmap
* v0.1 - echo skill with aws lambda
* v0.2 - external speech synthesis
