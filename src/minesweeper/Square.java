package minesweeper;

public class Square
{
    private boolean clicked;
    private int mineCount;
    private Type squareType;

    public enum Type {BLANK, MINE, NUMBER};

    public Square()
    {
        clicked = false;
        mineCount = 0;
        squareType = Type.BLANK;
    }

    public void setType(Type squareType)
    {
        this.squareType = squareType;
    }

    public boolean isClicked()
    {
        return clicked;
    }

    public void setAsClicked()
    {
        clicked = true;
    }

    public int getMineCount()
    {
        return mineCount;
    }

    public void increaseMineCount()
    {
        mineCount++;
    }

    public boolean isMine()
    {
        return (this.squareType == Type.MINE);
    }

    public boolean isBlank()
    {
        return (this.squareType == Type.BLANK);
    }

    public boolean isNumber()
    {
        return (this.squareType == Type.NUMBER);
    }

    public Type getType()
    {
        return squareType;
    }
}