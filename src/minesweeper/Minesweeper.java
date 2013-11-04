/* This class creates the field of mines, numbers, and blanks; and it handles
 * the logic for dealing with clicked squares. This class allows the GUI to
 * communicate with the individual Squares. Max size is 24 x 30
 */
package minesweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;


public class Minesweeper {
    private final static Minesweeper INSTANCE = new Minesweeper();
    
    static boolean gameOver, first;
    int fieldHeight, fieldWidth, currentTime;
    Square[][] field1;
    Minesweeper ms;
    JButton[][] fieldButtons;
    JLabel[][] fieldLabels;
    JPanel labelPanel, buttonPanel;
    JLayeredPane layeredPane;
    ButtonGroup BGmenuBar;
    JMenuBar menuBar;
    JMenu gameMenu;
    JMenuItem MInew, MIbestTimes, MIexit;
    JRadioButtonMenuItem RBMIbeginner, RBMIintermediate, RBMIexpert, RBMIcustom;
    MineSweeperFrame.CustomDialog csd;
    Timer timer;
    JLabel timerLabel;
    Point clickLocation;
    JFrame frame;
    //height and width are height and width of the field

    static int height = 9, width = 9, numberOfMines = 10, unclickedSquares;
    private Square[][] field;
    private int[] bestScores = {999, 999, 999, 0};
    private int type = 0;
    
    public static Minesweeper getInstance() {
        return INSTANCE;
    }

    private Minesweeper() {
        frame = new JFrame();
                //Frame Properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Minesweeper");
        frame.setResizable(false);

        //Create mine field1 creator
        ms = new Minesweeper();

        //Create MenuBar
        menuBar = new JMenuBar();
        createMenuBar();
        frame.setJMenuBar(menuBar);

        //Create Timer
        timer = new Timer(1000, this);
        timer.setInitialDelay(0);
        timerLabel = new JLabel("000", JLabel.CENTER);
        timerLabel.setForeground(Color.RED);

        //Determines what type of game to start with
        switch (ms.getType()) {
            case 0:
                RBMIbeginner.doClick();
                break;
            case 1:
                RBMIintermediate.doClick();
                break;
            case 2:
                RBMIexpert.doClick();
                break;
            case 3:
                RBMIcustom.setSelected(true);
                createNewBoard();
                break;
        }

        //Create Dialog for Custom Board
        csd = new CustomDialog(frame);
        String temp;
        String[] typesAndScores;

        //Settings file: these lines indicate previous height, width, 
        //number of mines, best scores (beginner, intermediate, expert), 
        //and type (beginner (0), intermediate (1), expert (2), custom (3)
        try {
            BufferedReader io = new BufferedReader(
                    new FileReader("settings"));
            temp = io.readLine();
            io.close();
            typesAndScores = temp.split("/");

            height = Integer.parseInt(typesAndScores[0]);
            width = Integer.parseInt(typesAndScores[1]);
            numberOfMines = Integer.parseInt(typesAndScores[2]);
            bestScores[0] = Integer.parseInt(typesAndScores[3]);
            bestScores[1] = Integer.parseInt(typesAndScores[4]);
            bestScores[2] = Integer.parseInt(typesAndScores[5]);
            type = Integer.parseInt(typesAndScores[6]);
        } catch (NumberFormatException N) {
            System.out.println("NFE");
        } catch (FileNotFoundException F) {
            System.out.println("FNFE");
        } catch (IOException E) {
            System.out.println("IOE");
        }
    }

    //Creates a new field using input from the user.
    public void startNewGame() {
        int a = 0, X, Y;
        Random rd = new Random();

        unclickedSquares = (height * width) - numberOfMines;

        field = new Square[height][width];

        for (int b = 0; b < height; b++) {
            for (int c = 0; c < width; c++) {
                field[b][c] = new Square();
            }
        }

        //Populate mine field and mine count for all squares
        while (a < numberOfMines) {
            X = rd.nextInt(width);
            Y = rd.nextInt(height);

            if (!field[Y][X].isMine()) {
                field[Y][X].setType(Square.Type.MINE);

                for (int d = Y - 1; d < Y + 2; d++) {
                    for (int e = X - 1; e < X + 2; e++) {
                        setNeighborMineCount(d, e);
                    }
                }
                a++;
            }
        }
    }

    //Used in startNewGame(); Increments the mine count of all squares within
    //a distance of 1 from the square (Y, X)
    private void setNeighborMineCount(int Y, int X) {
        //Prevents accessing out of bounds array indices
        if (X < 0 || X >= width || Y < 0 || Y >= height) {
            return;
        }

        if (!(field[Y][X].isMine())) {
            field[Y][X].increaseMineCount();
            field[Y][X].setType(Square.Type.NUMBER);
        }
    }

