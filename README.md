
========================================
GRAMMAR HELPER 1.0.2
    written by Spence Southard
    University of North Florida, 2015
========================================


This grammar helper should be used to help identify the first and follows of any LL(1) grammar.  Please do not use the
code in this project for user in any school projects.  This is only intended to be used as a tool.

The program accepts grammars of the following form:

    S -> AB | B
    A -> a | B | @
    B -> b | CD
    C -> c | @
    D -> d




Update 1.0.2:
    -Fixed issue with follow pass 1
