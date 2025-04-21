import sqlite3

def update_prayer_time(prayer_name: str, new_time: str):
 
 

    # prepare our UPDATE statement
    sql = f"""
    UPDATE prayer_times
    SET {prayer_name} = ?
    WHERE id = 1;

    """
    # use UTC now in ISO format, or switch to local if you prefer
    conn = sqlite3.connect("MyAppDB.db")  # Make sure this file is in same folder
    cur = conn.cursor()
    cur.execute(sql, [new_time])
    conn.commit()
    conn.close()