    //The method checks if the square is a MINE, BLANK, or NUMBER, and
    //returns the corresponding response
    public Square.Type checkResult(int Y, int X) {
        return field[Y][X].getType();
    }

    //Returns fieldMines array with a list of squares designated as mines
    public boolean[][] processMine() {
        boolean[][] fieldMines = new boolean[height][width];

        for (int a = 0; a < height; a++) {
            for (int b = 0; b < width; b++) {
                if (field[a][b].isMine()) {
                    fieldMines[a][b] = true;
                }
            }
        }
        return fieldMines;
    }

    //Marks this NUMBER square as clicked and decrements unclicked count
    public void processNumber(int Y, int X) {
        field[Y][X].setAsClicked();
        unclickedSquares--;
    }

    //Returns a list of blank and numbered spaces; the first 2 rows of the array
    //are the coordinates of the item; the third space is the number of mines.
    //Blanks are indicated by a 0 mine count
    public boolean[][] processBlank(int Y, int X) {
        boolean[][] results = new boolean[height][width];
        results[Y][X] = true;
        field[Y][X].setAsClicked();
        unclickedSquares--;

        for (int a = Y - 1; a < Y + 2; a++) {
            for (int b = X - 1; b < X + 2; b++) {
                checkNeighborSquares(a, b, results);
            }
        }
        return results;
    }

    //Y, X is the position in the field array of this square; results is the
    //array of squares to be changed in the GUI. index is the array index.
    //If this neighbor is a blank, the neighbors are checked for more blanks
    //recursively until there are no more blank spaces connected here.
    public void checkNeighborSquares(int Y, int X, boolean[][] results) {
        //Prevents accessing out of bounds array indices,
        //or if this has been checked already
        if (X < 0 || X >= width || Y < 0 || Y >= height
                || (results[Y][X]) || field[Y][X].isClicked()) {
            return;
        }

        results[Y][X] = true;
        field[Y][X].setAsClicked();
        unclickedSquares--;

        if (field[Y][X].isBlank()) {
            for (int a = Y - 1; a < Y + 2; a++) {
                for (int b = X - 1; b < X + 2; b++) {
                    checkNeighborSquares(a, b, results);
                }
            }
        }
    }

    public Square[][] getField() {
        return field;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        Minesweeper.width = width;
    }

    public void setHeight(int height) {
        Minesweeper.height = height;
    }

    public void setNumberOfMines(int number) {
        numberOfMines = number;
    }

    public int getNumberOfMines() {
        return numberOfMines;
    }

    public int[] getBestScores() {
        return bestScores;
    }

    public void setType(int type) {
        this.type = type;
    }

    //This is the type of game being played right now (beginner, expert, etc)
    public int getType() {
        return type;
    }

    //Writes settings to file for next time
    public void saveSettings() {
        try {
            PrintWriter out = new PrintWriter(
                    new FileWriter("settings"));
            out.println(height + "/" + width + "/" + numberOfMines + "/"
                    + bestScores[0] + "/" + bestScores[1] + "/" + bestScores[2]
                    + "/" + type);
            out.close();
        } catch (IOException e) {
        }
    }

    //Sets the best scores (begin, intermediate, expert)
    public void setBestScore(int score, int type) {
        bestScores[type] = score;
    }
    
    

    public void createNewBoard() {
        int a, b;

        timer.stop();
        gameOver = false;
        first = true;
        currentTime = 0;
        timerLabel.setText("000");
        frame.getContentPane().removeAll();

        ms.startNewGame();
        field1 = ms.getField();
        fieldHeight = ms.getHeight();
        fieldWidth = ms.getWidth();

        fieldButtons = new JButton[fieldHeight][fieldWidth];

        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(fieldHeight, fieldWidth));
        buttonPanel.setBounds(5, 5, (20 * fieldWidth), (20 * fieldHeight));

        for (a = 0; a < fieldHeight; a++) {
            for (b = 0; b < fieldWidth; b++) {
                fieldButtons[a][b] = new JButton();
                fieldButtons[a][b].addMouseListener(this);
                buttonPanel.add(fieldButtons[a][b]);
            }
        }

        //Create grid of labels to display mines, numbers and blanks
        fieldLabels = new JLabel[fieldHeight][fieldWidth];

        labelPanel = new JPanel();
        labelPanel.setOpaque(true);
        labelPanel.setLayout(new GridLayout(fieldHeight, fieldWidth));
        labelPanel.setBounds(5, 5, (20 * (fieldWidth)), (20 * (fieldHeight)));


