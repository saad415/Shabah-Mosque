from flask import Flask, jsonify, request, send_from_directory
import sqlite3
import os
import logging
from werkzeug.utils import secure_filename

from get_prayer_times import get_prayer_times
from update_prayer_time import update_prayer_time
from upload_post import register_upload_route
from get_posts import get_posts1
from delete_post import delete_post_by_id
from edit_post import edit_post_by_id

# Setup logging
logging.basicConfig(level=logging.ERROR)

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
        logging.error(f"Database error: {e}")
        return jsonify({"error": "Internal server error."}), 500

    return jsonify({"message": f"{prayer_name} updated to {new_time}"}), 200


@app.route("/api/getposts", methods=["GET"])
def api_get_posts():
    rows = get_posts1()
    return rows


@app.route("/api/delete_post/<int:post_id>", methods=["DELETE"])
def api_delete_post(post_id):
    result = delete_post_by_id(post_id)
    status_code = 200 if "message" in result else 500
    return jsonify(result), status_code


@app.route("/api/edit_post/<int:post_id>", methods=["PUT"])
def api_edit_post(post_id):
    result = edit_post_by_id(post_id)
    return jsonify(result), (200 if "message" in result else 500)


# === Serve uploaded images (safe version) ===
@app.route("/uploads/<filename>")
def serve_uploaded_file(filename):
    upload_folder = os.path.join(os.getcwd(), 'uploads')
    safe_filename = secure_filename(filename)
    return send_from_directory(upload_folder, safe_filename)


# === MAIN ===
if __name__ == "__main__":
    register_upload_route(app)
    app.run(host="0.0.0.0", port=5000, debug=True)  # debug=True for easier testing during development


