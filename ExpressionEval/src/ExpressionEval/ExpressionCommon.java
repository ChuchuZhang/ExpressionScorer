package ExpressionEval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ExpressionCommon {
	public static class Operator {
		String operator;
		int precedenceBinary;
		boolean isRightAssociative;
		// Null of the operator is binary
		Integer precedenceUnary;

		Operator(String operator, int precedenceBinary, boolean isRightAssociative, Integer precedenceUnary) {
			this.operator = operator;
			this.precedenceBinary = precedenceBinary;
			this.isRightAssociative = isRightAssociative;
			this.precedenceUnary = precedenceUnary;
		}
	}

	public static class Token {
		String operator;
		Double number;
		boolean isUnary;

		Token(String operator, boolean isUnary) {
			this.operator = operator;
			this.number = null;
			this.isUnary = isUnary;
		}
		
		Token(String operator) {
			this(operator, false);
		}

		Token(Double number) {
			this.number = number;
			this.operator = null;
		}
		
		boolean hasHigherPrecedence(Token other) {
			if (operator == null && other.operator == null) {
				return false;
			}
			Operator thisOp = OPERATORS.get(operator);
			Operator otherOp = OPERATORS.get(other.operator);
			if (thisOp == null || otherOp == null) {
				return false;
			}
			Integer thisPrecedence = this.isUnary? thisOp.precedenceUnary : thisOp.precedenceBinary;
			Integer otherPrecedence = other.isUnary? otherOp.precedenceUnary : otherOp.precedenceBinary;
			if (thisPrecedence == null || otherPrecedence == null) {
				throw new IllegalArgumentException("Inconsistent precedence");
			}
			if (otherOp.isRightAssociative) {
			   return thisPrecedence > otherPrecedence;
			}
			return thisPrecedence >= otherPrecedence;
			
		}
		
		boolean isOperator() {
			if (operator == null) {
				return false;
			}
			Operator thisOp = OPERATORS.get(operator);
			return thisOp != null;
		}

		@Override
		public String toString() {
			return number == null ? operator+ " " + String.valueOf(isUnary) : String.valueOf(number);
		}
		
	}

	public static final char COMMA = ',';
	public static final char DOT = '.';
	public static final char LEFT_PAREN = '(';
	public static final char RIGHT_PAREN = ')';
	public static final char LEFT_LOG_PAREN = '[';
	public static final char RIGHT_LOG_PAREN = ']';
	public static final char MULTIPLE = '*';
	public static final char POWER = '^';
	public static final char DEVIDE = '/';
	public static final char PLUS = '+';
	public static final char MINUS = '-';
	public static final String LOG = "log";
	private static final Map<String, Operator> OPERATORS = new HashMap<String, Operator>();
	static {
		OPERATORS.put(Character.toString(PLUS), new Operator(Character.toString(PLUS), 1, false, 2));
		OPERATORS.put(Character.toString(MINUS), new Operator(Character.toString(MINUS), 1, false, 2));
		OPERATORS.put(Character.toString(MULTIPLE), new Operator(Character.toString(MULTIPLE), 3, false, null));
		OPERATORS.put(Character.toString(DEVIDE), new Operator(Character.toString(DEVIDE), 3, false, null));
		OPERATORS.put(Character.toString(POWER), new Operator(Character.toString(POWER), 4, true, null));
		OPERATORS.put(LOG, new Operator(LOG, 5, false, null));
	}

	private static final Character[] binaryOpList = { '*', '/', '^' };
	private static final Character[] binaryUnaryOpList = { '+', '-' };
	public static final Set<Character> BINARY_OPERATOR = new HashSet<Character>(Arrays.asList(binaryOpList));
	public static final Set<Character> UNARY_BINARY_OPERATOR = new HashSet<Character>(Arrays.asList(binaryUnaryOpList));

	public static boolean isNumber(String str) {
		try {
			Double.valueOf(str);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
