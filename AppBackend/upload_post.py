import os
import uuid
import sqlite3
from flask import request, jsonify, current_app as app
from werkzeug.utils import secure_filename

BASE_DIR = os.path.dirname(__file__)
UPLOAD_FOLDER = os.path.join(BASE_DIR, 'uploads')
DATABASE = os.path.join(BASE_DIR, 'database.db')

os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def init_db():
    with sqlite3.connect(DATABASE) as conn:
        c = conn.cursor()
        c.execute('''
            CREATE TABLE IF NOT EXISTS posts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                text TEXT NOT NULL,
                filepath TEXT NOT NULL
            )
        ''')
        conn.commit()

def register_upload_route(app):
    @app.route('/upload', methods=['POST'])
    def upload_post():
        if 'picture' not in request.files or 'text' not in request.form:
            return jsonify({'error': 'Missing picture or text field'}), 400

        file = request.files['picture']
        text = request.form['text']

        if file.filename == '':
            return jsonify({'error': 'No selected file'}), 400

        original = secure_filename(file.filename)
        ext = os.path.splitext(original)[1]
        unique = f"{uuid.uuid4().hex}{ext}"
        path = os.path.join(UPLOAD_FOLDER, unique)
        file.save(path)

        with sqlite3.connect(DATABASE) as conn:
            c = conn.cursor()
            c.execute(
                'INSERT INTO posts (text, filepath) VALUES (?, ?)',
                (text, path)
            )
            conn.commit()

        return jsonify({
            'message': 'Post uploaded successfully',
            'filepath': path,
            'text': text
        })
