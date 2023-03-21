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

from typing import List, Optional, Set
from pydantic import BaseModel, Field
import time
from transformers import pipeline
model = open("modelname").read()
generator = pipeline('text-generation', model=model)

class Input(BaseModel):
    model: str
    prompt: str
    max_tokens: int = Field(16)
    temperature: float = Field(1.0)
    n: int = Field(1)

class Choice(BaseModel):
    text: str
    index: int
    logprobs: Optional[str]
    finish_reason: str = Field("length") # length, stop

class Usage(BaseModel):
    prompt_tokens: int = Field(0)
    completion_tokens: int = Field(0)
    total_tokens: int = Field(0)

class Output(BaseModel):
    id: str
    object: str = Field("text_completion")
    created: int
    model: str
    choices: List[Choice]
    usage: Usage

def completions(input: Input) -> Output:
    prompt = input.prompt
    max_length = len(prompt) // 4 + input.max_tokens # fixme estimation of tokens in prompt len(prompt) // 4
    temperature = input.temperature
    choices = []
    for index in range(input.n):
        output = generator(prompt, do_sample=True, max_length=max_length, temperature=temperature)
        choices.append(Choice(text=output[0]['generated_text'][len(prompt):], index=index))
    return Output(id="cmpl-0", created=int(time.time()), model=model, choices=choices, usage=Usage())
