#!/usr/bin/python3

import re, sys

with open(sys.argv[1], 'r', encoding='utf8') as f:
    for line in f.readlines():
        line = line.strip()
        if re.search(r"^\(S TooLong\d+\)$", line): 
            continue
        print(line)

