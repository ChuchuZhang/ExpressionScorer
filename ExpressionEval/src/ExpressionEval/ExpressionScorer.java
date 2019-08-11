package ExpressionEval;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.lang.Math;
public class ExpressionScorer {
	public static class ExpressionVal {
		double value;
		Node expressionTree;
		ExpressionVal(double value, Node expressionTree) {
			this.value = value;
			this.expressionTree = expressionTree;
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
	        if (right != null) {
	            right.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
	        }
	        if (left != null) {
	             left.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
	        }
	    }
	}

	public static List<ExpressionCommon.Token> toRPN(List<ExpressionCommon.Token> tokens) {
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
				if (token.operator.equals("(") || token.operator.equals("log") || token.operator.equals("[")) {
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
					// System.out.println(operatorStack);
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
	
	public static ExpressionVal evalRpn(List<ExpressionCommon.Token> rpnTokens) {
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
				if(currToken.isUnary) {
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
					switch(currToken.operator) {
						case "+":
							newVal = prevVal1 + prevVal2;
							newNode = new Node("+", prevNode1, prevNode2);
							break;
						case "-":
							newVal = prevVal1 - prevVal2;
							newNode = new Node("-", prevNode1, prevNode2);
							break;
						case "*":
							newVal = prevVal1 * prevVal2;
							newNode = new Node("*", prevNode1, prevNode2);
							break;
						case "/":
							newVal = prevVal1 / prevVal2;
							newNode = new Node("/", prevNode1, prevNode2);
							break;
						case "^":
							newVal = Math.pow(prevVal1, prevVal2);
							newNode = new Node("^", prevNode1, prevNode2);
							break;
						case "log":
							newVal = Math.log(prevVal2) / Math.log(prevVal1);
							newNode = new Node("log", prevNode1, prevNode2);
							break;
						default:
							throw new IllegalArgumentException("Unrecognizable operator");
					}	
				}
				System.out.println(newVal);
				valStack.offerFirst(newVal);
				nodeStack.offerFirst(newNode);
			}
		}
		return new ExpressionVal(valStack.pollFirst(), nodeStack.pollFirst());
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "log(3.5,2) + 1.2^(4-1/2) + 100* 3";//"log(10.5,2) + log(10.5,2) + log(10.5,2) + log(10.5,2)"; //2*0.1 -+ 10^2^4 * log(1+2*3+4+log(1/2*3,21), 10.3)";//"-10*2+(-log(2,2)^2)";
		List<ExpressionCommon.Token> tokens = InputHandler.tokenize(s);
		System.out.println(tokens);
		List<ExpressionCommon.Token> RPN = toRPN(tokens);
		System.out.println(RPN);
		ExpressionVal rst = evalRpn(RPN);
		System.out.println(rst.value);
		System.out.println(rst.expressionTree);

	}

}