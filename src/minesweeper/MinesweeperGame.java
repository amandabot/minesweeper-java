package minesweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class MinesweeperGame {

    private final static MinesweeperGame INSTANCE = new MinesweeperGame();
    private final static int[] bestScores = {999, 999, 999, 0};
    private final static int SQUARE_SIZE = 20;
    private static List<Color> colors;
    private static Map<String, Integer[]> gameSettings;
    private static Random random = new Random();
    private JFrame frame;
    private Square[][] squaresGrid;
    private JPanel labelPanel;
    private JPanel buttonLayer;
    private CustomDialog customGameDialog;
    private JLabel timerLabel;
    private int boardHeight;
    private int boardWidth;
    private int numOfMines;
    private int unclickedSquares;
    private int currentTime;
    private Timer timer;
    private boolean gameOver, first;
    private String gameType;

    /**
     * Returns an instance of a {@code MinesweeperGame }.
     * @return the {@code MinesweeperGame} instance
     */
    public static MinesweeperGame getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor for a Minesweeper game. Initializes game settings and GUI
     * elements, then starts a new game.
     */
    private MinesweeperGame() {
        initializeSettings();
        initializeGUIElements();
        createTimer();
        startNewGame();
    }

    /**
     * Starts a new game using the settings for the
     */
    private void startNewGame() {
        gameOver = false;
        first = true;
        currentTime = 0;
        unclickedSquares = (boardHeight * boardWidth) - numOfMines;
        squaresGrid = new Square[boardHeight][boardWidth];

        for (Square[] s : squaresGrid) {
            for (Square square : s) {
                square = new Square();
            }
        }

        int count = 0;
        int row;
        int column;
        //Populate mine field and mine count for all squares
        while (count < numOfMines) {
            column = random.nextInt(boardWidth);
            row = random.nextInt(boardHeight);

            if (squaresGrid[row][column].isBlank()) {
                squaresGrid[row][column].setType(Square.Type.MINE);

                for (int y = row - 1; y < row + 2; y++) {
                    for (int x = column - 1; x < column + 2; x++) {
                        setNeighborMineCount(y, x);
                    }
                }
                count += 1;
            }
        }
        createNewBoard();
    }

    public void createNewBoard() {
        buttonLayer.setLayout(new GridLayout(boardHeight, boardWidth));
        buttonLayer.setPreferredSize(new Dimension(
                (boardWidth * SQUARE_SIZE), (boardHeight * SQUARE_SIZE)));
        labelPanel.setLayout(new GridLayout(boardHeight, boardWidth));
        labelPanel.setPreferredSize(new Dimension(
                (boardWidth * SQUARE_SIZE), (boardHeight * SQUARE_SIZE)));
        JButton tempButton;
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                tempButton = new JButton();
                tempButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //Check here if game over or win, and return
                        if (gameOver) {
                            return;
                        }

                        //Check for first click, start timer if it is;
                        if (first) {
                            first = false;
                            timer.start();
                        }

                        JButton button = (JButton) e.getSource();
                        Point p = button.getLocation();
                        int row = (int) (p.getY() / SQUARE_SIZE);
                        int column = (int) (p.getX() / SQUARE_SIZE);
//
//                        if (squaresGrid[Y][X].isMine()) {
//                            timer.stop();
//                            gameOver = true;
//                            results = processMine();
//                            for (a = 0; a < boardHeight; a++) {
//                                for (b = 0; b < BoardWidth; b++) {
//                                    if (results[a][b]) {
//                                        fieldButtons[a][b].setVisible(false);
//                                    }
//                                }
//                            }
//                            fieldButtons[Y][X].setVisible(false);
//                            fieldLabels[Y][X].setOpaque(true);
//                            fieldLabels[Y][X].setBackground(Color.RED);
//                            timerLabel.setText("Game Over: " + String.format("%03d", currentTime));
//                            return;
//                        } else if (squaresGrid[Y][X].isBlank()) {
//                            results = processBlank(Y, X);
//                            for (a = 0; a < boardHeight; a++) {
//                                for (b = 0; b < BoardWidth; b++) {
//                                    if (results[a][b]) {
//                                        fieldButtons[a][b].setVisible(false);
//                                    }
//                                }
//                            }
//                        } //This is a number
//                        else {
//                            processNumber(Y, X);
//                            fieldButtons[Y][X].setVisible(false);
//                        }
//
//                        if (unclickedSquares == 0) {
//                            timer.stop();
//                            gameOver = true;
//                            if (getBestScores()[getType()] > currentTime) {
//                                setBestScore(currentTime, getType());
//                            }
//                            timerLabel.setText("You Win!: " + String.format("%03d", currentTime));
//                            saveSettings();
//                        }
                    }
                });
                buttonLayer.add(tempButton);
                labelPanel.add(createLabel(squaresGrid[y][x]));
            }
        }
    }

    //Used in startNewGame(); Increments the mine count of all squares within
    //a distance of 1 from the square (Y, X)
    private void setNeighborMineCount(int Y, int X) {
        //Prevents accessing out of bounds array indices
        if (X < 0 || X >= boardWidth || Y < 0 || Y >= boardHeight) {
            return;
        }

        if (!(squaresGrid[Y][X].isMine())) {
            squaresGrid[Y][X].increaseMineCount();
            squaresGrid[Y][X].setType(Square.Type.NUMBER);
        }
    }

    //The method checks if the square is a MINE, BLANK, or NUMBER, and
    //returns the corresponding response
    public Square.Type checkResult(int Y, int X) {
        return squaresGrid[Y][X].getType();
    }

    //Returns fieldMines array with a list of squares designated as mines
    public boolean[][] processMine() {
        boolean[][] fieldMines = new boolean[boardHeight][boardWidth];

        for (int a = 0; a < boardHeight; a++) {
            for (int b = 0; b < boardWidth; b++) {
                if (squaresGrid[a][b].isMine()) {
                    fieldMines[a][b] = true;
                }
            }
        }
        return fieldMines;
    }

    //Marks this NUMBER square as clicked and decrements unclicked count
    public void processNumber(int Y, int X) {
        squaresGrid[Y][X].setAsClicked();
        unclickedSquares--;
    }

    //Returns a list of blank and numbered spaces; the first 2 rows of the array
    //are the coordinates of the item; the third space is the number of mines.
    //Blanks are indicated by a 0 mine count
    public boolean[][] processBlank(int Y, int X) {
        boolean[][] results = new boolean[boardHeight][boardWidth];
        results[Y][X] = true;
        squaresGrid[Y][X].setAsClicked();
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
        if (X < 0 || X >= boardWidth || Y < 0 || Y >= boardHeight
                || (results[Y][X]) || squaresGrid[Y][X].isClicked()) {
            return;
        }

        results[Y][X] = true;
        squaresGrid[Y][X].setAsClicked();
        unclickedSquares--;

        if (squaresGrid[Y][X].isBlank()) {
            for (int a = Y - 1; a < Y + 2; a++) {
                for (int b = X - 1; b < X + 2; b++) {
                    checkNeighborSquares(a, b, results);
                }
            }
        }
    }

    /**
     * Creates a {@code JLabel} indicating if this space is a bomb, a number, or
     * an empty space. Convenience method for {@code createNewBoard}().
     *
     * @param square the {@code Square} to be labeled
     * @return the {@code JLabel} object
     */
    private JLabel createLabel(Square square) {
        JLabel tempJLabel = new JLabel(" ");

        if (square.isMine()) {
            tempJLabel.setText("B");
        } else if (square.isNumber()) {
            tempJLabel.setText(Integer.toString(square.getMineCount()));
        }
        tempJLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        return tempJLabel;
    }

    /**
     * Returns the designated {@code Color} for a square with {@code count}
     * bombs around it. This is a convenience method as bomb counts begin at 1
     * while the indices for the color array start at 0.
     *
     * @param count the number of bombs around the square to be colored
     * @return the color at {@code index}
     */
    private Color getColor(int count) {
        return colors.get(count - 1);
    }

