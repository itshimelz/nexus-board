package com.himelz.nexusboard.model.board;

import com.himelz.nexusboard.model.pieces.ChessPiece;
import com.himelz.nexusboard.model.pieces.Pawn;


public class Move {
    private final Position from;
    private final Position to;
    private final ChessPiece movingPiece;
    private final ChessPiece capturedPiece;
    private final MoveType moveType;
    private final ChessPiece promotionPiece;

    // Normal move constructor
    public Move(Position from, Position to, ChessPiece movingPiece) {
        this(from, to, movingPiece, null, MoveType.NORMAL, null);
    }

    // Capture move constructor
    public Move(Position from, Position to, ChessPiece movingPiece, ChessPiece capturedPiece) {
        this(from, to, movingPiece, capturedPiece, MoveType.CAPTURE, null);
    }

    // Full constructor
    public Move(Position from, Position to, ChessPiece movingPiece, ChessPiece capturedPiece,
                MoveType moveType, ChessPiece promotionPiece) {
        this.from = from;
        this.to = to;
        this.movingPiece = movingPiece;
        this.capturedPiece = capturedPiece;
        this.moveType = moveType;
        this.promotionPiece = promotionPiece;
    }

    // Getters
    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public ChessPiece getMovingPiece() {
        return movingPiece;
    }

    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public ChessPiece getPromotionPiece() {
        return promotionPiece;
    }

    public boolean isCapture() {
        return moveType == MoveType.CAPTURE || moveType == MoveType.EN_PASSANT;
    }

    public boolean isCastle() {
        return moveType == MoveType.CASTLE_KINGSIDE || moveType == MoveType.CASTLE_QUEENSIDE;
    }

    public boolean isPromotion() {
        return moveType == MoveType.PROMOTION;
    }


     // Convert move to algebraic notation

    public String toAlgebraic() {
        StringBuilder notation = new StringBuilder();

        // Add piece symbol (except for pawns)
        if (!(movingPiece instanceof Pawn)) {
            notation.append(movingPiece.getSymbol());
        }

        // Add capture notation
        if (isCapture()) {
            if (movingPiece instanceof Pawn) {
                notation.append((char) ('a' + from.getCol()));
            }
            notation.append("x");
        }

        // Add destination square
        notation.append(to.toAlgebraic());

        // Add special move notations
        if (moveType == MoveType.CASTLE_KINGSIDE) {
            return "O-O";
        } else if (moveType == MoveType.CASTLE_QUEENSIDE) {
            return "O-O-O";
        } else if (isPromotion()) {
            notation.append("=").append(promotionPiece.getSymbol());
        }

        return notation.toString();
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from.equals(move.from) && to.equals(move.to) &&
                moveType == move.moveType;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(from, to, moveType);
    }

    public enum MoveType {
        NORMAL,
        CAPTURE,
        CASTLE_KINGSIDE,
        CASTLE_QUEENSIDE,
        EN_PASSANT,
        PROMOTION
    }
}
