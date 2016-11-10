/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2012-2014, Purdue University
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.test.builtins;

import org.junit.Test;

import com.oracle.truffle.r.test.TestBase;

// Checkstyle: stop line length check
public class TestBuiltin_Re extends TestBase {

    @Test
    public void testRe1() {
        assertEval("argv <- list(c(0.997564050259824+0.069756473744125i, 0.987588409757226+0.069058909006684i, 0.977612769254628+0.068361344269243i, 0.96763712875203+0.067663779531802i, 0.957661488249431+0.06696621479436i, 0.947685847746833+0.066268650056919i, 0.937710207244235+0.065571085319478i, 0.927734566741637+0.064873520582037i, 0.917758926239038+0.064175955844595i, 0.90778328573644+0.063478391107154i, 0.897807645233842+0.062780826369713i, 0.887832004731244+0.062083261632272i, 0.877856364228645+0.06138569689483i, 0.867880723726047+0.060688132157389i, 0.857905083223449+0.059990567419948i, 0.847929442720851+0.059293002682507i, 0.837953802218252+0.058595437945065i, 0.827978161715654+0.057897873207624i, 0.818002521213056+0.057200308470183i, 0.808026880710458+0.056502743732742i, 0.79805124020786+0.0558051789953i, 0.788075599705261+0.055107614257859i, 0.778099959202663+0.054410049520418i, 0.768124318700065+0.053712484782976i, 0.758148678197466+0.053014920045535i, 0.748173037694868+0.052317355308094i, 0.73819739719227+0.051619790570653i, 0.728221756689672+0.050922225833211i, 0.718246116187073+0.05022466109577i, 0.708270475684475+0.049527096358329i, 0.698294835181877+0.048829531620888i, 0.688319194679279+0.048131966883446i, 0.67834355417668+0.047434402146005i, 0.668367913674082+0.046736837408564i, 0.658392273171484+0.046039272671123i, 0.648416632668886+0.045341707933681i, 0.638440992166287+0.04464414319624i, 0.628465351663689+0.043946578458799i, 0.618489711161091+0.043249013721358i, 0.608514070658493+0.042551448983916i, 0.598538430155894+0.041853884246475i, 0.588562789653296+0.041156319509034i, 0.578587149150698+0.040458754771593i, 0.5686115086481+0.039761190034151i, 0.558635868145502+0.03906362529671i, 0.548660227642903+0.038366060559269i, 0.538684587140305+0.037668495821828i, 0.528708946637707+0.036970931084386i, 0.518733306135109+0.036273366346945i, 0.50875766563251+0.035575801609504i, 0.498782025129912+0.034878236872063i, 0.488806384627314+0.034180672134621i, 0.478830744124716+0.03348310739718i, 0.468855103622117+0.032785542659739i, 0.458879463119519+0.032087977922298i, 0.448903822616921+0.031390413184856i, 0.438928182114323+0.030692848447415i, 0.428952541611724+0.029995283709974i, 0.418976901109126+0.029297718972533i, 0.409001260606528+0.028600154235091i, 0.39902562010393+0.02790258949765i, 0.389049979601331+0.027205024760209i, 0.379074339098733+0.026507460022768i, 0.369098698596135+0.025809895285326i, 0.359123058093537+0.025112330547885i, 0.349147417590938+0.024414765810444i, 0.33917177708834+0.023717201073003i, 0.329196136585742+0.023019636335561i, 0.319220496083144+0.02232207159812i, 0.309244855580545+0.021624506860679i, 0.299269215077947+0.020926942123238i, 0.289293574575349+0.020229377385796i, 0.279317934072751+0.019531812648355i, 0.269342293570153+0.018834247910914i, 0.259366653067554+0.018136683173473i, 0.249391012564956+0.017439118436031i, 0.239415372062358+0.01674155369859i, 0.22943973155976+0.016043988961149i, 0.219464091057161+0.015346424223708i, 0.209488450554563+0.014648859486266i, 0.199512810051965+0.013951294748825i, 0.189537169549367+0.013253730011384i, 0.179561529046768+0.012556165273943i, 0.16958588854417+0.011858600536501i, 0.159610248041572+0.01116103579906i, 0.149634607538974+0.010463471061619i, 0.139658967036375+0.009765906324178i, 0.129683326533777+0.009068341586736i, 0.119707686031179+0.008370776849295i, 0.109732045528581+0.007673212111854i, 0.0997564050259824+0.0069756473744125i, 0.0897807645233842+0.0062780826369713i, 0.0798051240207859+0.00558051789953i, 0.0698294835181877+0.0048829531620888i, 0.0598538430155895+0.0041853884246475i, 0.0498782025129912+0.0034878236872063i, 0.039902562010393+0.002790258949765i, 0.0299269215077947+0.0020926942123238i, 0.0199512810051965+0.0013951294748825i, 0.00997564050259824+0.00069756473744125i, 0+0i));Re(argv[[1]]);");
    }

