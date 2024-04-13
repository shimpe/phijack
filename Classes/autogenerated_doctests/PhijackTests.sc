

PhijackTests : UnitTest {
    *new {
     ^super.new.init();
    }

    init {
    }

   test_one {
      	var pred = {
      		| val, idx |
      		idx == 3 // hijack Pseries to replace fourth element with 100
      	};
      	var p1 = Pseries(0, 5, 6);
      	var p2 = Pseq([100], inf);
      	var hj = Phijack(pred, p1, p2);
      	var hj_stream = hj.asStream;
      	this.assertArrayFloatEquals(hj_stream.all, [0, 5, 10, 100, 20, 25], "check output");
   }
   test_run_out {
      	var pred = {
      		| val, idx |
              idx.mod(2) == 0 // hijack Pseries to replace first, third, ... member
      	};
      	var p1 = Pseries(0, 5, 6);
      	var p2 = Pseq([100], 2); // limit hijacker to only two values to check that after 2 hijackings, normal values are used
      	var hj = Phijack(pred, p1, p2);
      	var hj_stream = hj.asStream;
      	this.assertArrayFloatEquals(hj_stream.all, [100, 5, 100, 15, 20, 25], "check output");
   }
}
