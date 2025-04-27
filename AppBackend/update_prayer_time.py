import sqlite3

# Updated with whitelist validation
def update_prayer_time(prayer_name: str, new_time: str):
    allowed_columns = ["fajr", "dhuhr", "asr", "magrib", "isha"]  # Add your exact database columns here
    if prayer_name not in allowed_columns:
        raise ValueError("Invalid prayer name.")

    sql = f"""
    UPDATE prayer_times
    SET {prayer_name} = ?
    WHERE id = 1;
    """

    conn = sqlite3.connect("database.db")
    cur = conn.cursor()
    cur.execute(sql, [new_time])
    conn.commit()
    conn.close()