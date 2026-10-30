"""Demo backend server, Python3 stdlib only (http.server)."""
import csv
import hashlib
import io
import json
import os
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
EVENTS_FILE = os.path.join(DATA_DIR, "events.jsonl")

SUPPORTED_ALGORITHMS = ("md5", "sha1", "sha256")
VALID_DIMENSIONS = ("type", "level", "dept")
VALID_EXPORT_TARGETS = ("hello", "hash", "sort")

# 进程内事件内存
EVENTS = []


# ---------- 纯函数逻辑 ----------

def make_hash(text, algorithm):
    h = hashlib.new(algorithm)
    h.update(text.encode("utf-8"))
    return h.hexdigest()


def bubble_sort(array):
    """冒泡排序，返回 (排序结果, 比较次数)。"""
    arr = list(array)
    steps = 0
    n = len(arr)
    for i in range(n):
        for j in range(0, n - i - 1):
            steps += 1
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
    return arr, steps


def get_caller(headers):
    """从请求头提取调用人信息，均有默认值。"""
    def _get(name, default):
        v = headers.get(name)
        return v if v else default

    return {
        "name": _get("X-User-Name", "anonymous"),
        "type": _get("X-User-Type", "unknown"),
        "level": _get("X-User-Level", "unknown"),
        "dept": _get("X-User-Dept", "unknown"),
    }


def record_event(api, caller, input_summary, result_summary,
                 events=None, events_file=EVENTS_FILE):
    """记录一条埋点事件：内存 + 追加写 JSONL。"""
    if events is None:
        events = EVENTS
    event = {
        "time": datetime.now().isoformat(timespec="seconds"),
        "api": api,
        "caller": caller,
        "input": input_summary,
        "result": result_summary,
    }
    events.append(event)
    os.makedirs(os.path.dirname(events_file), exist_ok=True)
    with open(events_file, "a", encoding="utf-8") as f:
        f.write(json.dumps(event, ensure_ascii=False) + "\n")
    return event


def load_events(events_file=EVENTS_FILE, events=None):
    """启动时回放加载 events.jsonl。"""
    if events is None:
        events = EVENTS
    if not os.path.exists(events_file):
        return
    with open(events_file, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                events.append(json.loads(line))


def stats_summary(dimension, events=None):
    if events is None:
        events = EVENTS
    counter = {}
    series_counter = {}
    for e in events:
        key = e.get("caller", {}).get(dimension, "unknown")
        counter[key] = counter.get(key, 0) + 1
        minute = e.get("time", "")[:16].replace("T", " ")
        series_counter[minute] = series_counter.get(minute, 0) + 1
    items = [{"key": k, "count": v} for k, v in
             sorted(counter.items(), key=lambda kv: (-kv[1], kv[0]))]
    series = [{"time": t, "count": c} for t, c in sorted(series_counter.items())]
    return {"dimension": dimension, "total": len(events),
            "items": items, "series": series}


def export_csv(target, events=None):
    if events is None:
        events = EVENTS
    buf = io.StringIO()
    writer = csv.writer(buf)
    writer.writerow(["time", "user", "input", "result"])
    for e in events:
        if e.get("api") == target:
            writer.writerow([e.get("time", ""),
                             e.get("caller", {}).get("name", "anonymous"),
                             e.get("input", ""), e.get("result", "")])
    return buf.getvalue()


# ---------- HTTP Handler ----------

class DemoHandler(BaseHTTPRequestHandler):
    server_version = "DemoServer/1.0"

    def _send_json(self, payload, status=200):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers",
                         "Content-Type, X-User-Name, X-User-Type, "
                         "X-User-Level, X-User-Dept")
        self.end_headers()
        self.wfile.write(body)

    def _read_body(self):
        length = int(self.headers.get("Content-Length") or 0)
        if length <= 0:
            return {}
        try:
            return json.loads(self.rfile.read(length).decode("utf-8"))
        except (ValueError, UnicodeDecodeError):
            return {}

    def do_OPTIONS(self):
        self._send_json({"code": 0, "message": "ok"})

    def do_POST(self):
        path = urlparse(self.path).path
        if path == "/api/demo/hello":
            self._handle_hello()
        elif path == "/api/demo/hash":
            self._handle_hash()
        elif path == "/api/demo/sort":
            self._handle_sort()
        else:
            self._send_json({"code": 404, "message": "not found"}, status=404)

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/api/stats/summary":
            self._handle_stats(parse_qs(parsed.query))
        elif parsed.path == "/api/demo/export":
            self._handle_export(parse_qs(parsed.query))
        else:
            self._send_json({"code": 404, "message": "not found"}, status=404)

    # --- POST handlers ---

    def _handle_hello(self):
        result = "Hello, World!"
        record_event("hello", get_caller(self.headers), "", result)
        self._send_json({"code": 0, "message": "ok",
                         "data": {"result": result}})

    def _handle_hash(self):
        body = self._read_body()
        algorithm = body.get("algorithm", "")
        if algorithm not in SUPPORTED_ALGORITHMS:
            self._send_json({"code": 400, "message": "unsupported algorithm"})
            return
        text = str(body.get("input", ""))
        result = make_hash(text, algorithm)
        record_event("hash", get_caller(self.headers),
                     "input=%s,algorithm=%s" % (text, algorithm), result)
        self._send_json({"code": 0, "message": "ok",
                         "data": {"input": text, "algorithm": algorithm,
                                  "result": result}})

    def _handle_sort(self):
        body = self._read_body()
        array = body.get("array")
        if (not isinstance(array, list) or
                any(isinstance(x, bool) or not isinstance(x, (int, float))
                    for x in array)):
            self._send_json({"code": 400,
                             "message": "array must be a list of numbers"})
            return
        result, steps = bubble_sort(array)
        record_event("sort", get_caller(self.headers),
                     "array=%s" % json.dumps(array), json.dumps(result))
        self._send_json({"code": 0, "message": "ok",
                         "data": {"input": array, "result": result,
                                  "steps": steps}})

    # --- GET handlers ---

    def _handle_stats(self, query):
        dimension = (query.get("dimension") or [""])[0]
        if dimension not in VALID_DIMENSIONS:
            self._send_json({"code": 400, "message": "unsupported dimension"})
            return
        self._send_json({"code": 0, "data": stats_summary(dimension)})

    def _handle_export(self, query):
        target = (query.get("target") or [""])[0]
        if target not in VALID_EXPORT_TARGETS:
            self._send_json({"code": 400, "message": "unsupported target"})
            return
        body = export_csv(target).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/csv; charset=utf-8")
        self.send_header("Content-Disposition",
                         "attachment; filename=%s-result.csv" % target)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):  # 静默访问日志
        pass


def main(port=8080):
    load_events()
    server = ThreadingHTTPServer(("0.0.0.0", port), DemoHandler)
    print("listening on http://0.0.0.0:%d" % port)
    server.serve_forever()


if __name__ == "__main__":
    main()
