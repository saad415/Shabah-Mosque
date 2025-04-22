from flask import Flask, jsonify, request, send_from_directory
import sqlite3
import os

from get_prayer_times import get_prayer_times
from update_prayer_time import update_prayer_time
from upload_post import register_upload_route
from get_posts import get_posts, get_posts1

app = Flask(__name__)

# === ROUTES ===

@app.route("/api/prayer_times", methods=["GET"])
def prayer_times():
    rows = get_prayer_times()
    return jsonify(rows)

@app.route("/api/prayer_times/<prayer_name>", methods=["PUT"])
def api_update_prayer(prayer_name):
    payload = request.get_json() or {}
    new_time = payload.get("time")
    if not new_time:
        return jsonify({"error": "Must provide a 'time' in the JSON body"}), 400

    try:
        update_prayer_time(prayer_name, new_time)
    except ValueError as ve:
        return jsonify({"error": str(ve)}), 400
    except Exception as e:
        return jsonify({"error": "Database error: " + str(e)}), 500

    return jsonify({"message": f"{prayer_name} updated to {new_time}"}), 200

@app.route("/api/getposts", methods=["GET"])
def api_get_posts():
    rows = get_posts1()
    return rows

# === NEW: Serve uploaded images ===

@app.route("/uploads/<filename>")
def serve_uploaded_file(filename):
    upload_folder = os.path.join(os.getcwd(), 'uploads')
    return send_from_directory(upload_folder, filename)

# === MAIN ===

if __name__ == "__main__":
    register_upload_route(app)
    app.run(host="0.0.0.0", port=5000)
