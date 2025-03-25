import time
tell = 0
with open("new.log", "a", encoding="utf-8") as file:
    tell = file.tell()
    file.write("estgasdg\n")
    file.write("ysasgdsg\n")

i = 1
while i < 100000:
    with open("new.log", "r+", encoding="utf-8") as file:
        file.seek(tell)
        file.write(f"{i}")
        file.flush()
    time.sleep(1)
    i += 1

