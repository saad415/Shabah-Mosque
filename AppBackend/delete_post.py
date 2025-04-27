import sqlite3
import logging

def delete_post_by_id(post_id):
    DATABASE = "database.db"
    try:
        with sqlite3.connect(DATABASE) as conn:
            c = conn.cursor()
            c.execute("UPDATE posts SET \"delete\" = 1 WHERE id = ?", (post_id,))
            conn.commit()
        return {"message": "Deleted successfully"}
    except Exception as e:
        logging.error(f"Delete post error: {e}")
        return {"error": "Internal server error"}