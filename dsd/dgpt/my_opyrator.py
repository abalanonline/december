from pydantic import BaseModel
from transformers import pipeline
generator = pipeline('text-generation', model='EleutherAI/gpt-neo-125M')

class Input(BaseModel):
    message: str

class Output(BaseModel):
    message: str

def hello_world(input: Input) -> Output:
    prompt = input.message
    max_length = 250
    temperature = 0.8
    output = generator(prompt, do_sample=True, max_length=max_length, temperature=temperature)
    print(output[0]['generated_text'])
    return Output(message=output[0]['generated_text'])

