import sqlite3
import os
import logging
from flask import request
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def edit_post_by_id(post_id):
    try:
        text = request.form.get("text", "").strip()
        file = request.files.get("picture", None)
        remove_image = request.form.get("remove_image") == "true"

        with sqlite3.connect("database.db") as conn:
            c = conn.cursor()
            c.execute("SELECT filepath FROM posts WHERE id = ?", (post_id,))
            current_path = c.fetchone()
            current_path = current_path[0] if current_path else None

            new_filepath = current_path

            if file:
                filename = secure_filename(file.filename)
                new_filepath = os.path.join(UPLOAD_FOLDER, filename)
                file.save(new_filepath)

                if current_path and os.path.exists(current_path):
                    os.remove(current_path)

            elif remove_image and current_path and os.path.exists(current_path):
                os.remove(current_path)
                new_filepath = None

            if file or remove_image:
                c.execute("UPDATE posts SET text = ?, filepath = ?, time = CURRENT_TIMESTAMP WHERE id = ?", (text, new_filepath, post_id))
            else:
                c.execute("UPDATE posts SET text = ?, time = CURRENT_TIMESTAMP WHERE id = ?", (text, post_id))

            conn.commit()
        return {"message": "Post updated"}
    except Exception as e:
        logging.error(f"Edit post error: {e}")
        return {"error": "Internal server error"}