package ExpressionEval;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

class ExpressionScorerTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	void testInvalid() {
		exception.expect(Exception.class);
		String formula = "-10*(2";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula);
		});
		
		String formula0 = "-10+*2";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula0);
		});

		String formula1 = "-10*()";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula1);
		});

		String formula2 = "-10*log()";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula2);
		});

		String formula3 = "-10*(+)";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula3);
		});

		String formula4 = "-10*";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula4);
		});

		String formula5 = "log(1,4 +1234";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula5);
		});

		String formula6 = "log(1,4, 1234)";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula6);
		});

		String formula7 = "log(1,4, 1234..4)";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula7);
		});

		String formula8 = "++log(1,4, 1234..4)";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula8);
		});

		String formula9 = "--log(1,4, 1234..4)";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula9);
		});
		
		String formula10 = "2(10)";
		assertThrows(IllegalArgumentException.class, () -> {
			InputHandler.tokenize(formula10);
		});
	}

	@Test
	void testBinaryOperator() {
		String formula = "2*2^2^3";
		List<ExpressionCommon.Token> tokens = InputHandler.tokenize(formula);
		ExpressionScorer.ExpressionVal result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == 512);
		assertTrue(result.expressionTree.val.equals("*"));
		assertTrue(result.expressionTree.left.val.equals("2.0"));
		assertTrue(result.expressionTree.right.val.equals("^"));
		assertTrue(result.expressionTree.right.left.val.equals("2.0"));
		assertTrue(result.expressionTree.right.right.val.equals("^"));
		assertTrue(result.expressionTree.right.right.left.val.equals("2.0"));
		assertTrue(result.expressionTree.right.right.right.val.equals("3.0"));

		formula = "2*(2^2)^3";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == 128);
		assertTrue(result.expressionTree.val.equals("*"));
		assertTrue(result.expressionTree.left.val.equals("2.0"));
		assertTrue(result.expressionTree.right.val.equals("^"));
		assertTrue(result.expressionTree.right.right.val.equals("3.0"));
		assertTrue(result.expressionTree.right.left.val.equals("^"));
		assertTrue(result.expressionTree.right.left.left.val.equals("2.0"));
		assertTrue(result.expressionTree.right.left.right.val.equals("2.0"));

		formula = "2*(log(2, (2+2)))^2 + 4^4.5/2";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == 264);
	}

	@Test
	void testUnaryOperators() {
		String formula = "-10";
		List<ExpressionCommon.Token> tokens = InputHandler.tokenize(formula);
		ExpressionScorer.ExpressionVal result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == -10);
		assertTrue(result.expressionTree.val.equals("-"));
		assertTrue(result.expressionTree.left == null);
		assertTrue(result.expressionTree.right.val.equals("10.0"));

		formula = "1*-10";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == -10);
		assertTrue(result.expressionTree.val.equals("*"));
		assertTrue(result.expressionTree.left.val.equals("1.0"));
		assertTrue(result.expressionTree.right.val.equals("-"));
		assertTrue(result.expressionTree.right.right.val.equals("10.0"));
		assertTrue(result.expressionTree.right.left == null);

		formula = "1+-10^2";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == -99);
		assertTrue(result.expressionTree.val.equals("+"));
		assertTrue(result.expressionTree.left.val.equals("1.0"));
		assertTrue(result.expressionTree.right.val.equals("-"));
		assertTrue(result.expressionTree.right.left == null);
		assertTrue(result.expressionTree.right.right.val.equals("^"));
		assertTrue(result.expressionTree.right.right.left.val.equals("10.0"));
		assertTrue(result.expressionTree.right.right.right.val.equals("2.0"));

		formula = "10^-2";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == 0.01);
		assertTrue(result.expressionTree.val.equals("^"));
		assertTrue(result.expressionTree.left.val.equals("10.0"));
		assertTrue(result.expressionTree.right.val.equals("-"));
		assertTrue(result.expressionTree.right.left == null);
		assertTrue(result.expressionTree.right.right.val.equals("2.0"));

		formula = "-log(2, 4)";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == -2);
		assertTrue(result.expressionTree.val.equals("-"));
		assertTrue(result.expressionTree.left == null);
		assertTrue(result.expressionTree.right.val.equals("log"));
		assertTrue(result.expressionTree.right.right.val.equals("4.0"));
		assertTrue(result.expressionTree.right.left.val.equals("2.0"));

		formula = "-(-10)";
		tokens = InputHandler.tokenize(formula);
		result = ExpressionScorer.evalTokens(tokens, true);
		assertTrue(result.value == 10);
		assertTrue(result.expressionTree.val.equals("-"));
		assertTrue(result.expressionTree.left == null);
		assertTrue(result.expressionTree.right.val.equals("-"));
		assertTrue(result.expressionTree.right.right.val.equals("10.0"));
		assertTrue(result.expressionTree.right.right.left == null);
	}

}
