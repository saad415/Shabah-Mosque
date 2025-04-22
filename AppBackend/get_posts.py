
import sqlite3
from flask import Flask, jsonify


def get_posts1():
    DATABASE = "database.db"
    with sqlite3.connect(DATABASE) as conn:
        conn.row_factory = sqlite3.Row  # This enables column access by name
        c = conn.cursor()
        c.execute('SELECT * FROM posts WHERE "delete" = 0 ORDER BY id DESC')

        posts = []
        for row in c.fetchall():
            post = {
                'id': row['id'],
                'text': row['text'],
                'filepath': row['filepath'],
                'time ': row['time'],
                'delete': row['delete']
            }
            posts.append(post)

    return jsonify({'posts': posts})
