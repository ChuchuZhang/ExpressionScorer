# Expression Evaluator
This expression evaluator takes filenames as parameters and evaluates the mathmetical expressions inside each file. Each line inside the files represents an expression. To evaluate the value of the expression, this evaluator first tokenizes and validates the expressions and then converts the expressions into their reverse polish notations. Next, it builds the binary expression trees while evaluating the output values and then output to files.

## How to use
The entry point of the program is in `ExpressionEvaluator.java`. Filenames are passed by the `String[] args` to the main function and the evaluator would create result files in the same folder as the input file. You can also specified a `REPEAT_TIME` env variable to do microbenmarking. The number of `REPEAT_TIME` represents how many time the program will evaluate the expressions in the files, with and without cache optimization. One example is in directory `ExpressionEval/example`. Input file is `testdata.txt` and the rest are outputs for each expression. Benchmarking with `REPEAT_TIME = 200` returned somthing like

```
results for file /Users/chuchuzh/testdata.txt: [/Users/chuchuzh/testdata_0, /Users/chuchuzh/testdata_1, /Users/chuchuzh/testdata_2, /Users/chuchuzh/testdata_3, /Users/chuchuzh/testdata_4, /Users/chuchuzh/testdata_5, /Users/chuchuzh/testdata_6, /Users/chuchuzh/testdata_7, /Users/chuchuzh/testdata_8, /Users/chuchuzh/testdata_9]
Seconds elapsed for evaluation without cache: 2.91 seconds.
results for file /Users/chuchuzh/testdata.txt: [/Users/chuchuzh/testdata_0, /Users/chuchuzh/testdata_1, /Users/chuchuzh/testdata_2, /Users/chuchuzh/testdata_3, /Users/chuchuzh/testdata_4, /Users/chuchuzh/testdata_5, /Users/chuchuzh/testdata_6, /Users/chuchuzh/testdata_7, /Users/chuchuzh/testdata_8, /Users/chuchuzh/testdata_9]
Seconds elapsed for evaluation with cache: 1.941 seconds.
```
