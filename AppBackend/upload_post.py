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
                filepath TEXT
            )
        ''')
        conn.commit()

def register_upload_route(app):
    @app.route('/upload', methods=['POST'])
    def upload_post():
        print("Request received:", request.form.keys())
        
        if 'text' not in request.form:
            return jsonify({'error': 'Missing text field'}), 400

        text = request.form['text']
        path = None  # Default to None for posts without images

        # Handle file upload if a picture is included
        if 'picture' in request.files:
            file = request.files['picture']
            
            if file.filename != '':
                original = secure_filename(file.filename)
                ext = os.path.splitext(original)[1]
                unique = f"{uuid.uuid4().hex}{ext}"
                path = os.path.join(UPLOAD_FOLDER, unique)
                file.save(path)

        try:
            with sqlite3.connect(DATABASE) as conn:
                c = conn.cursor()
                c.execute(
                    'INSERT INTO posts (text, filepath) VALUES (?, ?)',
                    (text, path)
                )
                conn.commit()
        except Exception as e:
            import traceback
            print("Database error:", str(e))
            print(traceback.format_exc())
            return jsonify({'error': f'Database error: {str(e)}'}), 500

        return jsonify({
            'message': 'Post uploaded successfully',
            'filepath': path,
            'text': text
        })