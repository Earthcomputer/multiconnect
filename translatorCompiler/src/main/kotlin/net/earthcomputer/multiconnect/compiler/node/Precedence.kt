package net.earthcomputer.multiconnect.compiler.node

enum class Precedence {
    PARENTHESES,
    POSTFIX,
    UNARY,
    CAST,
    MULTIPLICATIVE,
    ADDITIVE,
    SHIFT,
    RELATIONAL,
    EQUALITY,
    BITWISE,
    LOGICAL,
    TERNARY,
    ASSIGNMENT,
    COMMA
}
