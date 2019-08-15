package ExpressionEval;

import java.io.*;
import java.util.*;

import ExpressionEval.ExpressionCommon;

/**
 * This class provide util functions to read expressions from file given a
 * filename, and tokenize and validate each expression.
 *
 */
public class InputHandler {
	/**
	 * Helper class for parsing left parenthesis. If the left parenthesis belongs to
	 * a log operator, |parenthesis| should be set to '[', otherwise '('.
	 * 
	 * For log parenthesis, |hasComma| indicates whether the log has met a matching
	 * comma in the expression yet.
	 * 
	 * |idx| represents the index of the parenthesis in the original expression
	 * string.
	 *
	 */
	private static class LeftParenthesis {
		char parenthesis;
		boolean hasComma;
		int idx;

		LeftParenthesis(char parenthesis, int idx) {
			this.parenthesis = parenthesis;
			this.hasComma = false;
			this.idx = idx;
		}
	}

	/**
	 * Given a filename, read the file line by line and put each line into a list of
	 * string.
	 * 
	 * @param filename
	 *            Absolute path of the file on disk.
	 * @return Lines of the file content in a list.
	 */
	public static List<String> readFile(String filename) throws FileNotFoundException, IOException {
		List<String> rtn = new ArrayList<String>();
		// Open the file
		try {
			FileInputStream fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				rtn.add(strLine);
			}
			// Close the input stream
			fstream.close();
		} catch (Exception ex) {
			throw ex;
		}
		return rtn;
	}

	/**
	 * Given a math expression, tokenize and validate the expression. NOTE: The left
	 * and right parenthesis for log operator would be replaced with [ and ].
	 * 
	 * @param formula
	 *            Expression in string.
	 * @return A list of the parsed token.
	 */
	public static List<ExpressionCommon.Token> tokenize(String formula) {
		if (formula == null) {
			throw new IllegalArgumentException("Formula is null");
		}
		List<ExpressionCommon.Token> rtn = new ArrayList<ExpressionCommon.Token>();
		char[] chars = formula.toCharArray();
		Deque<LeftParenthesis> parathesisStack = new ArrayDeque<LeftParenthesis>();
		int i = 0;
		while (i < chars.length) {
			char currChar = chars[i];
			if (currChar == ' ') {
				i++;
				continue;
			}
			if (ExpressionCommon.BINARY_OPERATOR.contains(currChar)) {
				// This case handles operators (*, /, ^)
				if (i == 0 || i == chars.length - 1) {
					throw new IllegalArgumentException("Binary operator needs two operands");
				}
				ExpressionCommon.Token token = parseBinaryOperator(currChar, rtn);
				i++;
				rtn.add(token);
			} else if (ExpressionCommon.UNARY_BINARY_OPERATOR.contains(currChar)) {
				// This case handles operators could be binary and unary (+, -)
				ExpressionCommon.Token token = parsePlusMinus(currChar, rtn);
				i++;
				rtn.add(token);
			} else if (currChar == ExpressionCommon.LEFT_PAREN) {
				// This case handles the parenthesis not belonging to "log" operator.
				ExpressionCommon.Token leftPren = parseLeftParenthesis(i, currChar, parathesisStack, rtn);
				i++;
				rtn.add(leftPren);
			} else if (currChar == ExpressionCommon.RIGHT_PAREN) {
				// This case handles )
				ExpressionCommon.Token token = parseRightParenthesis(i, parathesisStack, rtn);
				i++;
				rtn.add(token);
			} else if (currChar == ExpressionCommon.COMMA) {
				// This case handles ,
				ExpressionCommon.Token token = parseComma(parathesisStack);
				i++;
				rtn.add(token);
			} else if (isDigit(currChar)) {
				// This case handles number
				int[] endIdx = new int[1];
				ExpressionCommon.Token num = parseNumber(chars, i, endIdx);
				i = endIdx[0];
				rtn.add(num);
			} else if (currChar == 'l') {
				// This case handles log
				if (i + 3 > chars.length - 1) {
					throw new IllegalArgumentException("Invalid operator");
				}
				if (chars[i + 1] != 'o' || chars[i + 2] != 'g' || chars[i + 3] != ExpressionCommon.LEFT_PAREN) {
					throw new IllegalArgumentException("Invalid operator");
				}
				parathesisStack.offerFirst(new LeftParenthesis(ExpressionCommon.LEFT_LOG_PAREN, i + 3));
				i += 4;
				rtn.add(new ExpressionCommon.Token(ExpressionCommon.LOG));
				rtn.add(new ExpressionCommon.Token(Character.toString(ExpressionCommon.LEFT_LOG_PAREN)));
			} else {
				throw new IllegalArgumentException("Invalid input");
			}
		}
		if (!parathesisStack.isEmpty()) {
			throw new IllegalArgumentException("Unblanaced parenthesis");
		}
		return rtn;
	}

	/**
	 * Helper to parse operator could be both unary and binary +, -. If there are
	 * two consecutive unary operators, exception will be thrown.
	 * 
	 * @param currChar
	 *            The char needs to be parsed.
	 * @param currTokens
	 *            All the tokens parsed before currChar.
	 * @return Parsed token.
	 */
	private static ExpressionCommon.Token parsePlusMinus(char currChar, List<ExpressionCommon.Token> currTokens) {
		boolean isUnary = false;
		if (currTokens.size() > 0) {
			ExpressionCommon.Token prev = currTokens.get(currTokens.size() - 1);
			if (prev.operator != null) {
				if (prev.operator.length() == 1
						&& (ExpressionCommon.UNARY_BINARY_OPERATOR.contains(prev.operator.charAt(0))
								|| ExpressionCommon.BINARY_OPERATOR.contains(prev.operator.charAt(0))
								|| prev.operator.charAt(0) == ExpressionCommon.LEFT_PAREN
								|| prev.operator.charAt(0) == ExpressionCommon.LEFT_LOG_PAREN)) {
					isUnary = true;
				}
				if (prev.isUnary && isUnary) {
					throw new IllegalArgumentException("Cannot have two consecutive unary operator");
				}
			}
		} else {
			isUnary = true;
		}
		return new ExpressionCommon.Token(Character.toString(currChar), isUnary);
	}

	/**
	 * Helper to parse operator /, *, ^. If the operator does not have two operands,
	 * this function would throw IllegalArgumentException.
	 * 
	 * @param currChar
	 *            The char needs to be parsed.
	 * @param currTokens
	 *            All the tokens parsed before currChar.
	 * @return Parsed token.
	 */
	private static ExpressionCommon.Token parseBinaryOperator(char currChar, List<ExpressionCommon.Token> currTokens) {
		if (currTokens.size() == 0) {
			throw new IllegalArgumentException("Binary operator needs two operands");
		}
		ExpressionCommon.Token prev = currTokens.get(currTokens.size() - 1);
		if (prev.operator != null) {
			if (prev.operator.length() != 1) {
				throw new IllegalArgumentException("Binary operator needs two operands");
			}
			char prevChar = prev.operator.charAt(0);
			if (ExpressionCommon.UNARY_BINARY_OPERATOR.contains(prevChar)
					|| ExpressionCommon.BINARY_OPERATOR.contains(prevChar) || prevChar == ExpressionCommon.LEFT_PAREN
					|| prevChar == ExpressionCommon.LEFT_LOG_PAREN || prevChar == ExpressionCommon.DOT) {
				throw new IllegalArgumentException("Binary operator needs two operands");
			}
		}
		return new ExpressionCommon.Token(Character.toString(currChar));
	}

	/**
	 * Helper to parse (. If there are implicit multiply (i.e. 2(3+4), (3+4)(5+6)),
	 * this function would throw IllegalArgumentException.
	 * 
	 * @param startIdx
	 *            The index of currChar in the original expression.
	 * @param currChar
	 *            The char needs to be parsed.
	 * @param parathesisStack
	 *            The stack keeps track of the current opening parenthesis.
	 * @param currTokens
	 *            All the tokens parsed before currChar.
	 * @return Parsed token.
	 */
	private static ExpressionCommon.Token parseLeftParenthesis(int startIdx, char currChar,
			Deque<LeftParenthesis> parathesisStack, List<ExpressionCommon.Token> currTokens) {
		if (currTokens.size() > 0) {
			ExpressionCommon.Token lastToken = currTokens.get(currTokens.size() - 1);
			if (lastToken.number != null) {
				throw new IllegalArgumentException("Does not support implicit multiply. Please add * explicitly");
			}
			if (lastToken.operator != null && lastToken.operator.equals(")")) {
				throw new IllegalArgumentException("Does not support implicit multiply. Please add * explicitly");
			}
		}
		parathesisStack.offerFirst(new LeftParenthesis(ExpressionCommon.LEFT_PAREN, startIdx));
		return new ExpressionCommon.Token(Character.toString(currChar));
	}

	/**
	 * Helper to parse ). If there are binary operator right before the right
	 * parenthesis, this function would throw IllegalArgumentException.
	 * 
	 * @param startIdx
	 *            The index of currChar in the original expression.
	 * @param parathesisStack
	 *            The stack keeps track of the current opening parenthesis.
	 * @param currTokens
	 *            All the tokens parsed before currChar.
	 * @return Parsed token.
	 */
	private static ExpressionCommon.Token parseRightParenthesis(int startIdx, Deque<LeftParenthesis> parathesisStack,
			List<ExpressionCommon.Token> currTokens) {
		if (parathesisStack.isEmpty()) {
			throw new IllegalArgumentException("Invalid parenthesis");
		}
		ExpressionCommon.Token prev = currTokens.get(currTokens.size() - 1);
		if (prev.operator != null) {
			if (prev.operator.length() == 1) {
				char prevChar = prev.operator.charAt(0);
				if (ExpressionCommon.BINARY_OPERATOR.contains(prevChar)
						|| ExpressionCommon.UNARY_BINARY_OPERATOR.contains(prevChar)) {
					throw new IllegalArgumentException("Invalid operators before )");
				}
			}
		}

		ExpressionCommon.Token token;
		LeftParenthesis left = parathesisStack.peekFirst();
		if (left.idx == startIdx - 1) {
			throw new IllegalArgumentException("Invalid parenthesis");
		}
		if (left.parenthesis == ExpressionCommon.LEFT_LOG_PAREN) {
			if (!left.hasComma) {
				throw new IllegalArgumentException("Invalid log statement");
			} else {
				parathesisStack.pollFirst();
				token = new ExpressionCommon.Token(Character.toString(ExpressionCommon.RIGHT_LOG_PAREN));
			}
		} else {
			// left is '('
			parathesisStack.pollFirst();
			token = new ExpressionCommon.Token(Character.toString(ExpressionCommon.RIGHT_PAREN));
		}
		return token;
	}

	/**
	 * Helper to parse ,. If there are no matching log operator or the log operator
	 * already has a comma, this function would throw IllegalArgumentException.
	 * 
	 * @param parathesisStack
	 *            The stack keeps track of the current opening parenthesis.
	 * @return Parsed token.
	 */
	public static ExpressionCommon.Token parseComma(Deque<LeftParenthesis> parathesisStack) {
		if (parathesisStack.isEmpty()) {
			throw new IllegalArgumentException("Invalid comma for the log statement");
		}
		LeftParenthesis left = parathesisStack.peekFirst();
		if (left.parenthesis == ExpressionCommon.LEFT_PAREN) {
			throw new IllegalArgumentException("Invalid comma for the log statement");
		}
		if (left.hasComma) {
			throw new IllegalArgumentException("Invalid comma for the log statement");
		}
		// Left parenthesis is '['
		left.hasComma = true;
		return new ExpressionCommon.Token(Character.toString(ExpressionCommon.COMMA));
	}

	/**
	 * Helper to parse number.
	 * 
	 * @param chars
	 *            Charr array of the original expression.
	 * @param startIndex
	 *            The start index of the number.
	 * @param endIndex
	 *            An array with size = 1 to store the (end index of the number + 1).
	 * @return Parsed number in double and the end index would be stored in the
	 *         array |endIndex| passed in.
	 */
	public static ExpressionCommon.Token parseNumber(char[] chars, int startIndex, int[] endIndex) {
		int currInt = 0;
		double currDecimal = 0.0;
		double currDecimalMultiplier = 0.1;
		boolean encountersDot = false;
		while (startIndex < chars.length) {
			char currChar = chars[startIndex];
			if (isDigit(currChar)) {
				int currNum = currChar - '0';
				if (encountersDot) {
					currDecimal += currDecimalMultiplier * currNum;
					currDecimalMultiplier /= 10.0;
				} else {
					currInt = currInt * 10 + currNum;
				}
			} else if (currChar == ExpressionCommon.DOT) {
				if (encountersDot) {
					throw new IllegalArgumentException("Invalid floating point number");
				}
				encountersDot = true;
			} else {
				// Not number, not dot.
				break;
			}
			startIndex++;
		}
		endIndex[0] = startIndex;
		return new ExpressionCommon.Token(currInt + currDecimal);
	}

	/**
	 * Helper to check if a character is a digit.
	 * 
	 * @param currChar
	 *            Character to be evaluated.
	 * @return whether the character passed in is a digit.
	 */
	private static boolean isDigit(char currChar) {
		return currChar - '0' >= 0 && currChar - '0' <= 9;
	}
}
