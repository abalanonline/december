**repository archival note:**

The goal of December was to create a chatbot able to keep a natural voice conversation and help the user
with good advice when possible. It was finished in December (coincidence) 2022. ([video link](https://youtu.be/7iVm5WyDVdQ))

The project didn't age well though. It was fun to train bleeding edge speech synthesis and conversation models
and put them together, creating a device that had never existed before. But developers around the world were
competing on this goal too. In the same December 2022, ChatGPT was released, making most of the achievements
of this project obsolete. And today, it is possible to assemble something similar without even writing a line of code.

However, it was not only a chatbot. December also spawned a side project - Weather Radio - a generated live
radio stream with weather reports, music, AI-generated stories, and talk shows. ([video link](https://youtu.be/fJgrWtZdVaI))

The project remains under the Apache license. Please feel free to take anything you find useful.

---

# December - the business coach chatbot
This is a page about the goals and plans of the project.
You can also read [development blog](blog.md) and [installation instructions](install.md).

### Purpose
* Providing insights on setting the business goals and increasing profit
* Assisting in personal development

### Application design v1
* Artificial intelligence engine configured by AIML
* Natural language processing with speech recognition
* Pluggable text-to-speech engines
* Customizable voices
* Hardware devices connected by Alexa Skills API

### Roadmap
* v0.1 - echo skill with aws lambda
* v0.2 - external speech synthesis
* v0.3 - weather radio (AWOS/NOAA/CMB style)
* v0.4 - neural network voice engine
* v0.5 - test of google hardware features and limitations
* v0.6 - achieving continuous conversation
* v0.7 - artificial intelligence script interpreter

First stage of the development is done in the end of 2020 and moved to the [mono](mono) module folder.
