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
    private final static int SQUARE_SIZE = 20;
    private static List<Color> colors;
    private static Map<String, Integer> bestScores;
    private static Map<String, Integer[]> gameSettings;
    private Random random = new Random();
    private JFrame frame;
    //Holds the Squares that correspond to each grid square on the board
    private Square[][] squaresGrid;
    private JPanel labelPanel; //Displays uncovered numbers, mines and blanks
    private JPanel buttonLayer; //Covers the labelPanel to hide mines, etc.
    private CustomDialog customGameDialog; //
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
     *
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
        for (int i = 0; i < boardHeight; i++) {
            Arrays.fill(squaresGrid[i], new Square());
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
        packAndShowFrame();
    }

    /**
     * Increments the mine count of the {@code Square} at ({@code column},
     * {@code row}) unless the Square is already a mine or if it lies outside
     * the bounds of the board.
     *
     * @param row the y coordinate of the neighbor {@code Square}
     * @param column the x coordinate of the neighbor {@code Square}
     */
    private void setNeighborMineCount(int row, int column) {
        //Prevents accessing out of bounds array indices
        if (column < 0 || column >= boardWidth || row < 0 || row >= boardHeight) {
            return;
        }

        //Only NUMBER and BLANK Squares are incremented
        if (!(squaresGrid[row][column].isMine())) {
            squaresGrid[row][column].increaseMineCount();
            squaresGrid[row][column].setType(Square.Type.NUMBER);
        }
    }

    /**
     * Creates GUI elements dependent on the game settings and previously
     * initialized in the constructor. These elements include the label layer
     * which displays whether a square is a bomb, blank, or number square, and
     * the button layer which lies over the label layer and hides each square.
     */
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

                        //Check for first click, start timer if it is
                        if (first) {
                            first = false;
                            timer.start();
                        }
                        processButton((JButton) e.getSource());
                    }
                });
                squaresGrid[y][x].setButton(tempButton);
                buttonLayer.add(tempButton);

                JLabel tempLabel = createLabel(squaresGrid[y][x]);
                squaresGrid[y][x].setLabel(tempLabel);
                labelPanel.add(tempLabel);
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
        int count;

        if (square.isMine()) {
            tempJLabel.setText("B");
        } else if (square.isNumber()) {
            count = square.getMineCount();
            tempJLabel.setText(Integer.toString(count));
            tempJLabel.setForeground(getColor(count));
        }
        tempJLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        return tempJLabel;
    }

    /**
     * Updates the status of the board based on the type of button clicked.
     *
     * @param button the clicked button to be processed
     */
    private void processButton(JButton button) {

        Point p = button.getLocation();
        int row = (int) (p.getY() / SQUARE_SIZE);
        int column = (int) (p.getX() / SQUARE_SIZE);
        Square square = squaresGrid[row][column];

        if (square.isMine()) {
            processMine(square);
        } else {
            processNumberOrBlank(row, column);
        }
        checkForWin();
    }

    /**
     * Checks if the game is in a won state and stops the game if that is the
     * case.
     */
    private void checkForWin() {
        if (unclickedSquares == 0) {
            timer.stop();
            gameOver = true;
            if (bestScores.get(gameType) > currentTime) {
                bestScores.put(gameType, currentTime);
            }
            timerLabel.setText("You Win!: " + String.format("%03d", currentTime));
            saveSettings();
        }
    }

    /**
     * Ends the game and shows all remaining bombs on the board.
     *
     * @param square the mine which was clicked
     */
    private void processMine(Square square) {
        timer.stop();
        gameOver = true;
        timerLabel.setText("Game Over: " + String.format("%03d", currentTime));

        //Removes the buttons over any mine on the board
        for (int row = 0; row < boardHeight; row++) {
            for (int column = 0; column < boardWidth; column++) {
                if (squaresGrid[row][column].isMine()) {
                    squaresGrid[row][column].getButton().setVisible(false);
                }
            }
        }
        square.getLabel().setOpaque(true);
        square.getLabel().setBackground(Color.RED);
    }

    /**
     * Updates the board after a NUMBER or BLANK {@code Square} is clicked. The
     * {@code Square} is hidden and marked as clicked, and the
     * {@code unclickedSquares} field is decremented. For a blank
     * {@code Square}, the surrounding neighbors are recursively checked and
     * processed until no remaining BLANK neighbors are present.
     *
     * @param row the row of the {@code Square} to be checked
     * @param column the column of the {@code Square} to be checked
     */
    private void processNumberOrBlank(int row, int column) {
        //Checks if the square is unclicked and within the bounds of the board
        if (column < 0 || column >= boardWidth || row < 0 || row >= boardHeight
                || squaresGrid[row][column].isClicked()) {
            return;
        }
        Square square = squaresGrid[row][column];
        square.getButton().setVisible(false);
        square.setAsClicked();
        unclickedSquares--;

        if (square.isBlank()) {
            for (int y = row - 1; y < row + 2; y++) {
                for (int x = column - 1; x < column + 2; x++) {
                    processNumberOrBlank(y, x);
                }
            }
        }
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

    /**
     * Saves the current settings of the game, including the game type, height
     * and width of the board, number of mines, and the best scores for each
     * skill level. Displays an error message if the settings were not saved
     * correctly.
     */
    public void saveSettings() {
        try {
            try (PrintWriter out = new PrintWriter(
                    new FileWriter("settings"))) {
                out.println(String.format(
                        "%s/%d/%d/%d/%d/%d/%d",
                        gameType,
                        boardHeight,
                        boardWidth,
                        numOfMines,
                        bestScores.get("beginner"),
                        bestScores.get("intermediate"),
                        bestScores.get("expert")));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Could not save settings.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reads previously saved game settings and initializes the in-game values
     * to those values. In the absence of previous settings or in case of an
     * error in reading them, the default skill level is chosen.
     */
    private void readPreviousSettings() {
        try {
            String temp;
            try (BufferedReader io = new BufferedReader(
                    new FileReader("settings"))) {
                temp = io.readLine();
            }
            String[] typesAndScores = temp.split("/");

            setGameType("beginner"); //default choice
            temp = typesAndScores[0];
            if (temp.equalsIgnoreCase("intermediate")
                    || temp.equalsIgnoreCase("expert")) {
                setGameType(temp);
            } else if (temp.equalsIgnoreCase("custom")) {
                boardHeight = Integer.parseInt(typesAndScores[0]);
                boardWidth = Integer.parseInt(typesAndScores[1]);
                numOfMines = Integer.parseInt(typesAndScores[2]);
            }
            bestScores.put("beginner", Integer.parseInt(typesAndScores[3]));
            bestScores.put("intermediate", Integer.parseInt(typesAndScores[4]));
            bestScores.put("expert", Integer.parseInt(typesAndScores[5]));
        } catch (NumberFormatException | FileNotFoundException e) {
            //In case of a parse error or file reading error, the default game 
            //type is beginner. The best scores are initialized to default values as
            //well.
            setGameType("beginner");
        } catch (IOException e) {
            setGameType("beginner");
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

        bestScores = new HashMap<>();
        bestScores.put("beginner", new Integer(999));
        bestScores.put("intermediate", new Integer(999));
        bestScores.put("expert", new Integer(999));
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
                //Check if the Custom Dialog has been instantiated yet
                if (customGameDialog == null) {
                    customGameDialog = new CustomDialog(frame);
                }
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
                        bestScores.get("beginner"),
                        bestScores.get("intermediate"),
                        bestScores.get("expert")),
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
        timerLabel = new JLabel("000");
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