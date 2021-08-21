# December - installation instructions
Some useful configuration samples.
You can also read [about the project](README.md) and [development blog](blog.md).

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

### Weather radio outline

```bash
sudo apt install mpd mpc flite
curl -L https://github.com/abalanonline/december/releases/download/v0.3/weather_test.txt \
  | flite -o /var/lib/mpd/music/weather.wav
mpc rescan --wait
mpc insert weather.wav
mpc play
ffmpeg -loop 1 -i tv_test.jpg -i http://localhost:8000/ -f flv rtmp://youtube.com/live
```

