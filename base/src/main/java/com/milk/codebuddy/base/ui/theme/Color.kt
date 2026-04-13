package com.milk.codebuddy.base.ui.theme

import androidx.compose.ui.graphics.Color

// ── 原始色板（仅在组装 AppColors 时引用）────────────────────────────────────
val Color_000000 = Color(0xFF000000)
val Color_333333 = Color(0xFF333333)
val Color_666666 = Color(0xFF666666)
val Color_999999 = Color(0xFF999999)
val Color_CCCCCC = Color(0xFFCCCCCC)
val Color_E0E0E0 = Color(0xFFE0E0E0)
val Color_E3E6E8 = Color(0xFFE3E6E8)
val Color_FFFFFF = Color(0xFFFFFFFF)
val Color_F5F5F5 = Color(0xFFF5F5F5)
val Color_1F72E8 = Color(0xFF1F72E8)
val Color_EA4335 = Color(0xFFEA4335)
val Color_6200EE = Color(0xFF6200EE)
val Color_03DAC6 = Color(0xFF03DAC6)
val Color_121212 = Color(0xFF121212)
val Color_1E1E1E = Color(0xFF1E1E1E)
val Color_2D2D2D = Color(0xFF2D2D2D)
val Color_BB86FC = Color(0xFFBB86FC)
val Color_018786 = Color(0xFF018786)

// ── 语义层 AppColors（业务代码唯一入口，通过 LocalAppColors.current 访问）──────
data class AppColors(
    /** 主要正文颜色 */
    val primaryTextColor: Color,
    /** 次要正文颜色（副标题、说明文字） */
    val secondaryTextColor: Color,
    /** 辅助文字颜色（placeholder、时间戳等） */
    val auxiliaryTextColor: Color,
    /** 占位符/禁用文字颜色 */
    val hintTextColor: Color,
    /** 主背景色 */
    val primaryBackgroundColor: Color,
    /** 次要背景色（卡片、列表项） */
    val secondaryBackgroundColor: Color,
    /** 辅助背景色（标签、分隔线） */
    val auxiliaryBackgroundColor: Color,
    /** 品牌强调色（按钮、链接） */
    val accentColor: Color,
    /** 分割线 / 边框颜色 */
    val dividerColor: Color,
    /** Google 红（第三方登录图标等） */
    val googleRed: Color
)
