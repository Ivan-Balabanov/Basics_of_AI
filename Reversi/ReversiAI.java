import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ReversiAI extends JFrame {
    private static final int BOARD_SIZE = 8;
    private static final int CELL_SIZE = 70;
    
    // Board Representation: 0 = Empty, 1 = Black (Human), 2 = White (AI)
    private static final int EMPTY = 0;
    private static final int BLACK = 1; 
    private static final int WHITE = 2; 

    private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int currentPlayer = BLACK; // Human starts
    private final JLabel statusLabel = new JLabel("Your Turn (Black)", SwingConstants.CENTER);
    private final BoardPanel boardPanel = new BoardPanel();

    // Positional evaluation matrix prioritizing corners and penalizing unsafe spots
    private static final int[][] WEIGHTS = {
        {100, -20,  10,   5,   5,  10, -20, 100},
        {-20, -50,  -2,  -2,  -2,  -2, -50, -20},
        { 10,  -2,   5,   1,   1,   5,  -2,  10},
        {  5,  -2,   1,   1,   1,   1,  -2,   5},
        {  5,  -2,   1,   1,   1,   1,  -2,   5},
        { 10,  -2,   5,   1,   1,   5,  -2,  10},
        {-20, -50,  -2,  -2,  -2,  -2, -50, -20},
        {100, -20,  10,   5,   5,  10, -20, 100}
    };

    public ReversiAI() {
        setTitle("Reversi vs AI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initBoard();

        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, 40));
        add(statusLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initBoard() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = EMPTY;
            }
        }
        // Standard starting setup
        board[3][3] = WHITE;
        board[3][4] = BLACK;
        board[4][3] = BLACK;
        board[4][4] = WHITE;
    }

    private class BoardPanel extends JPanel {
        public BoardPanel() {
            setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
            setBackground(new Color(34, 139, 34)); // Forest Green

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentPlayer != BLACK) return;

                    int col = e.getX() / CELL_SIZE;
                    int row = e.getY() / CELL_SIZE;

                    if (isValidMove(board, row, col, BLACK)) {
                        makeMove(board, row, col, BLACK);
                        currentPlayer = WHITE;
                        updateGameState();
                        repaint();

                        // AI Move Execution
                        if (currentPlayer == WHITE) {
                            Timer timer = new Timer(500, ev -> {
                                aiMove();
                                updateGameState();
                                repaint();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Grid lines
            g2d.setColor(Color.BLACK);
            for (int i = 0; i <= BOARD_SIZE; i++) {
                g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
                g2d.drawLine(0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
            }

            // Draw pieces and valid move hints
            List<Point> validMoves = getValidMoves(board, currentPlayer);
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    int x = c * CELL_SIZE;
                    int y = r * CELL_SIZE;

                    if (board[r][c] == BLACK) {
                        g2d.setColor(Color.BLACK);
                        g2d.fillOval(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    } else if (board[r][c] == WHITE) {
                        g2d.setColor(Color.WHITE);
                        g2d.fillOval(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    } else if (currentPlayer == BLACK && validMoves.contains(new Point(r, c))) {
                        // Hint dots for human player
                        g2d.setColor(new Color(0, 0, 0, 80));
                        g2d.fillOval(x + CELL_SIZE / 2 - 6, y + CELL_SIZE / 2 - 6, 12, 12);
                    }
                }
            }
        }
    }

    private void aiMove() {
        List<Point> validMoves = getValidMoves(board, WHITE);
        if (validMoves.isEmpty()) {
            currentPlayer = BLACK;
            return;
        }

        Point bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        // Search depth of 6 plies provides fast and strong plays
        for (Point move : validMoves) {
            int[][] tempBoard = copyBoard(board);
            makeMove(tempBoard, move.x, move.y, WHITE);
            int value = minimax(tempBoard, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        if (bestMove != null) {
            makeMove(board, bestMove.x, bestMove.y, WHITE);
        }
        currentPlayer = BLACK;
    }

    private int minimax(int[][] currentBoard, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || isGameOver(currentBoard)) {
            return evaluateBoard(currentBoard);
        }

        int player = isMaximizing ? WHITE : BLACK;
        List<Point> validMoves = getValidMoves(currentBoard, player);

        if (validMoves.isEmpty()) {
            return minimax(currentBoard, depth - 1, alpha, beta, !isMaximizing);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Point move : validMoves) {
                int[][] tempBoard = copyBoard(currentBoard);
                makeMove(tempBoard, move.x, move.y, WHITE);
                int eval = minimax(tempBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Point move : validMoves) {
                int[][] tempBoard = copyBoard(currentBoard);
                makeMove(tempBoard, move.x, move.y, BLACK);
                int eval = minimax(tempBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluateBoard(int[][] board) {
        int score = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == WHITE) {
                    score += WEIGHTS[r][c];
                } else if (board[r][c] == BLACK) {
                    score -= WEIGHTS[r][c];
                }
            }
        }
        return score;
    }

    private void updateGameState() {
        List<Point> humanMoves = getValidMoves(board, BLACK);
        List<Point> aiMoves = getValidMoves(board, WHITE);

        if (humanMoves.isEmpty() && aiMoves.isEmpty()) {
            int blackScore = countPieces(BLACK);
            int whiteScore = countPieces(WHITE);
            String result = blackScore > whiteScore ? "You Win!" :
                           (whiteScore > blackScore ? "AI Wins!" : "It's a Tie!");
            statusLabel.setText(String.format("Game Over! %s (%d - %d)", result, blackScore, whiteScore));
        } else if (currentPlayer == BLACK && humanMoves.isEmpty()) {
            statusLabel.setText("No moves! Passing turn to AI...");
            currentPlayer = WHITE;
            Timer timer = new Timer(1000, ev -> {
                aiMove();
                updateGameState();
                boardPanel.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        } else if (currentPlayer == WHITE) {
            statusLabel.setText("AI Thinking...");
        } else {
            statusLabel.setText("Your Turn (Black)");
        }
    }

    private boolean isValidMove(int[][] b, int row, int col, int player) {
        if (b[row][col] != EMPTY) return false;
        int opponent = (player == BLACK) ? WHITE : BLACK;

        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int d = 0; d < 8; d++) {
            int r = row + dr[d];
            int c = col + dc[d];
            boolean foundOpponent = false;

            while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && b[r][c] == opponent) {
                r += dr[d];
                c += dc[d];
                foundOpponent = true;
            }

            if (foundOpponent && r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && b[r][c] == player) {
                return true;
            }
        }
        return false;
    }

    private void makeMove(int[][] b, int row, int col, int player) {
        b[row][col] = player;
        int opponent = (player == BLACK) ? WHITE : BLACK;

        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int d = 0; d < 8; d++) {
            int r = row + dr[d];
            int c = col + dc[d];
            List<Point> toFlip = new ArrayList<>();

            while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && b[r][c] == opponent) {
                toFlip.add(new Point(r, c));
                r += dr[d];
                c += dc[d];
            }

            if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && b[r][c] == player) {
                for (Point p : toFlip) {
                    b[p.x][p.y] = player;
                }
            }
        }
    }

    private List<Point> getValidMoves(int[][] b, int player) {
        List<Point> moves = new ArrayList<>();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (isValidMove(b, r, c, player)) {
                    moves.add(new Point(r, c));
                }
            }
        }
        return moves;
    }

    private boolean isGameOver(int[][] b) {
        return getValidMoves(b, BLACK).isEmpty() && getValidMoves(b, WHITE).isEmpty();
    }

    private int countPieces(int player) {
        int count = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == player) count++;
            }
        }
        return count;
    }

    private int[][] copyBoard(int[][] original) {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReversiAI().setVisible(true));
    }
}