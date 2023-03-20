from pydantic import BaseModel
from transformers import pipeline
generator = pipeline('text-generation', model='EleutherAI/gpt-neo-125M')

class Input(BaseModel):
    prompt: str

class Output(BaseModel):
    text: str

def completions(input: Input) -> Output:
    prompt = input.prompt
    max_length = 250
    temperature = 0.8
    output = generator(prompt, do_sample=True, max_length=max_length, temperature=temperature)
    print(output)
    return Output(text=output[0]['generated_text'])

