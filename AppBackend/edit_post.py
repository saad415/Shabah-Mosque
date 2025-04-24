# edit_post.py
import sqlite3
import os
from flask import request

UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def edit_post_by_id(post_id):
    try:
        text = request.form.get("text", "").strip()
        file = request.files.get("picture", None)
        remove_image = request.form.get("remove_image") == "true"

        with sqlite3.connect("database.db") as conn:
            c = conn.cursor()

            # Get current filepath to delete the file if needed
            c.execute("SELECT filepath FROM posts WHERE id = ?", (post_id,))
            current_path = c.fetchone()
            current_path = current_path[0] if current_path else None

            new_filepath = current_path

            if file:
                filename = file.filename
                new_filepath = os.path.join(UPLOAD_FOLDER, filename)
                file.save(new_filepath)

                # Delete old file if new one is uploaded
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
        return {"error": str(e)}
