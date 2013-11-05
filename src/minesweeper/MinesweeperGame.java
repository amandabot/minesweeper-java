/* This class creates the field of mines, numbers, and blanks; and it handles
 * the logic for dealing with clicked squares. This class allows the GUI to
 * communicate with the individual Squares. Max size is 24 x 30
 */
package minesweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
    private int boardHeight;
    private int boardWidth;
    private JFrame frame;
    private Square[][] squaresGrid;
    private JButton[][] buttonGrid;
    private JLabel[][] labelGrid;

    public static MinesweeperGame getInstance() {
        return INSTANCE;
    }
    //-------------------????-------------------------------
    static boolean gameOver, first;
    int currentTime;
    JPanel labelPanel, buttonLayer;
    JLayeredPane layeredPane;
    CustomDialog csd;
    Timer timer;
    JLabel timerLabel;
    //height and width are height and width of the field
    static int height = 9, width = 9, numberOfMines = 10, unclickedSquares;
    private int type = 0;

    private MinesweeperGame() {
        initializeGameSettings();
        createTimer();
        initializeGame();
        createAndShowFrame();



        //Determines what type of game to start with
//        switch (getType()) {
//            case 0:
//                RBMIbeginner.doClick();
//                break;
//            case 1:
//                RBMIintermediate.doClick();
//                break;
//            case 2:
//                RBMIexpert.doClick();
//                break;
//            case 3:
//                RBMIcustom.setSelected(true);
//                createNewBoard();
//                break;
//        }

    }

    //Creates a new field using input from the user.
    public void startNewGame() {
        int a = 0, X, Y;

        unclickedSquares = (height * width) - numberOfMines;

        squaresGrid = new Square[height][width];

        for (int b = 0; b < height; b++) {
            for (int c = 0; c < width; c++) {
                squaresGrid[b][c] = new Square();
            }
        }

        //Populate mine field and mine count for all squares
        while (a < numberOfMines) {
            X = random.nextInt(width);
            Y = random.nextInt(height);

            if (!squaresGrid[Y][X].isMine()) {
                squaresGrid[Y][X].setType(Square.Type.MINE);

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
        boolean[][] fieldMines = new boolean[height][width];

        for (int a = 0; a < height; a++) {
            for (int b = 0; b < width; b++) {
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
        boolean[][] results = new boolean[height][width];
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
        if (X < 0 || X >= width || Y < 0 || Y >= height
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

    public Square[][] getField() {
        return squaresGrid;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        MinesweeperGame.width = width;
    }

    public void setHeight(int height) {
        MinesweeperGame.height = height;
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
        
        boardHeight = getHeight();
        boardWidth = getWidth();
        buttonGrid = new JButton[boardHeight][boardWidth];

        buttonLayer = new JPanel();
        buttonLayer.setOpaque(false);
        buttonLayer.setLayout(new GridLayout(boardHeight, boardWidth));
        buttonLayer.setPreferredSize(new Dimension(
                (boardWidth * SQUARE_SIZE), (boardHeight * SQUARE_SIZE)));
        //----------------------------------????----------
        int i, j;

        



        for (i = 0; i < boardHeight; i++) {
            for (j = 0; j < boardWidth; j++) {
                buttonGrid[i][j] = new JButton();
                buttonGrid[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
//                        int a, b, X, Y;
//                        boolean[][] results;
//                        Point p;
//
//                        //Check here if game over or win, and return
//                        if (gameOver) {
//                            return;
//                        }
//
//                        //Check for first click, start timer if it is;
//                        if (first) {
//                            first = false;
//                            timer.start();
//                        }
//
//                        p = e.getComponent().getLocation();
//                        Y = (int) p.getY() / 20;
//                        X = (int) p.getX() / 20;
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
//                        if (MinesweeperGame.unclickedSquares == 0) {
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
                buttonLayer.add(buttonGrid[i][j]);
            }
        }

        //Create grid of labels to display mines, numbers and blanks
        labelGrid = new JLabel[boardHeight][boardWidth];

        labelPanel = new JPanel();
        labelPanel.setOpaque(true);
        labelPanel.setLayout(new GridLayout(boardHeight, boardWidth));
        labelPanel.setBounds(5, 5, (20 * (boardWidth)), (20 * (boardHeight)));


        for (i = 0; i < boardHeight; i++) {
            for (j = 0; j < boardWidth; j++) {
                if (squaresGrid[i][j].isMine()) {
                    labelGrid[i][j] = new JLabel("B", JLabel.CENTER);
                    labelGrid[i][j].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    labelPanel.add(labelGrid[i][j]);
                } else if (squaresGrid[i][j].isNumber()) {
                    labelGrid[i][j] = new JLabel(
                            Integer.toString(squaresGrid[i][j].getMineCount()),
                            JLabel.CENTER);
                    labelGrid[i][j].setForeground(getColor(squaresGrid[i][j].getMineCount()));
                    labelGrid[i][j].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    labelPanel.add(labelGrid[i][j]);
                } else {
                    labelGrid[i][j] = new JLabel(" ", JLabel.CENTER);
                    labelGrid[i][j].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                    labelPanel.add(labelGrid[i][j]);
                }
            }
        }

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(
                new Dimension((SQUARE_SIZE * boardWidth) + 10, (20 * boardHeight) + 10));
        layeredPane.add(new JButton());
        layeredPane.add(labelPanel, new Integer(0));
        layeredPane.add(buttonLayer, new Integer(1));
        
        timerLabel = new JLabel("000", JLabel.CENTER);
        timerLabel.setForeground(Color.RED);
        timer.stop();
        gameOver = false;
        first = true;
        currentTime = 0;
        timerLabel.setText("000");
        
        saveSettings();
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

//This creates the menubar for the frame
    public JMenuBar createMenuBar() {

        JRadioButtonMenuItem RBMIbeginner, RBMIintermediate, RBMIexpert, RBMIcustom;
        ButtonGroup BGmenuBar;
        JMenu gameMenu;
        JMenuItem MInew, MIbestTimes, MIexit;
        BGmenuBar = new ButtonGroup();

        //Game Menu
        gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        MInew = new JMenuItem("New");
        MInew.setMnemonic('N');
        MInew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        MInew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                createNewBoard();
            }
        });
        gameMenu.add(MInew);
        gameMenu.addSeparator();

        RBMIbeginner = new JRadioButtonMenuItem("Beginner");
        RBMIbeginner.setMnemonic('B');
        RBMIbeginner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setHeight(9);
                setWidth(9);
                setNumberOfMines(10);
                setType(0);
                createNewBoard();
            }
        });
        BGmenuBar.add(RBMIbeginner);
        gameMenu.add(RBMIbeginner);

        RBMIintermediate = new JRadioButtonMenuItem("Intermediate");
        RBMIintermediate.setMnemonic('I');
        RBMIintermediate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setHeight(16);
                setWidth(16);
                setNumberOfMines(40);
                setType(1);
                createNewBoard();
            }
        });
        BGmenuBar.add(RBMIintermediate);
        gameMenu.add(RBMIintermediate);

        RBMIexpert = new JRadioButtonMenuItem("Expert");
        RBMIexpert.setMnemonic('E');
        RBMIexpert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setHeight(16);
                setWidth(30);
                setNumberOfMines(99);
                setType(2);
                createNewBoard();
            }
        });
        BGmenuBar.add(RBMIexpert);
        gameMenu.add(RBMIexpert);

        RBMIcustom = new JRadioButtonMenuItem("Custom...");
        RBMIcustom.setMnemonic('C');
        RBMIcustom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                csd = new CustomDialog(frame);
                csd.setDialogFields();
                csd.setVisible(true);
            }
        });
        BGmenuBar.add(RBMIcustom);
        gameMenu.add(RBMIcustom);
        gameMenu.addSeparator();

        MIbestTimes = new JMenuItem("Best Times...");
        MIbestTimes.setMnemonic('T');
        MIbestTimes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JOptionPane.showMessageDialog(frame,
                        "Beginner : " + getBestScores()[0]
                        + "\nIntermediate: " + getBestScores()[1]
                        + "\nExpert: " + getBestScores()[2],
                        "Best Times", JOptionPane.PLAIN_MESSAGE);
            }
        });
        gameMenu.add(MIbestTimes);
        gameMenu.addSeparator();

        MIexit = new JMenuItem("Exit");
        MIexit.setMnemonic('x');
        MIexit.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        MIexit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        gameMenu.add(MIexit);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        return menuBar;
    }

    private JPanel createGameBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(timerLabel, BorderLayout.NORTH);
        panel.add(layeredPane, BorderLayout.SOUTH);
        return panel;
    }

    private void initializeGameSettings() {
        colors = new ArrayList<>(Arrays.asList(
                Color.BLUE,
                Color.GREEN,
                Color.RED,
                Color.MAGENTA,
                Color.YELLOW,
                Color.ORANGE,
                Color.DARK_GRAY,
                Color.WHITE));

        gameSettings = new HashMap<>();
        gameSettings.put("beginner", new Integer[]{9, 9, 10});
        gameSettings.put("intermediate", new Integer[]{16, 16, 40});
        gameSettings.put("expert", new Integer[]{16, 30, 99});
        gameSettings.put("custom", new Integer[]{16, 30, 40});


        readPreviousSettings();
    }

    private void readPreviousSettings() {

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

    private void initializeGame() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            heightText.setText(Integer.toString(boardHeight));
            widthText = new JTextField(2);
            widthText.setText(Integer.toString(boardWidth));
            minesText = new JTextField(3);
            minesText.setText(Integer.toString(getNumberOfMines()));

            okButton = new JButton("OK");
            okButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            });
            cancelButton = new JButton("Cancel");
            cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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

            getContentPane().add(customPanel);
            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            int h, w, n;

            if (source == okButton) {
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
                setHeight(h);
                setWidth(w);
                setNumberOfMines(n);
                //setType(3);
            }
            setVisible(false);
            createNewBoard();
        }

        public void setDialogFields() {
            //Set height, width, mine count for this dialog
            heightText.setText(Integer.toString(getHeight()));
            widthText.setText(Integer.toString(getWidth()));
            minesText.setText(Integer.toString(getNumberOfMines()));
        }
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

    //Methods for creating the GUI------------------------------------
    /**
     * Creates a {@code JFrame} to display the game and sets the frame's
     * options.
     */
    private void createAndShowFrame() {
        frame = new JFrame();
        frame.setJMenuBar(createMenuBar());
        frame.add(createGameBoard());
        frame.setTitle("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}