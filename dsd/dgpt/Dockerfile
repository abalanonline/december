FROM huggingface/transformers-pytorch-gpu

RUN python3 -c "from transformers import pipeline;\
  generator = pipeline('text-generation', model='EleutherAI/gpt-neo-125M')"

RUN pip install opyrator

RUN mv \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py\
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py.bak && \
head -n -6\
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py.bak\
  > /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py

COPY my_opyrator.py .

CMD ["opyrator", "launch-api", "my_opyrator:hello_world"]
