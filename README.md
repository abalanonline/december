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

### v0.2.1 "list voices"
Play all the en-us/uk voices from all the providers. More than forty of them including very special ones. ([video](https://youtu.be/_oEXTOOjgpo))

### v0.3 "december weather"
Automated radio that broadcast weather forecasts over the GMRS radio channel. Open source replica of NOAA radio. 
The rig generate synthesized voice reports 24/7 and can be used as a base for commercial product. ([video](https://youtu.be/ZXkDPnFoQPc))

### v0.3.1
The same as 0.3, but with AWOS aviation broadcast text and METAR provider.

### v0.4
On hold, building neural network hardware.

### v0.5 "december repeat google"
Test of google home hardware. Device bilingual speech recognition, google assistant actions integrated with java webhooks, 
speech synthesis with on-prem text-to-speech engine. ([video](https://youtu.be/zjI9jDvpl4M))
* OpenTTS: https://github.com/synesthesiam/opentts
* MaryTTS: http://mary.dfki.de/
* French voice: marytts:enst-dennys-hsmm by https://www.telecom-paris.fr/
* English voice: marytts:cmu-rms-hsmm by http://festvox.org/cmu_arctic/

### v0.6 "cowboys"
Test of various hardware (Google and Amazon), speech engines (IBM, Google and Amazon) and languages (French, English) at once.
The devices can keep a simple partially scripted conversation. ([video](https://youtu.be/8XPig8gUBVs))
* Left: Echo Dot 3rd gen, Google fr-CA-Wavenet-A (French Canadian), IBM Kate (British)
* Right: Home Mini 1st gen, IBM Nicolas (French), Amazon Brian (British)

![v0.6](https://img.youtube.com/vi/8XPig8gUBVs/mqdefault.jpg)

### Alexa skill configuration

* Skill: Custom, Provision your own, Start from scratch
* Intent: intent, utterance: ok {slot}, dialog delegation: enable
* Slot: slot, type: AMAZON.SearchQuery, required: yes, prompt: any text
* Endpoint: https://my-free-secured-dynamic-domain.ddns.net/alexa/repeat
* Interfaces: audio player, auto delegation
* Test: enable in development

### Google assistant actions configuration

* One type of any name supporting free form text
* One scene of any name with one slot of any name and type free form text
* Main invocation transition to the created scene
* Webhook https://my-free-secured-dynamic-domain.ddns.net/ga/repeat where repeat is a skill/bot name
* Webhook calls: in main invocation, in scene slot validation, slot prompts for no input (optional)

### Roadmap
* v0.1 - echo skill with aws lambda
* v0.2 - external speech synthesis
* v0.3 - weather radio (AWOS/NOAA/CMB style)
* v0.4 - neural network voice engine
* v0.5 - test of google hardware features and limitations
* v0.6 - achieving continuous conversation
* v0.7 - artificial intelligence script interpreter
