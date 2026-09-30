# Java Reversi

Java Swing Reversi game with a Min-Maxing AI engine.

## Features

* **Algorithm**: Uses a 6-ply Minimax decision algorithm with Alpha-Beta Pruning and a positional weight matrix.
Built entirely using native Java Swing utilities (`javax.swing`).


## AI Algorithm Details

The AI evaluates board states using the **Min-max** algorithm, enhanced with **Alpha-Beta Pruning** and a strategic **Positional Weight Matrix** (outsourced).

### 1. Minimax Search (6-Ply Depth)
The AI looks 6 moves ahead (3 full turns for each player). It generates a decision tree where:
* **Maximizing steps (AI)** select moves that maximize the state score.
* **Minimizing steps (Human)** assume the opponent plays optimally to reduce the AI's score.

### 2. Alpha-Beta Pruning
The search tree is dynamically pruned. Determines a branch cannot produce a better result than an already evaluated alternative (Beta <= Alpha ), exploration of that branch is terminated.

### 3. Positional Evaluation Matrix*
Leaf nodes are scored by evaluating piece placements against an 8x8 weight matrix:
* **Corners (+100)**: Highly prioritized because captured corner pieces are permanent (cannot be flipped) and act as stable strategic anchors.
* **X-Squares (-50) & C-Squares (-20)**: Heavily penalized because placing a piece diagonally or adjacent to an open corner allows the opponent to capture that corner on their next turn.
* **Edges (+10 to +5)**: Rewarded for perimeter control and stability.

---

File features both source code and a compiled .class (java needed to play). 

*- Consulted AI (Gemini) for clarification.