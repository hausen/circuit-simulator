    // info about each row/column of the matrix for simplification purposes
    class RowInfo {
	static final int ROW_NORMAL = 0;  // ordinary value
	static final int ROW_CONST  = 1;  // value is constant
	static final int ROW_EQUAL  = 2;  // value is equal to another value
	int nodeEq, type, mapCol, mapRow;
	double value;
	boolean rsChanges; // row's right side changes
	boolean lsChanges; // row's left side changes
	boolean dropRow;   // row is not needed in matrix
	RowInfo() { type = ROW_NORMAL; }
    }
