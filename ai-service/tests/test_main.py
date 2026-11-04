import unittest
from app.main import retrieve


class RetrievalTests(unittest.TestCase):
    def test_retrieves_course_text_for_print_question(self):
        text, source = retrieve("Para que serve print no Python?")
        self.assertIn("print", text)
        self.assertEqual("python-intro", source)

    def test_returns_safe_answer_for_unknown_topic(self):
        text, source = retrieve("Como faço jardinagem?")
        self.assertEqual("sem_fonte", source)
        self.assertIn("Não encontrei", text)


if __name__ == "__main__":
    unittest.main()
