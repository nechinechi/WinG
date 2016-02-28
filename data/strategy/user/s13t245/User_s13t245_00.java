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
    // GameState testState = state;

    //--  置石チェック
    init_values(theState, theBoard);

    //--  評価値の計算
    calc_values(theState, theBoard);
    // 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える

    //--  着手の決定
    return deside_hand();

    // return seeBeyond(theState);
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
    my_stone = get_mystone(prev) / 2;
    enemy_stone = get_enemystone(prev) / 2;

    //--  各マスの評価値
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        int my_len = check_run(cell, mycolor, i, j);
        int enemy_len = check_run(cell, mycolor*-1, i, j);
        int my_rem = check_rem(cell, mycolor, i, j);
        int enemy_rem = check_rem(cell, mycolor*-1, i, j);
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        // 三々の禁じ手は打たない → -1
        if ( check_prohibited(cell, mycolor, i, j) ) {
          values[i][j] = -1;
          continue;
        }
        //--  適当な評価の例
        // 相手の五連を崩す → 1000;
        // 勝利(五連) → 900;
        if ( my_len == 5 ) {
          values[i][j] += 20000;
        }
        // 敗北阻止(五連) → 800;
        if ( enemy_len == 5 ) {
          values[i][j] += 20000;
        }
        // 相手の四連を止める → 700;
        if ( enemy_len == 4 ) {
          values[i][j] += 29000;
        }
        // 自分の四連を作る → 600;
        if ( my_len == 4 ) {
          values[i][j] += 19000;
        }
        // 相手の石を取る → 300;
        if ( enemy_rem != 0 ) {
          // values[i][j] += ( my_stone >= 4 ) ? 20000 : 300;
          switch ( my_stone + enemy_rem ) {
            case 5: values[i][j] += 20000; break;
            case 4: values[i][j] += 15000; break;
            default : values[i][j] += 300;
          }
        }
        // 自分の石を守る → 200;
        if ( my_rem != 0 ) {
          // values[i][j] += (enemy_stone >= 4) ? 40000 : 200;
          switch ( enemy_stone + my_rem ) {
            case 5: values[i][j] += 40000; break;
            case 4: values[i][j] += 15000; break;
            default : values[i][j] += 200;
          }
        }
        // 相手の三連を防ぐ → 500;
        if ( enemy_len == 3 ) {
          values[i][j] += 500;
        }
        // 自分の三連を作る → 400;
        if ( my_len == 3 ) { values[i][j] += 400; }
        // ランダム
        if (values[i][j] == 0) {
          int aaa = (int) Math.round(Math.random() * 15);
          if (values[i][j] < aaa) { values[i][j] += aaa; }
        }
        // 三をどちらで止めるか
      }
    }
  }

//----------------------------------------------------------------
//  連の全周チェック
//----------------------------------------------------------------

  int check_run(int[][] board, int color, int i, int j) {
    // for ( int dx = -1; dx <= 1; dx++ ) {
    //   for ( int dy = -1; dy <= 1; dy++ ) {
    //     if ( dx == 0 && dy == 0 ) { continue; }
    //     if ( check_run_dir(board, color, i, j, dx, dy, len) ) { return true; }
    //   }
    // }
    // return false;
    int[] count = new int[4];
    count[0] = check_run_dir(board, color, i, j, 0, 1);   // 横
    count[1] = check_run_dir(board, color, i, j, 1, 0);   // 縦
    count[2] = check_run_dir(board, color, i, j, 1, 1);   // 傾き 1
    count[3] = check_run_dir(board, color, i, j, 1, -1);  // 傾き-1
    int max_index = 0;
    for ( int k = 1; k < count.length; k++ ) {
      if ( count[max_index] < count[k] ) { max_index = k; }
    }

    return count[max_index];
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------

  int check_run_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    // for ( int k = 1; k < len; k++ ) {
    //   int x = i+k*dx;
    //   int y = j+k*dy;
    //   if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
    //   if ( board[x][y] != color ) { return false; }
    // }
    // return true;
    int count = 1;
    int p, q;
    p = i-dx; q = j-dy;
    if ( check_range(p, q) ) {
      while ( board[p][q] == color ) {
        count++;
        p -= dx;  q -= dy;
        if ( !check_range(p, q) ) { break; }
      }
    }
    p = i+dx; q = j+dy;
    if ( check_range(p, q) ) {
      while ( board[p][q] == color ) {
        count++;
        p += dx;  q += dy;
        if ( !check_range(p, q) ) { break; }
      }
    }

    return count;
  }

//----------------------------------------------------------------
//  禁じ手チェック
//----------------------------------------------------------------

  boolean check_prohibited(int[][] board, int color, int i, int j)
  {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy) == 3 ) {
          if ( check_three_len(board, color, i+dx, j+dy) ) { return true; }
          if ( check_three_len(board, color, i+2*dx, j+2*dy) ) { return true; }
        }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  禁じ手チェック
//----------------------------------------------------------------

  boolean check_three_len(int[][] board, int color, int i, int j)
  {
    return check_run_dir(board, color, i, j, 1, 0) == 3         // 横
             || check_run_dir(board, color, i, j, 0, 1) == 3    // 縦
             || check_run_dir(board, color, i, j, 1, 1) == 3    // 傾き 1
             || check_run_dir(board, color, i, j, 1, -1) == 3;  // 傾き-1
  }

//----------------------------------------------------------------
//  取の全周チェック(ダブルの判定は無し)
//----------------------------------------------------------------

  int check_rem(int [][] board, int color, int i, int j) {
    int count = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_rem_dir(board, color, i, j, dx, dy) ) { count++; }
      }
    }
    return count;
  }

//----------------------------------------------------------------
//  取の方向チェック
//----------------------------------------------------------------

  boolean check_rem_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 3;
    for ( int k = 1; k <= len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( k == len ) { color *= -1; }
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[x][y] != color ) { return false; }
    }
    return true;
  }

  boolean check_range(int i, int j)
  {
    return ( i >= 0 && j >= 0 && i < size && j < size );
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

//----------------------------------------------------------------
//  着手の決定
//----------------------------------------------------------------
/*
  public GameHand seeBeyond(GameState state) {
    GameState testState = state;
    theBoard = state.board;
    for ( int i = 0; i < 3; i++ ) {
      //--  置石チェック
      init_values(testState, theBoard);

      //--  評価値の計算
      calc_values(testState, theBoard);
      // 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える

      //--  着手の決定
      GameHand testHand = deside_hand();

      // testState = test_hand(testHand);
    }
  }*/

}
