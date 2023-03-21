# Copyright 2023 Aleksei Balan
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM huggingface/transformers-pytorch-gpu

RUN pip install opyrator

RUN mv \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py.bak && \
head -n -6 \
  /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py.bak \
  > /usr/local/lib/python3.8/dist-packages/opyrator/api/fastapi_utils.py

RUN apt install -y nginx && \
  ln -s /mnt /root/.cache/huggingface

COPY . .

RUN rm /etc/nginx/sites-enabled/default && \
  ln -s /nginx.conf /etc/nginx/sites-enabled/default && \
  ln -s /moderations.json /var/www/html/moderations.json

#RUN python3 app.py # load model

CMD ["sh", "docker-entrypoint.sh"]
