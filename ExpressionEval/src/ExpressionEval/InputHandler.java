package ExpressionEval;

import java.io.*;
import java.util.*;

import ExpressionEval.ExpressionCommon;

public class InputHandler {
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

	private static List<String> readFile(String filename) throws FileNotFoundException, IOException {
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

	private static ExpressionCommon.Token parsePlusMinus(char currChar, List<ExpressionCommon.Token> currTokens) {
		boolean isUnary = false;
		if (currTokens.size() > 0) {
			ExpressionCommon.Token prev = currTokens.get(currTokens.size() - 1);
			if (prev.operator != null) {
				if (prev.operator.length() == 1
						&& (ExpressionCommon.UNARY_BINARY_OPERATOR.contains(prev.operator.charAt(0))
								|| prev.operator.charAt(0) == '(')) {
					isUnary = true;
				}
				if (prev.operator.equals(Character.toString(ExpressionCommon.PLUS))
						&& currChar == ExpressionCommon.PLUS) {
					throw new IllegalArgumentException("Cannot have two consecutive +  operator");
				}
			}
			if (currTokens.size() > 1) {
				ExpressionCommon.Token prevPrevRtn = currTokens.get(currTokens.size() - 2);
				if (prev.operator != null && prev.operator.length() == 1 && prevPrevRtn.operator != null
						&& prevPrevRtn.operator.length() == 1) {
					if (ExpressionCommon.UNARY_BINARY_OPERATOR.contains(prev.operator.charAt(0))
							&& ExpressionCommon.UNARY_BINARY_OPERATOR.contains(prevPrevRtn.operator.charAt(0))) {
						throw new IllegalArgumentException("Cannot have 3 consecutive -/+ operators");
					}
				}
			}
		} else {
			isUnary = true;
		}
		return new ExpressionCommon.Token(Character.toString(currChar), isUnary);
	}

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
			if (ExpressionCommon.BINARY_OPERATOR.contains(prevChar) || prevChar == '(' || prevChar == '['
					|| prevChar == '.') {
				throw new IllegalArgumentException("Binary operator needs two operands");
			}
		}
		return new ExpressionCommon.Token(Character.toString(currChar));
	}

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

	public static double parseNumber(char[] chars, int startIndex, int[] endIndex) {
		int currInt = 0;
		double currDecimal = 0.0;
		double currDecimalMultiplier = 0.1;
		boolean encountersDot = false;
		while (startIndex < chars.length) {
			char currChar = chars[startIndex];
			if (currChar - '0' >= 0 && currChar - '0' <= 9) {
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
		return currInt + currDecimal;
	}

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
				if (i == 0 || i == chars.length - 1) {
					throw new IllegalArgumentException("Binary operator needs two operands");
				}
				ExpressionCommon.Token token = parseBinaryOperator(currChar, rtn);
				i++;
				rtn.add(token);
			} else if (ExpressionCommon.UNARY_BINARY_OPERATOR.contains(currChar)) {
				ExpressionCommon.Token token = parsePlusMinus(currChar, rtn);
				i++;
				rtn.add(token);
			} else if (currChar == ExpressionCommon.LEFT_PAREN) {
				// Previous token is not "log". The case where previous token is log is handled
				// separately.
				parathesisStack.offerFirst(new LeftParenthesis(ExpressionCommon.LEFT_PAREN, i));
				i++;
				rtn.add(new ExpressionCommon.Token(Character.toString(currChar)));
			} else if (currChar == ExpressionCommon.RIGHT_PAREN) {
				ExpressionCommon.Token token = parseRightParenthesis(i, parathesisStack, rtn);
				rtn.add(token);
				i++;
			} else if (currChar == ExpressionCommon.COMMA) {
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
				i++;
				rtn.add(new ExpressionCommon.Token(Character.toString(ExpressionCommon.COMMA)));
			} else if (currChar - '0' >= 0 && currChar - '0' <= 9) {
				int[] endIdx = new int[1];
				double num = parseNumber(chars, i, endIdx);
				i = endIdx[0];
				rtn.add(new ExpressionCommon.Token(num));
			} else if (currChar == 'l') {
				if (i + 3 > chars.length - 1) {
					throw new IllegalArgumentException("Invalid operator");
				}
				if (chars[i + 1] != 'o' || chars[i + 2] != 'g' || chars[i + 3] != ExpressionCommon.LEFT_PAREN) {
					throw new IllegalArgumentException("Invalid operator");
				}
				parathesisStack.offerFirst(new LeftParenthesis(ExpressionCommon.LEFT_LOG_PAREN, i + 3));
				i += 4;
				rtn.add(new ExpressionCommon.Token(ExpressionCommon.LOG, false));
				rtn.add(new ExpressionCommon.Token(Character.toString(ExpressionCommon.LEFT_LOG_PAREN)));
			} else {
				throw new IllegalArgumentException("Invalid input");
			}
		}
		return rtn;
	}

	public static void main(String[] args) {
		String s = "2*0.1000003 +- 10^2 * log(1+3+4+log((1/2)*3,21), 10.3)";
		List<ExpressionCommon.Token> rtn = tokenize(s);
		System.out.println(rtn);

	}
}
