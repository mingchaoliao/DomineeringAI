package domineering;


import java.util.Random;

import domineering.DomineeringTTEntry.NT;
import game.*;

public class AlphaBetaPlayer extends GamePlayer {
	public static final int FIRST_LAYER_DEBUG = 1;

	public long total = 0;
	public int MAX_DEPTH = -1;
	public static final double MAX_SCORE = 999999;
	boolean isHome;
	public static char homeSym;
	public static char emptySym;
	public static char awaySym;
	public static int[][][] zobristArray;
	private DomineeringTT tt;
	public static long counter = 0;

	public static void main(String [] args) {
		GamePlayer p = new AlphaBetaPlayer("liaom_Alpha_Beta"); //create player with nikename
		p.compete(args, 1);
	}

	public AlphaBetaPlayer(String n) {
		super(n, "Domineering"); //n: nikename
	}

	public GameMove getMove(GameState state, String lastMove) {
		DomineeringState board = (DomineeringState)state;
		homeSym = board.homeSym;
		awaySym = board.awaySym;
		emptySym = board.emptySym;
		isHome = state.who == GameState.Who.HOME; //HOME: true, AWAY: false
		if(isHome) {
			MAX_DEPTH = 6;
		} else {
			MAX_DEPTH = 5;
		}
		DomineeringMove[] stack = new DomineeringMove[MAX_DEPTH+1]; 
		init(stack);
		long start = System.currentTimeMillis();
		alphabeta(board.board,0,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,isHome,stack); 
		long end = System.currentTimeMillis();
		total += (end-start);
		System.out.printf("%s %3d Time: %-4d Total:%dn",stack[0],(int) stack[0].score,(end-start),total);
		return stack[0];
	}

	public void alphabeta(char[][] map, int d, double a, double b, boolean isHome, DomineeringMove[] stack) {
		double ev_tem = 0;
		ev_tem = ev2(map,isHome);
		if(d == MAX_DEPTH || Math.abs(ev_tem) == MAX_SCORE) {
			stack[d].score = ev_tem;
			return ;
		}
		
		DomineeringBoard board = new DomineeringBoard(map,isHome);
		DomineeringMove move;
		
		if(isHome) {
			double v = Double.NEGATIVE_INFINITY;
			while(board.hasNext()) {
				if(FIRST_LAYER_DEBUG==1 && d==0) System.out.println("Home: 0 Total: "+board.getSize());
				move = board.next();
				map[move.row1][move.col1] = homeSym;
				map[move.row2][move.col2] = homeSym;
				if(d+1==MAX_DEPTH) {
					stack[d+1].set(move.row1, move.col1, move.row2, move.col2);
				}
				alphabeta(map,d+1,a,b,false,stack);
				if(stack[d+1].score > v) {
					v = stack[d+1].score;
					stack[d].set(move.row1, move.col1, move.row2, move.col2, v);
				}
				map[move.row1][move.col1] = emptySym;
				map[move.row2][move.col2] = emptySym;
				a = Math.max(a, stack[d].score);
				if(stack[d].score>=b || stack[d].score== MAX_SCORE) return;
			}
			return;
		} else {
			double v = Double.POSITIVE_INFINITY;
			while(board.hasNext()) {
				if(FIRST_LAYER_DEBUG==1 && d==0) System.out.println("Away: 0 Total: "+board.getSize());
				move = board.next();
				map[move.row1][move.col1] = awaySym;
				map[move.row2][move.col2] = awaySym;
				if(d+1==MAX_DEPTH) {
					stack[d+1].set(move.row1, move.col1, move.row2, move.col2);
				}
				alphabeta(map,d+1,a,b,true,stack);
				if(stack[d+1].score<v) {
					v = stack[d+1].score;
					stack[d].set(move.row1, move.col1, move.row2, move.col2, v);
				}
				map[move.row1][move.col1] = emptySym;
				map[move.row2][move.col2] = emptySym;
				b = Math.min(b, stack[d].score);
				if(stack[d].score<=a || stack[d].score==-MAX_SCORE) return;
			}
			return;
		}
	}

	public double ev2(char[][] board, boolean isHome) {
		int realH = 0, realV = 0, safeH = 0, safeV = 0;
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[0].length; j++) {

				if((board[i][j] == emptySym) && inRange(i,j+1,board) && (board[i][j+1] == emptySym)) {
					realH++;
					if((!inRange(i-1,j,board) && !inRange(i-1,j+1,board)) || (board[i-1][j] != emptySym && board[i-1][j+1] != emptySym)) {
						if((!inRange(i+1,j,board) && !inRange(i+1,j+1,board)) || (board[i+1][j] != emptySym && board[i+1][j+1] != emptySym)) {
							safeH++;
						}
					}
				}

				if((board[i][j] == emptySym) && inRange(i+1,j,board) && (board[i+1][j] == emptySym)) {
					realV++;
					if((!inRange(i,j-1,board) && !inRange(i+1,j-1,board)) || (board[i][j-1] != emptySym && board[i+1][j-1] != emptySym)) {
						if((!inRange(i,j+1,board) && !inRange(i+1,j+1,board)) || (board[i][j+1] != emptySym && board[i+1][j+1] != emptySym)) {
							safeV++;
						}
					}
				}
			}
		}
		if(isHome) {
			if(realH == 0) return -MAX_SCORE;
			return realH-realV+safeH-safeV;
		} else {
			if(realV == 0) return MAX_SCORE;
			return realV-realH+safeV-safeH;
		}
	}

	public boolean inRange(int row, int col, char[][] board) {
		return row >= 0 && col >= 0 && row < board.length && col < board[0].length;
	}

	public void init(DomineeringMove[] stack) {
		for(int i = 0; i < stack.length; i++) {
			stack[i] = new DomineeringMove(0,0,0,0,0);
		}
	}

}
