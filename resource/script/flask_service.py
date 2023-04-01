import argparse

from flask import Flask
from flask_socketio import SocketIO
from transformers import AutoModelForCausalLM, BertTokenizerFast, GPT2LMHeadModel, BatchEncoding

parser = argparse.ArgumentParser()

parser.add_argument('model_path', type=str)
parser.add_argument('port', type=int)

args = parser.parse_args()

model: GPT2LMHeadModel = AutoModelForCausalLM.from_pretrained(args.model_path)
tokenizer = BertTokenizerFast.from_pretrained(args.model_path, padding_side='left')
model.eval()

config = model.generation_config

app = Flask(__name__)
socketio = SocketIO(app
                    # , cors_allowed_origins='*', async_mode='eventlet'
                    # , engineio_logger=True, logger=True,
                    # engineio_logger_level='DEBUG', ping_timeout=30, ping_interval=10, async_handlers=True
                    )


@socketio.on('connect')
def connect():
    print('Java connect!')


@socketio.on('generate')
def reply_text(data):
    user_inputs: BatchEncoding = tokenizer(tokenizer.sep_token.join(data['text']), return_tensors='pt', truncation=True,
                                           max_length=1024 - config.max_new_tokens)

    # generated a response while limiting the total chat history to 1000 tokens,
    chat_history_ids = model.generate(generation_config=config, **user_inputs)

    reply = ''.join(tokenizer.decode(chat_history_ids[:, user_inputs['input_ids'].shape[-1]:][0],
                                     skip_special_tokens=True).split())
    print(reply)
    socketio.emit('reply', reply)


if __name__ == '__main__':
    socketio.run(app, 'localhost', port=args.port)