    @Test
    public void testRe2() {
        assertEval("argv <- list(structure(c(3+2i, 3+2i, NA, 3+2i, 3+2i, 3+2i, 3+2i, 3+2i, 4-5i, 3-5i, NA, NA, 2-5i, 3-5i, 4-5i, 5-5i), .Dim = c(8L, 2L), .Dimnames = list(NULL, c('x1', 'x2'))));Re(argv[[1]]);");
    }

    @Test
    public void testRe3() {
        assertEval("argv <- list(c(-0.1-0.9i, -0.2-0.8i, -0.3-0.7i, -0.4-0.6i, -0.1-0.5i, -0.2-0.4i, -0.3-0.3i, -0.4-0.2i, -0.1-0.1i));Re(argv[[1]]);");
    }

    @Test
    public void testRe4() {
        assertEval("argv <- list(c(0.923879532511287+0.38268343236509i, 0.707106781186548+0.707106781186547i, 0.38268343236509+0.923879532511287i, 0+1i, -0.38268343236509+0.923879532511287i, -0.707106781186547+0.707106781186548i, -0.923879532511287+0.38268343236509i, -1+0i, -0.923879532511287-0.38268343236509i, -0.707106781186548-0.707106781186547i, -0.38268343236509-0.923879532511287i, 0-1i, 0.38268343236509-0.923879532511287i, 0.707106781186547-0.707106781186548i, 0.923879532511287-0.38268343236509i, 1-0i));Re(argv[[1]]);");
    }

    @Test
    public void testRe5() {
        assertEval("argv <- list(logical(0));Re(argv[[1]]);");
    }

    @Test
    public void testRe6() {
        assertEval("argv <- list(FALSE);Re(argv[[1]]);");
    }

    @Test
    public void testRe7() {
        assertEval("argv <- list(c(0.00086580086580088+0i, 0.00259740259740261+0i, 0.00519480519480521+0i, 0.00865800865800867+0i, 0.012987012987013+0i, 0.0181818181818182+0i, 0.0242424242424242+0i, 0.0303030303030303+0i, 0.0363636363636363+0i, 0.0424242424242424+0i, 0.0484848484848484+0i, 0.0536796536796536+0i, 0.058008658008658+0i, 0.0614718614718614+0i, 0.064069264069264+0i, 0.0649350649350649+0i, 0.064069264069264+0i, 0.0614718614718614+0i, 0.058008658008658+0i, 0.0536796536796536+0i, 0.0484848484848485+0i, 0.0424242424242424+0i, 0.0363636363636363+0i, 0.0303030303030303+0i, 0.0242424242424242+0i, 0.0181818181818182+0i, 0.012987012987013+0i, 0.00865800865800867+0i, 0.00519480519480521+0i, 0.00259740259740261+0i, 0.000865800865800882+0i));Re(argv[[1]]);");
    }

    @Test
    public void testRe() {
        assertEval("{ Re(1+1i) }");
        assertEval("{ Re(1) }");
        assertEval("{ Re(c(1+1i,2-2i)) }");
        assertEval("{ Re(c(1,2)) }");
        assertEval("{ Re(as.double(NA)) }");
        assertEval("{ Re(c(1,NA,2)) }");
        assertEval("{ Re(NA+2i) }");

        assertEval("{ x <- 1:2 ; attr(x,\"my\") <- 2 ; Re(x) }");
        assertEval("{ x <- c(1+2i,3-4i) ; attr(x,\"my\") <- 2 ; Re(x) }");

        assertEval("{ Re(as.raw(12)) }");

        assertEval("Re(c(NaN, 1+1i))");
        assertEval("Re(NaN)");
    }
}
