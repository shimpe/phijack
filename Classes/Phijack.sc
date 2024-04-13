/*
[general]
title = "Phijack"
summary = "a value pattern that hijacks the output of another value pattern when a predicate is true"
categories = "Patterns"
related = "Classes/Pattern"
description = '''
Phijack replaces the value produced by a pattern stream with a value from another pattern stream in case a predicate is true. The original pattern stream value in that case is replaced (not just postponed), meaning it is lost forever.
'''
*/
Phijack : Pattern {
	/*
	[method.predicate]
	description = '''
	predicate must be a function that accepts 2 arguments: value and idx, and that returns a bool.
	value is the current value of the normal pattern and idx in the how many'th element of the stream we are examining. The bool return value indicates if the normal pattern output should be replaced with the hijack pattern
	output.
	'''
	*/
	var <>predicate;
	/*
	[method.pattern]
	description = '''
	pattern is a value pattern that generates values which can be hijacked if the predicate function is true.
	hijacking means that the value produced by pattern is replaced with a value produced by the hijacker_pattern
	'''
	*/
	var <>pattern;
	/*
	[method.hijacker_pattern]
	description = '''
	a value pattern that generates the values that overwrite those of the normal pattern, in case the predicate function is true
	'''
	*/
	var <>hijacker_pattern;

	/*
	[classmethod.new]
	description = '''
	new creates a new Phijack
	'''
	[classmethod.new.args]
	predicate = "function, accepting 2 arguments value and idx, and which returns a bool indicating if hijacking should take place"
	pattern = "a value pattern producing values which are output if predicate is false"
	hijacker_pattern = "a value pattern producing values which are output instead of the pattern value if predicate is true. Note that hijacker_pattern only advances when predicate is true, and pattern always advances (so when it gets hijacked, one of its values is lost). Once the hijacker_pattern runs out of values, only normal pattern values are returned."
	'''
	*/
	*new { arg predicate, pattern, hijacker_pattern;
		^super.newCopyArgs(predicate, pattern, hijacker_pattern);
	}
	/*
	[method.storeArgs]
	description = '''
	this function makes sure a nice representation is printed to the post window when creating a new instance
	'''
	[method.storeArgs.args]
	predicate = "function, accepting 2 arguments value and idx, and which returns a bool"
	pattern = "a value pattern producing values which are output if predicate is false"
	hijacker_pattern = "a value pattern producing values which are output instead of the pattern value if predicate is true. Note that hijacker_pattern only advances when predicate is true, and pattern always advances (so when it gets hijacked, one of its values is lost). Note: oncde the hijack pattern runs out of values, only normal pattern values are returned."
	'''
	*/
	storeArgs { ^[predicate, pattern, hijacker_pattern] }

	/*
	[method.embedInStream]
	description = '''
	method that translates the pattern into a stream of values
	'''
	*/
	embedInStream {
		| inVal |
		var nextval;
		var stream = pattern.asStream;
		var hijackstream = hijacker_pattern.asStream;
		inf.do({
			| idx |
			// get next value for normal pattern
			nextval = stream.next;
			if (nextval.isNil) { ^nextval; };
			if (predicate.value(nextval, idx)) {
				// if predicate is true, replace it with one from the hijacking pattern
				var replacement = hijackstream.next;
				if (replacement.notNil) {
					replacement.yield;
				} {
					// unless the hijacker ran out of values, in which case we keep returning normal values
					nextval.yield;
				}
			} {
				// if predicate is false, do not replace
				nextval.yield;
			};
		});
	}
}
/*
[examples]
example_1 = '''
(
// replace every third element in a Pseries with 100
var p = Pseries(1, 20, 20);
var hijacker = Pseq([100], inf);
var predicate = {
 | value, idx |
 (idx + 1).mod(3) == 0
};
var pvalues = p.asStream.all;
var hvalues = Phijack(predicate, p, hijacker).asStream.all;
pvalues.debug("normal pattern values");
hvalues.debug("hijacked pattern values");
)
'''
*/

/*
[doctests.PhijackTests]
test_one = '''
	var pred = {
		| val, idx |
		idx == 3 // hijack Pseries to replace fourth element with 100
	};
	var p1 = Pseries(0, 5, 6);
	var p2 = Pseq([100], inf);
	var hj = Phijack(pred, p1, p2);
	var hj_stream = hj.asStream;
	this.assertArrayFloatEquals(hj_stream.all, [0, 5, 10, 100, 20, 25], "check output");
'''
test_run_out = '''
	var pred = {
		| val, idx |
        idx.mod(2) == 0 // hijack Pseries to replace first, third, ... member
	};
	var p1 = Pseries(0, 5, 6);
	var p2 = Pseq([100], 2); // limit hijacker to only two values to check that after 2 hijackings, normal values are used
	var hj = Phijack(pred, p1, p2);
	var hj_stream = hj.asStream;
	this.assertArrayFloatEquals(hj_stream.all, [100, 5, 100, 15, 20, 25], "check output");
'''
*/
