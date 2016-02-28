package data.strategy.user.s13t245;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t245_00 extends GogoCompSub {

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t245_00(GamePlayer player) {
    super(player);
    name = "s13t245";    // プログラマが入力
  }

//--------------------------------------------------------------------
//  コンピュータの着手
//--------------------------------------------------------------------

  public synchronized GameHand calc_hand(GameState state, GameHand hand) {
    theState = state;
    theBoard = state.board;
    lastHand = hand;

    //--  置石チェック
    init_values(theState, theBoard);

    //--  評価値の計算
    calc_values(theState, theBoard);
    // 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える

    //--  着手の決定
    return deside_hand();

  }

//----------------------------------------------------------------
//  置石チェック
//----------------------------------------------------------------

  public void init_values(GameState prev, GameBoard board) {
    this.size = board.SX;
    values = new int[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (board.get_cell(i, j) != board.SPACE) {
          values[i][j] = -2;
        } else {
          if (values[i][j] == -2) {
            values[i][j] = 0;
          }
        }
      }
    }
  }

//----------------------------------------------------------------
//  評価値の計算
//----------------------------------------------------------------

  public void calc_values(GameState prev, GameBoard board) {
    int [][] cell = board.get_cell_all();  // 盤面情報
    int mycolor;                  // 自分の石の色
    int my_stone;
    int enemy_stone;
    mycolor = role;
    my_stone = get_mystone(prev);
    enemy_stone = get_enemystone(prev);

    // System.out.printf("my: %s\n", get_mystone(prev));
    // System.out.printf("enemy: %s\n", get_enemystone(prev));
    //--  各マスの評価値
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        // 三々の禁じ手は打たない → -1
        // if ( check_prohibited(cell, mycolor, i, j) ) {
        //   values[i][j] -= -1;
        //   continue;
        // }
        //--  適当な評価の例
        // 相手の五連を崩す → 1000;
        // 勝利(五連) → 900;
        if ( check_run(cell, mycolor, i, j, 5) ) {
          values[i][j] += 900;
        }
        // 敗北阻止(五連) → 800;
        if ( check_run(cell, mycolor*-1, i, j, 5) ) {
          values[i][j] += 800;
        }
        // 相手の四連を止める → 700;
        if ( check_run(cell, mycolor*-1, i, j, 4) ) {
          values[i][j] += 700;
        }
        // 自分の四連を作る → 600;
        if ( check_run(cell, mycolor, i, j, 4) ) {
          values[i][j] += 600;
        }
        // 相手の石を取る → 300;
        if ( check_rem(cell, mycolor, i, j) ) {
          values[i][j] += (my_stone >= 8) ? 20000 : 300;
        }
        // 自分の石を守る → 200;
        if ( check_rem(cell, mycolor*-1, i, j) ) {
          values[i][j] += (enemy_stone >= 8) ? 20000 : 200;
        }
        if ( values[i][j] != 0 ) { continue; }
        // 相手の三連を防ぐ → 500;
        if ( check_run(cell, mycolor*-1, i, j, 3) ) {
          values[i][j] += 500;
        }
        // 自分の三連を作る → 400;
        if ( check_run(cell, mycolor, i, j, 3) ) { values[i][j] += 400; }
        // ランダム
        if (values[i][j] == 0) {
          int aaa = (int) Math.round(Math.random() * 15);
          if (values[i][j] < aaa) { values[i][j] += aaa; }
        }
        // 四々や四三の判定
        // 飛び三や飛び四の判定
        // 三をどちらで止めるか
      }
    }
  }

//----------------------------------------------------------------
//  連の全周チェック
//----------------------------------------------------------------

  boolean check_run(int[][] board, int color, int i, int j, int len) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy, len) ) { return true; }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------

  boolean check_run_dir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    for ( int k = 1; k < len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[x][y] != color ) { return false; }
    }
    return true;
  }

//----------------------------------------------------------------
//  取の方向チェック
//----------------------------------------------------------------

  boolean check_prohibited(int[][] board, int color, int i, int j)
  {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy, 3) ) {
        }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  取の全周チェック(ダブルの判定は無し)
//----------------------------------------------------------------

  boolean check_rem(int [][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_rem_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  取の方向チェック
//----------------------------------------------------------------

  boolean check_rem_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 3;
    for ( int k = 1; k <= len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if (k == len) { color *= -1; }
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[x][y] != color ) { return false; }
    }
    return true;
  }

//----------------------------------------------------------------
//  着手の決定
//----------------------------------------------------------------

  public GameHand deside_hand() {
    GogoHand hand = new GogoHand();
    hand.set_hand(0, 0);  // 左上をデフォルトのマスとする
    int value = -1;       // 評価値のデフォルト
    //--  評価値が最大となるマス
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (value < values[i][j]) {
          hand.set_hand(i, j);
          value = values[i][j];
        }
      }
    }
    return hand;
  }

}