//This class creates a dialog used to accept input for a custom game
    class CustomDialog extends JDialog {

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
            heightText.setText(Integer.toString(boardHeight));
            widthText = new JTextField(2);
            widthText.setText(Integer.toString(boardWidth));
            minesText = new JTextField(3);
            minesText.setText(Integer.toString(numOfMines));

            okButton = new JButton("OK");
            okButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int h, w, n;

                    try {
                        h = Integer.parseInt(heightText.getText());
                        w = Integer.parseInt(widthText.getText());
                        n = Integer.parseInt(minesText.getText());
                    } //If the input is invalid, the squaresGrid remains as is
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

                    //Set height, width, mine count, type for squaresGrid
                    boardHeight = h;
                    boardWidth = w;
                    numOfMines = n;
                    //setType(3);

                    setVisible(false);
                    createNewBoard();
                }
            });

            cancelButton = new JButton("Cancel");
            cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    startNewGame();
                }
            });

            customPanel.add(heightLabel);
            customPanel.add(heightText);
            customPanel.add(widthLabel);
            customPanel.add(widthText);
            customPanel.add(minesLabel);
            customPanel.add(minesText);
            customPanel.add(okButton);
            customPanel.add(cancelButton);
            add(customPanel);
            pack();
        }

        public void setDialogFields() {
            //Set height, width, mine count for this dialog
            heightText.setText(Integer.toString(boardHeight));
            widthText.setText(Integer.toString(boardWidth));
            minesText.setText(Integer.toString(numOfMines));
        }
    }

    //Game settings methods below
    public void setBestScore(int score, int type) {
        bestScores[type] = score;
    }

    public void saveSettings() {
        try {
            try (PrintWriter out = new PrintWriter(
                    new FileWriter("settings"))) {
                out.println(boardHeight + "/" + boardWidth + "/" + numOfMines + "/"
                        + bestScores[0] + "/" + bestScores[1] + "/" + bestScores[2]
                        + "/" + gameType);
            }
        } catch (IOException e) {
        }
    }

    /**
     * Reads previously saved game settings and initializes the in-game values
     * to those values. In the absence of previous settings or in case of an
     * error in reading them, the default skill level is chosen.
     */
    private void readPreviousSettings() {

        String temp;
        String[] typesAndScores;

        //Settings file: these lines indicate previous height, width, 
        //number of mines, best scores (beginner, intermediate, expert), 
        //and type (beginner (0), intermediate (1), expert (2), custom (3)

        try {
            try (BufferedReader io = new BufferedReader(
                    new FileReader("settings"))) {
                temp = io.readLine();
            }
            typesAndScores = temp.split("/");

            boardHeight = Integer.parseInt(typesAndScores[0]);
            boardWidth = Integer.parseInt(typesAndScores[1]);
            numOfMines = Integer.parseInt(typesAndScores[2]);
            bestScores[0] = Integer.parseInt(typesAndScores[3]);
            bestScores[1] = Integer.parseInt(typesAndScores[4]);
            bestScores[2] = Integer.parseInt(typesAndScores[5]);
            gameType = typesAndScores[6];
        } catch (NumberFormatException N) {
            System.out.println("NFE");
        } catch (FileNotFoundException F) {
            System.out.println("FNFE");
        } catch (IOException E) {
            System.out.println("IOE");
        }
    }

    /**
     * Initializes the static settings of a Minesweeper game that will be reused
     * and will remain unchanged across games. Determines the skill level of the
     * game based on previously saved settings.
     */
    private void initializeSettings() {
        colors = new ArrayList<>(Arrays.asList(
                Color.BLUE,
                Color.GREEN,
                Color.RED,
                Color.MAGENTA,
                Color.YELLOW,
                Color.ORANGE,
                Color.DARK_GRAY,
                Color.WHITE));

        //The settings in Integer[] are boardHeight, boardWidth, and numOfMines
        gameSettings = new HashMap<>();
        gameSettings.put("beginner", new Integer[]{9, 9, 10});
        gameSettings.put("intermediate", new Integer[]{16, 16, 40});
        gameSettings.put("expert", new Integer[]{16, 30, 99});
        gameSettings.put("custom", new Integer[]{16, 30, 40});

        readPreviousSettings();
    }

    /**
     * Sets the game skill level type and sets the height, width and number of
     * mines associated with that skill level.
     *
     * @param type the skill level type
     */
    private void setGameType(String type) {
        gameType = type;
        boardHeight = gameSettings.get(type)[0];
        boardWidth = gameSettings.get(type)[1];
        numOfMines = gameSettings.get(type)[2];
    }

    /**
     * Creates a {@code Timer} that fires on 1 second intervals and increments
     * the time.
     */
    private void createTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTime = Math.min(999, currentTime + 1);
                timerLabel.setText(String.format("%03d", currentTime));
            }
        });
        timer.setInitialDelay(0);
    }

    //GUI-related methods below
    /**
     * Creates a {@code JMenuBar} object for a Minesweeper game. The menu bar
     * contains options for starting a new game (of the current skill level),
     * changing between skill levels, choosing a custom game, showing the best
     * scores for each skill level, and exiting the game. All menu items have a
     * mnemonic key and selected items have an key accelerator.
     *
     * @return the JMenuBar
     */
    private JMenuBar createMenuBar() {
        JMenuItem newMenu = new JMenuItem("New");
        newMenu.setMnemonic('N');
        newMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        newMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');
        gameMenu.add(newMenu);
        gameMenu.addSeparator();

        JRadioButtonMenuItem beginner = new JRadioButtonMenuItem("Beginner");
        beginner.setMnemonic('B');
        addActionListener(beginner, "beginner");
        ButtonGroup BGmenuBar = new ButtonGroup();
        BGmenuBar.add(beginner);
        gameMenu.add(beginner);

        JRadioButtonMenuItem intermediate = new JRadioButtonMenuItem("Intermediate");
        intermediate.setMnemonic('I');
        addActionListener(intermediate, "intermediate");
        BGmenuBar.add(intermediate);
        gameMenu.add(intermediate);

        JRadioButtonMenuItem expert = new JRadioButtonMenuItem("Expert");
        expert.setMnemonic('E');
        addActionListener(expert, "expert");
        BGmenuBar.add(expert);
        gameMenu.add(expert);

        JRadioButtonMenuItem custom = new JRadioButtonMenuItem("Custom...");
        custom.setMnemonic('C');
        custom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customGameDialog = new CustomDialog(frame);
                customGameDialog.setDialogFields();
                customGameDialog.setVisible(true);
            }
        });
        BGmenuBar.add(custom);
        gameMenu.add(custom);
        gameMenu.addSeparator();

        JMenuItem bestTimes = new JMenuItem("Best Times...");
        bestTimes.setMnemonic('T');
        bestTimes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame,
                        String.format(
                        "Beginner: %s\nIntermediate: %s\n Expert: %s",
                        bestScores[0], bestScores[1], bestScores[2]),
                        "Best Times",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
        gameMenu.add(bestTimes);
        gameMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('x');
        exit.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        gameMenu.add(exit);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        return menuBar;
    }

    /**
     * Adds an {@code ActionListener} to {@code item}. This is a convenience
     * method used by createMenuBar().
     *
     * @param item the item to which the ActionListener will be added
     * @param type the type of game associated with this item
     */
    private void addActionListener(JRadioButtonMenuItem item, final String type) {
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setGameType(type);
                startNewGame();
            }
        });
    }

    /**
     * Creates the {@code JPanel} on which the timer and game board are
     * displayed. The components added to this panel do not depend on the game
     * logic, so it is okay for them to be created before a new game. Because of
     * this, this panel and its children have no size; however, their sizes will
     * be determined at the start of each new game.
     *
     * @return the unsized game board
     */
    private JPanel initializeGamePanel() {
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        timerPanel.add(timerLabel);
        buttonLayer = new JPanel();
        buttonLayer.setOpaque(false);
        labelPanel = new JPanel();
        labelPanel.setOpaque(true);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.add(labelPanel, new Integer(0));
        layeredPane.add(buttonLayer, new Integer(1));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(timerPanel, BorderLayout.NORTH);
        panel.add(layeredPane, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Creates a {@code JFrame} to display the game and sets the frame's
     * options. These elements do not depend on the game logic, so it is okay
     * for them to be created before a new game.
     */
    private void initializeGUIElements() {
        frame = new JFrame();
        frame.setJMenuBar(createMenuBar());
        frame.add(initializeGamePanel());
        frame.setTitle("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }

    /**
     * Shows the window in which the game plays after the initial new game has
     * been created.
     */
    private void packAndShowFrame() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}