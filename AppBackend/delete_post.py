# delete_post.py

import sqlite3

def delete_post_by_id(post_id):
    DATABASE = "database.db"
    try:
        with sqlite3.connect(DATABASE) as conn:
            c = conn.cursor()
            c.execute("UPDATE posts SET \"delete\" = 1 WHERE id = ?", (post_id,))
            conn.commit()
        return {"message": "Deleted successfully"}
    except Exception as e:
        return {"error": str(e)}
