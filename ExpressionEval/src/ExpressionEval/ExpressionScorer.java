package ExpressionEval;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.lang.Math;

/**
 * This class provides the util functions to evaluate expression values and
 * build the binary expression tree.
 *
 */
public class ExpressionScorer {
	// This map caches the calculated value for *, /, ^ and log operations.
	private static Map<String, Double> calculatedExpressionToValueCache = new HashMap<String, Double>();

	public static class ExpressionVal {
		double value;
		Node expressionTree;
		Exception exp;

		ExpressionVal(double value, Node expressionTree) {
			this.value = value;
			this.expressionTree = expressionTree;
		}

		ExpressionVal(Exception exp) {
			this.exp = exp;
		}
	}

	public static class Node {
		String val;
		Node left;
		Node right;

		Node(String val) {
			this.val = val;
		}

		Node(String val, Node left, Node right) {
			this.val = val;
			this.left = left;
			this.right = right;
		}

		public String toString() {
			StringBuilder buffer = new StringBuilder(50);
			print(buffer, "", "");
			return buffer.toString();
		}

		private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
			buffer.append(prefix);
			buffer.append(val);
			buffer.append('\n');
			if (right != null && left != null) {
				right.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
			}
			if (right != null && left == null) {
				right.print(buffer, childrenPrefix + "├── ", childrenPrefix + "    ");
			}
			if (left != null) {
				left.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
			}
		}
	}

	/**
	 * Evaluate the values and build binary expression tree given the expression
	 * tokens.
	 * 
	 * @param tokens
	 *            Token parsed by the InputHandler
	 * @return Value and binary expression tree of the tokens wrapped in the
	 *         expressionVal data struct.
	 */
	public static ExpressionVal evalTokens(List<ExpressionCommon.Token> tokens) {
		List<ExpressionCommon.Token> rpnTokens = toRPN(tokens);
		return evalRpn(rpnTokens);
	}

	/**
	 * Helper function to convert the expressions to the reverse polish notations.
	 * 
	 * @param tokens
	 *            Token parsed by the InputHandler.
	 * @return Tokens in the reverse polish notations.
	 */
	private static List<ExpressionCommon.Token> toRPN(List<ExpressionCommon.Token> tokens) {
		List<ExpressionCommon.Token> rtn = new ArrayList<ExpressionCommon.Token>();
		Deque<ExpressionCommon.Token> operatorStack = new ArrayDeque<ExpressionCommon.Token>();
		for (int i = 0; i < tokens.size(); i++) {
			ExpressionCommon.Token token = tokens.get(i);
			if ((token.number == null && token.operator == null) || (token.number != null && token.operator != null)) {
				throw new IllegalArgumentException("Logic error. Token can either be number or operator but not both");
			}
			if (token.number != null) {
				rtn.add(token);
			} else {
				if (token.operator.equals("(") || token.operator.equals("log") || token.operator.equals("[")
						|| token.isUnary) {
					operatorStack.offerFirst(token);
				} else if (token.operator.equals(")")) {
					boolean findLeft = false;
					while (!operatorStack.isEmpty()) {
						ExpressionCommon.Token lastOperator = operatorStack.pollFirst();
						if (lastOperator.operator.equals("(")) {
							findLeft = true;
							break;
						} else {
							rtn.add(lastOperator);
						}
					}
					if (!findLeft) {
						throw new IllegalArgumentException("Invalid parenthesis");
					}
				} else if (token.operator.equals(",")) {
					// Make sure all the operator before the last ] is pushed to the output
					while (!operatorStack.isEmpty() && !operatorStack.peekFirst().operator.equals("[")) {
						rtn.add(operatorStack.pollFirst());
					}
				} else if (token.operator.equals("]")) {
					while (!operatorStack.isEmpty()) {
						ExpressionCommon.Token lastOperator = operatorStack.pollFirst();
						if (lastOperator.operator.equals("[")) {
							break;
						} else {
							rtn.add(lastOperator);
						}
					}
					if (operatorStack.isEmpty()) {
						throw new IllegalArgumentException("Invalid LOG");
					}
					ExpressionCommon.Token logOperator = operatorStack.pollFirst();
					if (!logOperator.operator.equals("log")) {
						throw new IllegalArgumentException("Invalid LOG");
					}
					rtn.add(logOperator);
				} else {
					while (!operatorStack.isEmpty() && operatorStack.peekFirst().isOperator()) {
						ExpressionCommon.Token lastOperator = operatorStack.peekFirst();
						boolean prevShouldExecuteFirst = lastOperator.hasHigherPrecedence(token);
						if (prevShouldExecuteFirst) {
							operatorStack.pollFirst();
							rtn.add(lastOperator);
						} else {
							break;
						}
					}
					operatorStack.offerFirst(token);
				}
			}
		}
		while (!operatorStack.isEmpty()) {
			rtn.add(operatorStack.pollFirst());
		}
		return rtn;
	}

	/**
	 * Helper function to evaluate the values of the reverse polish notations.
	 * 
	 * @param tokens
	 *            Tokens in the reverse polish notations.
	 * @return value and binary expression tree of the tokens.
	 */
	private static ExpressionVal evalRpn(List<ExpressionCommon.Token> rpnTokens) {
		Deque<Node> nodeStack = new ArrayDeque<Node>();
		Deque<Double> valStack = new ArrayDeque<Double>();
		for (int i = 0; i < rpnTokens.size(); i++) {
			ExpressionCommon.Token currToken = rpnTokens.get(i);
			if (currToken.number != null) {
				valStack.offerFirst(currToken.number);
				nodeStack.offerFirst(new Node(String.valueOf(currToken.number)));
			} else if (currToken.operator == null) {
				throw new IllegalArgumentException("Invalid token. Both number and operator are null");
			} else {
				// Curr Token is an operator
				Node newNode;
				Double newVal;
				if (currToken.isUnary) {
					Double prevVal = valStack.pollFirst();
					Node prevNode = nodeStack.pollFirst();
					if (currToken.operator.equals("+")) {
						newNode = new Node("+", null, prevNode);
						newVal = prevVal;
					} else if (currToken.operator.equals("-")) {
						newVal = -1 * prevVal;
						newNode = new Node("-", null, prevNode);
					} else {
						throw new IllegalArgumentException("Unary operator should be either + or -");
					}
				} else {
					if (nodeStack.size() < 2 || valStack.size() < 2) {
						throw new IllegalArgumentException("Logic error. Binary operators should have two operands");
					}
					Double prevVal2 = valStack.pollFirst();
					Node prevNode2 = nodeStack.pollFirst();
					Double prevVal1 = valStack.pollFirst();
					Node prevNode1 = nodeStack.pollFirst();
					switch (currToken.operator) {
					case "+":
						newVal = prevVal1 + prevVal2;
						newNode = new Node("+", prevNode1, prevNode2);
						break;
					case "-":
						newVal = prevVal1 - prevVal2;
						newNode = new Node("-", prevNode1, prevNode2);
						break;
					case "*":
						// Check if the operation has been calculated. If not, calculate and push it
						// into the cache.
						String operationInString = String.format("%f * %f", prevVal1, prevVal2);
						String operationInStringReverse = String.format("%f * %f", prevVal2, prevVal1);
						Double val = calculatedExpressionToValueCache.get(operationInString);
						if (val == null) {
							val = calculatedExpressionToValueCache.get(operationInStringReverse);
							if (val == null) {
								newVal = prevVal1 * prevVal2;
								calculatedExpressionToValueCache.put(operationInString, newVal);
								calculatedExpressionToValueCache.put(operationInStringReverse, newVal);
							} else {
								newVal = val;
							}
						} else {
							newVal = val;
						}
						newNode = new Node("*", prevNode1, prevNode2);
						break;
					case "/":
						// Check if the operation has been calculated. If not, calculate and push it
						// into the cache.
						String operationInStr = String.format("%f / %f", prevVal1, prevVal2);
						Double oldVal = calculatedExpressionToValueCache.get(operationInStr);
						if (oldVal == null) {
							newVal = prevVal1 / prevVal2;
							calculatedExpressionToValueCache.put(operationInStr, newVal);
						} else {
							newVal = oldVal;
						}
						newNode = new Node("/", prevNode1, prevNode2);
						break;
					case "^":
						// Check if the operation has been calculated. If not, calculate and push it
						// into the cache.
						String opInStr = String.format("%f ^ %f", prevVal1, prevVal2);
						Double oldValPower = calculatedExpressionToValueCache.get(opInStr);
						if (oldValPower == null) {
							newVal = Math.pow(prevVal1, prevVal2);
							calculatedExpressionToValueCache.put(opInStr, newVal);
						} else {
							newVal = oldValPower;
						}
						newNode = new Node("^", prevNode1, prevNode2);
						break;
					case "log":
						// Check if the operation has been calculated. If not, calculate and push it
						// into the cache.
						String opInStrLog = String.format("log(%f,%f)", prevVal1, prevVal2);
						Double oldValLog = calculatedExpressionToValueCache.get(opInStrLog);
						if (oldValLog == null) {
							newVal = Math.log(prevVal2) / Math.log(prevVal1);
							calculatedExpressionToValueCache.put(opInStrLog, newVal);
						} else {
							newVal = oldValLog;
						}
						newNode = new Node("log", prevNode1, prevNode2);
						break;
					default:
						throw new IllegalArgumentException("Unrecognizable operator");
					}
				}
				valStack.offerFirst(newVal);
				nodeStack.offerFirst(newNode);
			}
		}
		return new ExpressionVal(valStack.pollFirst(), nodeStack.pollFirst());
	}
}