        for (a = 0; a < fieldHeight; a++) {
            for (b = 0; b < fieldWidth; b++) {
                if (field1[a][b].isMine()) {
                    fieldLabels[a][b] = new JLabel("B", JLabel.CENTER);
                    fieldLabels[a][b].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    labelPanel.add(fieldLabels[a][b]);
                } else if (field1[a][b].isNumber()) {
                    fieldLabels[a][b] = new JLabel(
                            Integer.toString(field1[a][b].getMineCount()),
                            JLabel.CENTER);
                    fieldLabels[a][b].setForeground(getFGColor(field1[a][b].getMineCount()));
                    fieldLabels[a][b].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    labelPanel.add(fieldLabels[a][b]);
                } else {
                    fieldLabels[a][b] = new JLabel(" ", JLabel.CENTER);
                    fieldLabels[a][b].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    labelPanel.add(fieldLabels[a][b]);
                }
            }
        }

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(
                new Dimension((20 * fieldWidth) + 10, (20 * fieldHeight) + 10));
        layeredPane.add(labelPanel, new Integer(0));
        layeredPane.add(buttonPanel, new Integer(1));

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(timerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(layeredPane, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        ms.saveSettings();
    }

//Used in constructor to create NUMBER label foreground colors
    public Color getFGColor(int count) {
        switch (count) {
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.RED;
            case 4:
                return Color.MAGENTA;
            case 5:
                return Color.YELLOW;
            case 6:
                return Color.ORANGE;
            case 7:
                return Color.DARK_GRAY;
            case 8:
                return Color.WHITE;
            default:
                return Color.BLACK;
        }
    }

//Mouse Event Handlers
    public void mouseClicked(MouseEvent e) {
        int a, b, X, Y;
        boolean[][] results;
        Point p;

        //Check here if game over or win, and return
        if (gameOver) {
            return;
        }

        //Check for first click, start timer if it is;
        if (first) {
            first = false;
            timer.start();
        }

        p = e.getComponent().getLocation();
        Y = (int) p.getY() / 20;
        X = (int) p.getX() / 20;

        if (field1[Y][X].isMine()) {
            timer.stop();
            gameOver = true;
            results = ms.processMine();
            for (a = 0; a < fieldHeight; a++) {
                for (b = 0; b < fieldWidth; b++) {
                    if (results[a][b]) {
                        fieldButtons[a][b].setVisible(false);
                    }
                }
            }
            fieldButtons[Y][X].setVisible(false);
            fieldLabels[Y][X].setOpaque(true);
            fieldLabels[Y][X].setBackground(Color.RED);
            timerLabel.setText("Game Over: " + String.format("%03d", currentTime));
            return;
        } else if (field1[Y][X].isBlank()) {
            results = ms.processBlank(Y, X);
            for (a = 0; a < fieldHeight; a++) {
                for (b = 0; b < fieldWidth; b++) {
                    if (results[a][b]) {
                        fieldButtons[a][b].setVisible(false);
                    }
                }
            }
        } //This is a number
        else {
            ms.processNumber(Y, X);
            fieldButtons[Y][X].setVisible(false);
        }

        if (Minesweeper.unclickedSquares == 0) {
            timer.stop();
            gameOver = true;
            if (ms.getBestScores()[ms.getType()] > currentTime) {
                ms.setBestScore(currentTime, ms.getType());
            }
            timerLabel.setText("You Win!: " + String.format("%03d", currentTime));
            ms.saveSettings();
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

//Action Event and Item Event Handlers (used for menubar and timer)
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == timer) {
            currentTime = Math.min(999, currentTime + 1);
            timerLabel.setText(String.format("%03d", currentTime));
        } else if (source == MInew) {
            createNewBoard();
        } else if (source == MIbestTimes) {
            JOptionPane.showMessageDialog(frame,
                    "Beginner : " + ms.getBestScores()[0]
                    + "\nIntermediate: " + ms.getBestScores()[1]
                    + "\nExpert: " + ms.getBestScores()[2],
                    "Best Times", JOptionPane.PLAIN_MESSAGE);
        } else if (source == MIexit) {
            System.exit(0);
        } else if (source == RBMIbeginner) {
            ms.setHeight(9);
            ms.setWidth(9);
            ms.setNumberOfMines(10);
            ms.setType(0);
            createNewBoard();
        } else if (source == RBMIintermediate) {
            ms.setHeight(16);
            ms.setWidth(16);
            ms.setNumberOfMines(40);
            ms.setType(1);
            createNewBoard();
        } else if (source == RBMIexpert) {
            ms.setHeight(16);
            ms.setWidth(30);
            ms.setNumberOfMines(99);
            ms.setType(2);
            createNewBoard();
        } else if (source == RBMIcustom) {
            csd.setDialogFields();
            csd.setVisible(true);
        }
    }

//This creates the menubar for the frame
    public void createMenuBar() {
        BGmenuBar = new ButtonGroup();

        //Game Menu
        gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        MInew = new JMenuItem("New");
        MInew.setMnemonic('N');
        MInew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        MInew.addActionListener(this);
        gameMenu.add(MInew);
        gameMenu.addSeparator();

        RBMIbeginner = new JRadioButtonMenuItem("Beginner");
        RBMIbeginner.setMnemonic('B');
        RBMIbeginner.addActionListener(this);
        BGmenuBar.add(RBMIbeginner);
        gameMenu.add(RBMIbeginner);

        RBMIintermediate = new JRadioButtonMenuItem("Intermediate");
        RBMIintermediate.setMnemonic('I');
        RBMIintermediate.addActionListener(this);
        BGmenuBar.add(RBMIintermediate);
        gameMenu.add(RBMIintermediate);

        RBMIexpert = new JRadioButtonMenuItem("Expert");
        RBMIexpert.setMnemonic('E');
        RBMIexpert.addActionListener(this);
        BGmenuBar.add(RBMIexpert);
        gameMenu.add(RBMIexpert);

        RBMIcustom = new JRadioButtonMenuItem("Custom...");
        RBMIcustom.setMnemonic('C');
        RBMIcustom.addActionListener(this);
        BGmenuBar.add(RBMIcustom);
        gameMenu.add(RBMIcustom);
        gameMenu.addSeparator();

        MIbestTimes = new JMenuItem("Best Times...");
        MIbestTimes.setMnemonic('T');
        MIbestTimes.addActionListener(this);
        gameMenu.add(MIbestTimes);
        gameMenu.addSeparator();

        MIexit = new JMenuItem("Exit");
        MIexit.setMnemonic('x');
        MIexit.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        MIexit.addActionListener(this);

        gameMenu.add(MIexit);
        menuBar.add(gameMenu);
    }

//This class creates a dialog used to accept input for a custom game
    class CustomDialog extends JDialog implements ActionListener {

        JLabel heightLabel, widthLabel, minesLabel;
        JTextField heightText, widthText, minesText;
        JButton okButton, cancelButton;
        JPanel customPanel;

        public CustomDialog(JFrame frame) {
            super(frame, true);
            customPanel = new JPanel();
            customPanel.setLayout(new GridLayout(4, 2, 5, 5));
            customPanel.setPreferredSize(new Dimension(215, 156));
            customPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            heightLabel = new JLabel("Height", JLabel.CENTER);
            widthLabel = new JLabel("Width", JLabel.CENTER);
            minesLabel = new JLabel("Number of Mines", JLabel.CENTER);
            heightText = new JTextField(2);
            heightText.setText(Integer.toString(fieldHeight));
            widthText = new JTextField(2);
            widthText.setText(Integer.toString(fieldWidth));
            minesText = new JTextField(3);
            minesText.setText(Integer.toString(ms.getNumberOfMines()));

            okButton = new JButton("OK");
            okButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            okButton.addActionListener(this);
            cancelButton = new JButton("Cancel");
            cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            cancelButton.addActionListener(this);

            customPanel.add(heightLabel);
            customPanel.add(heightText);
            customPanel.add(widthLabel);
            customPanel.add(widthText);
            customPanel.add(minesLabel);
            customPanel.add(minesText);
            customPanel.add(okButton);
            customPanel.add(cancelButton);

            getContentPane().add(customPanel);
            pack();
        }

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            int h, w, n;

            if (source == okButton) {
                try {
                    h = Integer.parseInt(heightText.getText());
                    w = Integer.parseInt(widthText.getText());
                    n = Integer.parseInt(minesText.getText());
                } //If the input is invalid, the field1 remains as is
                catch (NumberFormatException nfe) {
                    setVisible(false);
                    createNewBoard();
                    return;
                }

                //Checks limits for height, width, number of mines. If the height or
                //width is too low, it selects the lower bound; too high, and the
                //upper bound is chosen. Mine count can be from 10 to the product of
                //(height - 1) and (width - 1).
                h = Math.min(h, 24);
                h = Math.max(h, 9);
                w = Math.min(w, 30);
                w = Math.max(w, 9);
                n = Math.min(n, ((h - 1) * (w - 1)));
                n = Math.max(n, 10);

                //Set height, width, mine count, type for field1
                ms.setHeight(h);
                ms.setWidth(w);
                ms.setNumberOfMines(n);
                ms.setType(3);
            }
            setVisible(false);
            createNewBoard();
        }

        public void setDialogFields() {
            //Set height, width, mine count for this dialog
            heightText.setText(Integer.toString(ms.getHeight()));
            widthText.setText(Integer.toString(ms.getWidth()));
            minesText.setText(Integer.toString(ms.getNumberOfMines()));
        }
    }
}