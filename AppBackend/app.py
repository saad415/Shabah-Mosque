from flask import Flask, jsonify
import sqlite3

app = Flask(__name__)

def get_prayer_times():
    conn = sqlite3.connect("MyAppDB.db")  # Make sure this file is in same folder
    conn.row_factory = sqlite3.Row 
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM prayer_times")
    rows = cursor.fetchall()
    conn.close()
    return [dict(row) for row in rows]
    #return rows

@app.route("/api/prayer_times", methods=["GET"])
def prayer_times():
    rows = get_prayer_times()
    return jsonify(rows)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
