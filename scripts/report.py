"""Render benchmark tables and SVG line plots with Python's standard library."""
import csv
from pathlib import Path
from html import escape
ROOT = Path(__file__).resolve().parents[1]
rows = list(csv.DictReader((ROOT/'results/tables/summary.csv').open()))
colors = ['#2563eb','#e11d48','#059669','#9333ea','#d97706','#0891b2']
names = list(dict.fromkeys(r['algorithm'] for r in rows))
for axis in ['V', 'E']:
    x_max = max(int(r[axis]) for r in rows)
    y_max = max(float(r['average_ns'])/1000 for r in rows)*1.12
    svg = ['<svg xmlns="http://www.w3.org/2000/svg" width="1040" height="560" viewBox="0 0 1040 560">',
           '<rect width="1040" height="560" fill="white"/>',
           '<g font-family="Arial,sans-serif" font-size="14" fill="#1f2937">',
           f'<text x="75" y="30" font-size="22">Execution time vs. {"vertices" if axis=="V" else "edges"}</text>',
           '<text x="75" y="53">Mean of 5 instrumented runs; fixed seed; 100 warmups</text>']
    for i in range(6):
        y=450-i*72
        svg += [f'<path d="M75 {y}H760" stroke="#e5e7eb"/>',f'<text x="65" y="{y+5}" text-anchor="end">{y_max*i/5:.0f}</text>']
    svg += ['<path d="M75 90V450H760" fill="none" stroke="#374151"/>',
            '<text x="15" y="280" transform="rotate(-90 15 280)">Time (microseconds)</text>',
            f'<text x="350" y="505">Number of {"vertices (V)" if axis=="V" else "edges (E)"}</text>']
    for value in sorted({int(r[axis]) for r in rows}):
        x=75+685*value/x_max
        svg += [f'<text x="{x}" y="478" text-anchor="middle">{value}</text>']
    for index,name in enumerate(names):
        group=[r for r in rows if r['algorithm']==name]
        points=[(75+685*int(r[axis])/x_max,450-360*(float(r['average_ns'])/1000)/y_max) for r in group]
        svg += [f'<polyline points="{" ".join(f"{x},{y}" for x,y in points)}" fill="none" stroke="{colors[index]}" stroke-width="2.5"/>']
        for x,y in points: svg += [f'<circle cx="{x}" cy="{y}" r="4" fill="{colors[index]}"/>']
        svg += [f'<rect x="790" y="{95+index*35}" width="15" height="4" fill="{colors[index]}"/>',f'<text x="815" y="{102+index*35}">{escape(name)}</text>']
    svg += ['<text x="75" y="540">V and E vary together: these plots do not isolate their individual effects.</text></g></svg>']
    (ROOT/f'results/plots/time-vs-{axis.lower()}.svg').write_text('\n'.join(svg))
time_table = '| Algorithm | V | E | Mean (µs) | Min–max (µs) |\n|---|---:|---:|---:|---:|\n'
ops_table = '| Algorithm | V | Edge exams | Comparisons | Find calls | Union calls | Heap pushes / pops |\n|---|---:|---:|---:|---:|---:|---:|\n'
for r in rows:
    time_table += f"| {r['algorithm']} | {r['V']} | {r['E']} | {float(r['average_ns'])/1000:.2f} | {int(r['min_ns'])/1000:.2f}–{int(r['max_ns'])/1000:.2f} |\n"
    ops_table += f"| {r['algorithm']} | {r['V']} | {r['edge_examinations']} | {r['comparisons']} | {r['finds']} | {r['unions']} | {r['heap_pushes']} / {r['heap_pops']} |\n"
(ROOT/'results/tables/results.md').write_text(time_table+'\n'+ops_table)
readme = (ROOT/'README.template').read_text()
(ROOT/'README.md').write_text(readme.replace('{{TIMES}}',time_table).replace('{{OPS}}',ops_table))
print('Updated README.md, results.md and two SVG plots.')
