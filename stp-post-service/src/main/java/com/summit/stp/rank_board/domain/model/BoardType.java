package com.summit.stp.rank_board.domain.model;

public enum BoardType {
    CREATOR("creator"),
    TOPIC("topic"),
    POST("post");
    private String type;
    BoardType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public static BoardType getBoardType(String type) {
        for (BoardType value : values()) {
            if (value.name().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
