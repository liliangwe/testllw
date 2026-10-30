"""Unit tests for server/app.py (unittest, stdlib only)."""
import io
import json
import os
import tempfile
import unittest

import app


class TestPureFunctions(unittest.TestCase):
    def test_make_hash(self):
        self.assertEqual(app.make_hash("abc", "md5"),
                         "900150983cd24fb0d6963f7d28e17f72")
        self.assertEqual(app.make_hash("abc", "sha256"),
                         "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
        self.assertEqual(app.make_hash("abc", "sha1"),
                         "a9993e364706816aba3e25717850c26c9cd0d89d")

    def test_bubble_sort(self):
        result, steps = app.bubble_sort([3, 1, 2])
        self.assertEqual(result, [1, 2, 3])
        self.assertEqual(steps, 3)  # 3 元素冒泡第一轮 2 次 + 第二轮 1 次

    def test_bubble_sort_empty(self):
        self.assertEqual(app.bubble_sort([]), ([], 0))

    def test_get_caller_defaults(self):
        caller = app.get_caller({})
        self.assertEqual(caller, {"name": "anonymous", "type": "unknown",
                                  "level": "unknown", "dept": "unknown"})

    def test_get_caller_headers(self):
        caller = app.get_caller({"X-User-Name": "alice", "X-User-Type": "admin",
                                 "X-User-Level": "vip", "X-User-Dept": "dev"})
        self.assertEqual(caller["name"], "alice")
        self.assertEqual(caller["dept"], "dev")


class TestEvents(unittest.TestCase):
    def setUp(self):
        self.tmpdir = tempfile.TemporaryDirectory()
        self.events_file = os.path.join(self.tmpdir.name, "events.jsonl")
        self.events = []

    def tearDown(self):
        self.tmpdir.cleanup()

    def test_record_and_load(self):
        caller = {"name": "bob", "type": "t", "level": "l", "dept": "d"}
        app.record_event("hello", caller, "", "Hello, World!",
                         events=self.events, events_file=self.events_file)
        self.assertEqual(len(self.events), 1)
        self.assertTrue(os.path.exists(self.events_file))
        loaded = []
        app.load_events(events_file=self.events_file, events=loaded)
        self.assertEqual(len(loaded), 1)
        self.assertEqual(loaded[0]["api"], "hello")
        self.assertEqual(loaded[0]["caller"]["name"], "bob")

    def test_stats_summary(self):
        caller = {"name": "a", "type": "admin", "level": "vip", "dept": "dev"}
        for _ in range(2):
            app.record_event("hello", caller, "", "r",
                             events=self.events, events_file=self.events_file)
        app.record_event("sort", {"name": "b", "type": "user",
                                  "level": "normal", "dept": "ops"},
                         "", "", events=self.events,
                         events_file=self.events_file)
        summary = app.stats_summary("type", events=self.events)
        self.assertEqual(summary["dimension"], "type")
        self.assertEqual(summary["total"], 3)
        self.assertEqual(summary["items"][0], {"key": "admin", "count": 2})
        self.assertTrue(all("time" in s and "count" in s
                            for s in summary["series"]))

    def test_export_csv(self):
        caller = {"name": "c", "type": "t", "level": "l", "dept": "d"}
        app.record_event("hello", caller, "", "Hello, World!",
                         events=self.events, events_file=self.events_file)
        app.record_event("sort", caller, "[3,1,2]", "[1,2,3]",
                         events=self.events, events_file=self.events_file)
        csv_text = app.export_csv("hello", events=self.events)
        lines = [l for l in csv_text.strip().splitlines()]
        self.assertEqual(lines[0], "time,user,input,result")
        self.assertEqual(len(lines), 2)
        self.assertIn("Hello, World!", lines[1])


class TestHandler(unittest.TestCase):
    """通过真实 HTTP 请求测 handler 逻辑（随机端口）。"""

    @classmethod
    def setUpClass(cls):
        import threading
        from http.server import ThreadingHTTPServer
        cls.tmpdir = tempfile.TemporaryDirectory()
        app.EVENTS.clear()
        cls.orig_file = app.EVENTS_FILE
        app.EVENTS_FILE = os.path.join(cls.tmpdir.name, "events.jsonl")
        # record_event 默认参数绑定于定义时的 EVENTS_FILE，需显式绕过：
        cls.orig_record = app.record_event

        def _record(api, caller, inp, res):
            return cls.orig_record(api, caller, inp, res,
                                   events=app.EVENTS,
                                   events_file=app.EVENTS_FILE)
        app.record_event = _record
        cls.server = ThreadingHTTPServer(("127.0.0.1", 0), app.DemoHandler)
        cls.port = cls.server.server_address[1]
        cls.thread = threading.Thread(target=cls.server.serve_forever,
                                      daemon=True)
        cls.thread.start()

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        app.record_event = cls.orig_record
        app.EVENTS_FILE = cls.orig_file
        cls.tmpdir.cleanup()

    def _post(self, path, body=None, headers=None):
        import http.client
        conn = http.client.HTTPConnection("127.0.0.1", self.port)
        conn.request("POST", path, body=json.dumps(body or {}),
                     headers={"Content-Type": "application/json",
                              **(headers or {})})
        resp = conn.getresponse()
        payload = json.loads(resp.read().decode("utf-8"))
        conn.close()
        return resp.status, payload

    def test_hello(self):
        status, payload = self._post("/api/demo/hello",
                                     headers={"X-User-Name": "tester"})
        self.assertEqual(status, 200)
        self.assertEqual(payload, {"code": 0, "message": "ok",
                                   "data": {"result": "Hello, World!"}})

    def test_hash_ok(self):
        status, payload = self._post("/api/demo/hash",
                                     {"input": "abc", "algorithm": "md5"})
        self.assertEqual(status, 200)
        self.assertEqual(payload["code"], 0)
        self.assertEqual(payload["data"]["result"],
                         "900150983cd24fb0d6963f7d28e17f72")
        self.assertEqual(payload["data"]["input"], "abc")

    def test_hash_bad_algorithm(self):
        status, payload = self._post("/api/demo/hash",
                                     {"input": "abc", "algorithm": "sha512"})
        self.assertEqual(status, 200)
        self.assertEqual(payload["code"], 400)
        self.assertEqual(payload["message"], "unsupported algorithm")

    def test_sort_ok(self):
        status, payload = self._post("/api/demo/sort", {"array": [3, 1, 2]})
        self.assertEqual(payload["code"], 0)
        self.assertEqual(payload["data"]["result"], [1, 2, 3])
        self.assertEqual(payload["data"]["steps"], 3)

    def test_sort_bad_elements(self):
        status, payload = self._post("/api/demo/sort", {"array": [1, "a"]})
        self.assertEqual(payload["code"], 400)


if __name__ == "__main__":
    unittest.main()
