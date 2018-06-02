
import glob
import json
import csv
import sys
import os.path

print("\\begin{table}[]")
print("\\begin{tabular}{@{}lS[table-format=1.2(2)]S[table-format=1.2(2)]S[table-format=1.2(2)]S[table-format=5]S[table-format=2]rS@{}}")
print("\\toprule")
print("& \\multicolumn{3}{c}{logging time (\\si{s})} & \multicolumn{2}{c}{log size} &  & \\\\")
print("\\cmidrule(lr){2-4}\cmidrule(lr){5-6}")
print("{name} & {base} & {record} & {replay} & {before} & {after} & {steps} & {time (\si{s})} \\\\ \midrule")

for f in glob.glob("evaluation/*"): 
    name = os.path.relpath(f, "evaluation")

    with open(os.path.join(f, "pref.json")) as fp:
        perf = json.load(fp) 

    for p in perf["results"]: 
        mean = p["mean"]
        stddev = p["stddev"]
        timing = "{:.2f} \\pm {:.1f}".format(mean, stddev)
        if 'java' in p["command"]:
            base = timing
        if 'record' in p["command"]:
            record = timing
        if 'replay' in p["command"]:
            replay = timing

    with open(os.path.join(f, "history.log")) as fp:
        log_size = len(fp.readlines()); 
    
    with open(os.path.join(f, "dd.log")) as fp:
        reduced_size = len(fp.readlines()); 
    
    with open(os.path.join(f, "stats.csv")) as fp:
        rows = list(csv.DictReader(fp))
    
    print(" & ".join(
        [ name, base, record, replay, str(log_size), 
            str(reduced_size), rows[-1]["iterations"], "{:0.2f}".format(float(rows[-1]["time"]))
            ]) + "\\\\")

print("\\bottomrule")
print("\\end{tabular}")
print("\\end{table}")
