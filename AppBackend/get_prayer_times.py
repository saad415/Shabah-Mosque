
import sqlite3
def get_prayer_times():
    conn = sqlite3.connect("MyAppDB.db")  # Make sure this file is in same folder
    conn.row_factory = sqlite3.Row 
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM prayer_times")
    rows = cursor.fetchall()
    conn.close()
    return [dict(row) for row in rows]
    #return rows
