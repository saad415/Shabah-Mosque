
import sqlite3
from flask import Flask, jsonify
def get_posts():
    conn = sqlite3.connect("database.db")
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM posts ORDER BY id DESC")
    rows = cursor.fetchall()
    conn.close()
    return [dict(row) for row in rows]


def get_posts1():
    DATABASE = "database.db"
    with sqlite3.connect(DATABASE) as conn:
        conn.row_factory = sqlite3.Row  # This enables column access by name
        c = conn.cursor()
        c.execute('SELECT id, text, filepath FROM posts ORDER BY id DESC')

        posts = []
        for row in c.fetchall():
            post = {
                'id': row['id'],
                'text': row['text'],
                'filepath': row['filepath']
            }
            posts.append(post)

    return jsonify({'posts': posts})
