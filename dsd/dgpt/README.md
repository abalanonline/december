# dgpt
GPT-3 compatible microservice for [December](https://github.com/abalanonline/december)

```console
docker run -d --rm --name dgpt -p 2741:80 abalanonline/dgpt
docker run -d --rm --name dgpt -v $HOME/.cache/huggingface:/mnt -p 2741:80 abalanonline/dgpt:gptj
```
