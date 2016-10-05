package com.haeny.nemologicsolver;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;

/**
 * 네모네모로직을 풀이하는 클래스 * 
 * @author 정태현 *
 */
public class NemoLogicSolver {

	private int ROW_SIZE, COLUMN_SIZE;			// 전체 행/열의 크기
	private int[][] rowsHints, columnHints;		// 행/열 힌트 
	private long[] grid;
	private long[][] rowPermutations; 			// 인접행렬
	private final char EMPTY = '□', FILLED = '■';
	
	private int[][] columnValue, columnIndex;
	private long[] mask, val;
	
	/**
	 * 생성자, 행/열 힌트를 받아 초기화
	 * @param rowsHints
	 * @param columnHints
	 */
	public NemoLogicSolver(int[][] rowsHints, int[][] columnHints) {
		this.ROW_SIZE = rowsHints.length;
		this.COLUMN_SIZE = columnHints.length;
		this.rowsHints = rowsHints;
		this.columnHints = columnHints;
		this.grid = new long[ROW_SIZE];
	}

	public void process() throws Exception {
		final OutputWriter out = new OutputWriter(System.out);

		rowPermutations = new long[ROW_SIZE][];
		for (int r = 0; r < ROW_SIZE; r++) {
			LinkedList<Long> resolves = new LinkedList<Long>();
			int spaces = COLUMN_SIZE - (rowsHints[r].length - 1);
			for (int i = 0; i < rowsHints[r].length; i++) {
				spaces -= rowsHints[r][i];
			}
			calculatePerms(r, 0, spaces, 0, 0, resolves);
			if (resolves.isEmpty()) {
				throw new RuntimeException(r + "번째 행에 대하여 해답을 찾을 수 없습니다.");
			}
			rowPermutations[r] = new long[resolves.size()];
			while (!resolves.isEmpty()) {
				rowPermutations[r][resolves.size() - 1] = resolves.pollLast();
			}
		}
		
		//계산
		columnValue = new int[ROW_SIZE][COLUMN_SIZE];
		columnIndex = new int[ROW_SIZE][COLUMN_SIZE];
		mask = new long[ROW_SIZE];
		val = new long[ROW_SIZE];
		if (dfs(0)) {
			for (int r = 0; r < ROW_SIZE; r++) {
				for (int c = 0; c < COLUMN_SIZE; c++) {
					out.print((grid[r] & (1L << c)) == 0 ? EMPTY : FILLED);
				}
				out.printLine();
			}
		} else {
			out.printLine("해답을 구할 수 없습니다. GG.");
		}
		out.close();		
	}

	private boolean dfs(int row) {
		if (row == ROW_SIZE) {
			return true;
		}
		rowMask(row); //다음 행에서 유효한 마스크 계산
		for (int i = 0; i < rowPermutations[row].length; i++) {
			if ((rowPermutations[row][i] & mask[row]) != val[row]) {
				continue;
			}
			grid[row] = rowPermutations[row][i];
			updateColumns(row);
			if (dfs(row + 1)) {
				return true;
			}
		}
		return false;
	}

	private void rowMask(int row) {
		mask[row] = val[row] = 0;
		if (row == 0) {
			return;
		}
		long ixc = 1L;
		for (int c = 0; c < COLUMN_SIZE; c++, ixc <<= 1) {
			if (columnValue[row - 1][c] > 0) {
				mask[row] |= ixc;
				if (columnHints[c][columnIndex[row - 1][c]] > columnValue[row - 1][c]) {
					val[row] |= ixc; // must set
				}
			} else if (columnValue[row - 1][c] == 0 && columnIndex[row - 1][c] == columnHints[c].length) {
				mask[row] |= ixc;
			}
		}
	}

	private void updateColumns(int row) {
		long indexColumn = 1L;
		for (int c = 0; c < COLUMN_SIZE; c++, indexColumn <<= 1) {
			// 이전 값을 복사한다
			columnValue[row][c] = row == 0 ? 0 : columnValue[row - 1][c];
			columnIndex[row][c] = row == 0 ? 0 : columnIndex[row - 1][c];
			if ((grid[row] & indexColumn) == 0) {
				if (row > 0 && columnValue[row - 1][c] > 0) {
					// bit가 세팅되지 않았고 이전 컬럼이 비어있지 않으면 0으로 처리
					columnValue[row][c] = 0;
					columnIndex[row][c]++;
				}
			} else {
				columnValue[row][c]++; // 비트값 증가
			}
		}
	}

	private void calculatePerms(int r, int cur, int spaces, long perm, int shift, LinkedList<Long> res) {
		if (cur == rowsHints[r].length) {
			if ((grid[r] & perm) == grid[r]) {
				res.add(perm);
			}
			return;
		}
		while (spaces >= 0) {
			calculatePerms(r, cur + 1, spaces, perm | (bits(rowsHints[r][cur]) << shift), shift + rowsHints[r][cur] + 1, res);
			shift++;
			spaces--;
		}
	}

	private long bits(int b) {
		return (1L << b) - 1;
	}

	// 콘솔출력용
	class OutputWriter {
		private final PrintWriter writer;

		public OutputWriter(OutputStream outputStream) {
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
		}

		public OutputWriter(Writer writer) {
			this.writer = new PrintWriter(writer);
		}

		public void print(Object... objects) {
			for (int i = 0; i < objects.length; i++) {
				if (i != 0)
					writer.print(' ');
				writer.print(objects[i]);
			}
		}

		public void printLine(Object... objects) {
			print(objects);
			writer.println();
		}

		public void close() {
			writer.close();
		}
	}

}
