"""对话模块 —— 自然语言文本解析与任务创建。

提供：
- IntentParser:  基于关键词的意图识别
- EntityExtractor: 基于正则的实体提取（名称 + 描述）
- ConversationService: 对话服务门面，串联解析→创建全流程
"""

from __future__ import annotations

import re
from pathlib import Path

from .main import add_task, validate_name

# ── 常量定义 ──────────────────────────────────────────────────

CREATE_INTENT_KEYWORDS: list[str] = [
    "添加", "创建", "新增", "记录", "帮我记",
    "代办", "待办", "todo", "add", "create",
    "记录一下", "记一下",
]

NON_INTENT_INDICATORS: list[str] = [
    "查看", "查询", "列出", "删除", "完成",
    "list", "show", "delete", "done", "complete",
]

# 正则：匹配 "关键词 [量词] 名称 [,， 描述：xxx]" 结构
# 捕获组 1 = 名称，捕获组 2 = 描述（可选）
_NAME_PATTERN: str = (
    r"(?:添加|创建|新增|记录|帮我记|代办|待办|todo|add|create|记一下|记录一下)"
    r"\s*(?:一个|一下|个\s*)?"
    r"(.+?)"
    r"(?:\s*[，,]\s*(?:描述|备注|说明|内容)\s*[：:]\s*(.+))?$"
)

# ── IntentParser ──────────────────────────────────────────────

class IntentParser:
    """基于关键词字典的意图识别器。

    判断输入文本是否表达了"创建任务"的意图。
    策略：文本中包含任一创建关键词，且不包含任一明确排除词。
    """

    def is_create_intent(self, text: str) -> bool:
        """判断文本是否为创建任务意图。

        策略：在文本中扫描所有关键词（创建词 + 排除词），
        取出现位置最早的那个关键词：
        - 若为创建词 → 接受为创建意图
        - 若为排除词 → 拒绝
        - 无匹配 → 拒绝

        Args:
            text: 用户输入的自然语言文本。

        Returns:
            True 表示检测到创建意图。
        """
        if not text or not text.strip():
            return False

        # 找到位置最早的关键词（创建词或排除词）
        all_keywords = CREATE_INTENT_KEYWORDS + NON_INTENT_INDICATORS
        first_pos = len(text)
        first_kw: str | None = None

        for kw in all_keywords:
            pos = text.find(kw)
            if pos != -1 and pos < first_pos:
                first_pos = pos
                first_kw = kw

        if first_kw is None:
            return False

        # 最早匹配的是排除词 → 拒绝
        if first_kw in NON_INTENT_INDICATORS:
            return False

        return True


# ── EntityExtractor ───────────────────────────────────────────

class EntityExtractor:
    """基于正则的实体提取器。

    从已确认包含创建意图的文本中提取事项名称和可选描述。
    """

    _pattern: re.Pattern[str] = re.compile(_NAME_PATTERN)

    def extract(self, text: str) -> dict:
        """从文本中提取名称和描述。

        Args:
            text: 已确认包含创建意图的自然语言文本。

        Returns:
            {"name": str, "description": str}，description 可能为空字符串。

        Raises:
            ValueError: 无法从文本中提取有效名称。
        """
        match = self._pattern.match(text.strip())
        if not match:
            raise ValueError("无法从事项描述中提取名称")

        name = match.group(1).strip()
        description = (match.group(2) or "").strip()

        if not name:
            raise ValueError("无法从事项描述中提取名称")

        return {"name": name, "description": description}


# ── ConversationService ───────────────────────────────────────

class ConversationService:
    """对话服务 —— 自然语言创建待办事项的门面。

    组合 IntentParser 和 EntityExtractor，对外暴露单一入口
    create_from_text()，串联意图识别→实体提取→任务创建全流程。
    """

    def __init__(self) -> None:
        self._intent_parser = IntentParser()
        self._entity_extractor = EntityExtractor()

    def create_from_text(
        self,
        text: str,
        data_file: Path | None = None,
    ) -> dict:
        """从自然语言文本创建待办事项。

        Args:
            text: 用户输入的自然语言文本。
            data_file: 数据文件路径，默认使用 DEFAULT_DATA_FILE。

        Returns:
            创建成功的任务 dict（name, description, created_at）。

        Raises:
            ValueError: 文本为空 / 非创建意图 / 无法提取名称 / 名称非法。
            OSError: 文件写入失败。
        """
        if not text or not text.strip():
            raise ValueError("输入文本不能为空")

        if not self._intent_parser.is_create_intent(text):
            raise ValueError("无法识别为创建任务意图")

        extracted = self._entity_extractor.extract(text)
        return add_task(
            name=extracted["name"],
            description=extracted["description"],
            data_file=data_file,
        )