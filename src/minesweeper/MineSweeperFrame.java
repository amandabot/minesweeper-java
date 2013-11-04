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
import java.awt.event.MouseListener;
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


public class MineSweeperFrame extends JFrame implements MouseListener,
        ActionListener {

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
    CustomDialog csd;
    Timer timer;
    JLabel timerLabel;
    Point clickLocation;

    public MineSweeperFrame() {
        //Frame Properties
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Minesweeper");
        setResizable(false);

        //Create mine field1 creator
        ms = new Minesweeper();

        //Create MenuBar
        menuBar = new JMenuBar();
        createMenuBar();
        setJMenuBar(menuBar);

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
        csd = new CustomDialog(this);
    }

    public void createNewBoard() {
        int a, b;

        timer.stop();
        gameOver = false;
        first = true;
        currentTime = 0;
        timerLabel.setText("000");
        getContentPane().removeAll();

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

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(timerLabel, BorderLayout.NORTH);
        getContentPane().add(layeredPane, BorderLayout.SOUTH);
        pack();
        setVisible(true);
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
            JOptionPane.showMessageDialog(this,
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
