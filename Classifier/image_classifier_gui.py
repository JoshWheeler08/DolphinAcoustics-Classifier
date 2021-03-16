import flask

from flask import send_from_directory, jsonify

app = flask.Flask(__name__)

@app.route('/', methods=['GET'])
def home():
    return send_from_directory('.', 'index.html')

@app.route('/api/data', methods=['GET'])
def get_next_result():
    

app.run()