import subprocess
import unittest


class TestHello(unittest.TestCase):
    def test_hello_output(self):
        result = subprocess.run(
            ["python3", "hello.py"],
            capture_output=True,
            text=True,
        )
        self.assertEqual(result.stdout, "Hello, World!\n")


if __name__ == "__main__":
    unittest.main()