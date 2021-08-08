package com.example.airhockey.models;

public interface ProtocolConstants {
    char END_MSG = '@';
    char SEP = '#';
    char POSITION_MSG = 'P';
    char COLLISION_MSG = 'C';
    char COLLISION_ACK = 'R';
    char GOAL_SCORED = 'G';
    char GOAL_ACK = 'F';
    String DOUBLE_PATTERN = "[-+]?[0-9]*\\.[0-9]+";
}
