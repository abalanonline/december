from typing import List, Optional, Set
from pydantic import BaseModel, Field
from transformers import pipeline
generator = pipeline('text-generation', model='EleutherAI/gpt-neo-125M')

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
    finish_reason: str

class Usage(BaseModel):
    prompt_tokens: int
    completion_tokens: int
    total_tokens: int

class Output(BaseModel):
    id: str
    object: str
    created: int
    model: str
    choices: List[Choice]
    usage: Usage

def completions(input: Input) -> Output:
    prompt = input.prompt
    max_length = input.max_tokens # fixme prompt must not be a part of max_tokens and output
    temperature = input.temperature
    choices = []
    for index in range(input.n):
        output = generator(prompt, do_sample=True, max_length=max_length, temperature=temperature)
        choices.append(Choice(text=output[0]['generated_text'], index=index, finish_reason="stop"))
    return Output(id="cmpl-0", object="text_completion", created=0, model="EleutherAI/gpt-neo-125M",
        choices=choices, usage=Usage(prompt_tokens=0, completion_tokens=0, total_tokens=0))
