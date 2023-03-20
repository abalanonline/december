FROM huggingface/transformers-pytorch-gpu

RUN python3 -c "from transformers import pipeline; \
  generator = pipeline('text-generation', model='EleutherAI/gpt-neo-125M')"

RUN pip install opyrator

RUN mv \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py.bak && \
head -n -6 \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py.bak \
  > /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py

RUN apt install -y nginx

COPY . .

RUN rm /etc/nginx/sites-enabled/default && \
  ln -s /nginx.conf /etc/nginx/sites-enabled/default && \
  ln -s /moderations.json /var/www/html/moderations.json

CMD ["sh", "docker-entrypoint.sh"]
