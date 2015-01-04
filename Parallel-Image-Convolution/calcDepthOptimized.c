// CS 61C Fall 2014 Project 3
// include SSE intrinsics
#if defined(_MSC_VER)
#include <intrin.h>
#elif defined(__GNUC__) && (defined(__x86_64__) || defined(__i386__))
#include <x86intrin.h>
#endif

#include "calcDepthOptimized.h"
#include "calcDepthNaive.h"

#define ABS(x) (((x) < 0) ? (-(x)) : (x))


void calcDepthOptimized(float *depth, float *left, float *right,
			int imageWidth, int imageHeight, int featureWidth,
			int featureHeight, int maximumDisplacement) {

    for (int y = 0; y < imageHeight; y++) {
	for (int x = 0; x < imageWidth; x++) {
	    index = y * imageWidth + x;
	    
	    if ((y < featureHeight) || (y >= imageHeight - featureHeight) ||
		(x < featureWidth) || (x >= imageWidth - featureWidth)) {
		depth[index] = 0;
		continue;
	    }

	    float minimumSquaredDifference = -1;
	    int minimumDy = 0;
	    int minimumDx = 0;

	    for (int dy = -maximumDisplacement; dy <= maximumDisplacement; dy++) {
		for (int dx = -maximumDisplacement; dx <= maximumDisplacement; dx++) {
		    if (y + dy - featureHeight < 0 || y + dy + featureHeight >= imageHeight ||
			x + dx - featureWidth < 0 || x + dx + featureWidth >= imageWidth) {
			continue;
		    }

		    float squaredDifference = 0;
		    float sqDiffs[4] = {0, 0, 0, 0};
		    
		    // Use SSE Intrinsics to handle general case

		    for (int boxY = -featureHeight; boxY <= featureHeight; boxY++) {
			int boxX;
			for (boxX = -featureWidth; boxX + 3 <= featureWidth; boxX += 4) {
			    int leftX = x + boxX;
			    int leftY = y + boxY;
			    int rightX = x + dx + boxX;
			    int rightY = y + dy + boxY;

			    int leftIndex = leftY * imageWidth + leftX;
			    int rightIndex = rightY * imageWidth + rightX;

			    __m128 leftPixels = _mm_loadu_ps(left + leftIndex);
			    __m128 rightPixels = _mm_loadu_ps(right + rightIndex);

			    __m128 difference = _mm_sub_ps(leftPixels, rightPixels);
			    __m128 squared = _mm_mul_ps(difference, difference);

			    _mm_storeu_ps(sqDiffs, squared);

			    squaredDifference += sqDiffs[0] + sqDiffs[1] + sqDiffs[2] + sqDiffs[3];
			}

			if ((minimumSquaredDifference != -1) && (squaredDifference > minimumSquaredDifference)) {
			    continue;
			}
		    
			// Optimizing the tail case by recognizing that and odd number mod 4 will be 3
			// and an even number mod 4 will be 1
			
			/*
			// Odd featureWidth leaves 3 pixels
			if (featureWidth % 2 != 0) {
			    for (; boxX <= featureWidth; boxX += 4) {
				int leftX = x + boxX;
				int leftY = y + boxY;
				int rightX = x + dx + boxX;
				int rightY = y + dy + boxY;

				int leftIndex = leftY * imageWidth + leftX;
				int rightIndex = rightY * imageWidth + rightX;

				__m128 leftPixels = _mm_loadu_ps(left + leftIndex);
				__m128 rightPixels = _mm_loadu_ps(right + rightIndex);

				__m128 difference = _mm_sub_ps(leftPixels, rightPixels);
				__m128 squared = _mm_mul_ps(difference, difference);

				_mm_storeu_ps(sqDiffs, squared);

				squaredDifference += sqDiffs[0] + sqDiffs[1] + sqDiffs[2];
			    }
			}

			// Even featureWidth leaves 1 pixels
			else {
			    int leftX = x + boxX;
			    int leftY = y + boxY;
			    int rightX = x + dx + boxX;
			    int rightY = y + dy + boxY;

			    float difference = left[leftY * imageWidth + leftX] -
				right[rightY * imageWidth + rightX];
			    squaredDifference += difference * difference;
			   
			}
			*/


			// Using naive methods to handle tail case
			for (; boxX <= featureWidth; boxX++) {
			    int leftX = x + boxX;
			    int leftY = y + boxY;
			    int rightX = x + dx + boxX;
			    int rightY = y + dy + boxY;

			    float difference = left[leftY * imageWidth + leftX] -
				right[rightY * imageWidth + rightX];
			    squaredDifference += difference * difference;

			}
		    }

		    if ((minimumSquaredDifference == -1) ||
			((minimumSquaredDifference == squaredDifference) &&
			 (displacementNaive(dx, dy) < displacementNaive(minimumDx, minimumDy))) ||
			(minimumSquaredDifference > squaredDifference)) {
			minimumSquaredDifference = squaredDifference;
			minimumDx = dx;
			minimumDy = dy;
		    }
		}
	    }

	    if (minimumSquaredDifference != -1) {
		if (maximumDisplacement == 0) {
		    depth[index] = 0;
		} else {
		    depth[index] = displacementNaive(minimumDx, minimumDy);
		}
	    } else {
		depth[index] = 0;
	    }
	}
    }
}



//Version 2
void calcDepthOptimized_newold(float *depth, float *left, float *right,
			       int imageWidth, int imageHeight, int featureWidth,
			       int featureHeight, int maximumDisplacement) {
    
    for (int y = 0; y < imageHeight; y++) {
	for (int x = 0; x < imageWidth; x++) {
	    int index = y*imageWidth + x;
	    if ((y < featureHeight) || (y >= imageHeight - featureHeight) ||
		(x < featureWidth) || (x >= imageWidth - featureWidth)) {
		depth[index] = 0;
		continue;
	    }

	    float minimumSquaredDifference = -1;
	    int minimumDy = 0;
	    int minimumDx = 0;

	    for (int dy = -maximumDisplacement; dy <= maximumDisplacement; dy++) {
		for (int dx = -maximumDisplacement; dx <= maximumDisplacement; dx++) {
		    if (y + dy - featureHeight < 0 || y + dy + featureHeight >= imageHeight ||
			x + dx - featureWidth < 0 || x + dx + featureWidth >= imageWidth) {
			continue;
		    }

		    float squaredDifference = 0;
		    float sqDiffs[4] = {0, 0, 0, 0};

		    for (int boxY = -featureHeight; boxY <= featureHeight; boxY++) {
			for (int boxX = -featureWidth; boxX <= featureWidth/4 * 4; boxX += 4) {
			    int leftX = x + boxX;
			    int leftY = y + boxY;
			    int rightX = x + dx + boxX;
			    int rightY = y + dy + boxY;

			    int leftIndex = leftY * imageWidth + leftX;
			    int rightIndex = rightY * imageWidth + rightX;

			    __m128 leftPixels = _mm_loadu_ps(left + leftIndex);
			    __m128 rightPixels = _mm_loadu_ps(right + rightIndex);

			    __m128 difference = _mm_sub_ps(leftPixels, rightPixels);
			    __m128 squared = _mm_mul_ps(difference, difference);

			    _mm_storeu_ps(sqDiffs, squared);

			    squaredDifference += sqDiffs[0] + sqDiffs[1] + sqDiffs[2] + sqDiffs[3];
			}
		    }

		    if ((minimumSquaredDifference != -1) &&
			(squaredDifference > minimumSquaredDifference)) {
			continue;
		    }
		    
		    // Tail-Case
		    for (int boxY = -featureHeight; boxY <= featureHeight; boxY++) {
			for (int boxX = (featureWidth/4 * 4) + 1; boxX <= featureWidth; boxX++) {
			    int leftX = x + boxX;
			    int leftY = y + boxY;
			    int rightX = x + dx + boxX;
			    int rightY = y + dy + boxY;

			    float difference = left[leftY * imageWidth + leftX] -
				right[rightY * imageWidth + rightX];
			    squaredDifference += difference * difference;
			}
		    }
		    
		    if ((minimumSquaredDifference == -1) ||
			((minimumSquaredDifference == squaredDifference) &&
			 (displacementNaive(dx, dy) < displacementNaive(minimumDx, minimumDy))) ||
			(minimumSquaredDifference > squaredDifference)) {			

			minimumSquaredDifference = squaredDifference;	
			minimumDx = dx;
			minimumDy = dy;
		    }

		}
	    }
	    if (minimumSquaredDifference != -1) {
		if (maximumDisplacement == 0) {
		    depth[index] = 0;
		} else {
		    depth[index] = displacementNaive(minimumDx, minimumDy);
		}
	    } else {
		depth[index] = 0;
	    }
	}
    }
}



/*
void calcDepthOptimized_old(float *depth, float *left, float *right,
			int imageWidth, int imageHeight, int featureWidth,
			int featureHeight, int maximumDisplacement) {

    __m128i imageWidth128 = _mm_set_epi32(imageWidth, imageWidth, imageWidth, imageWidth);
    
    for (int y = imageHeight%4; y < imageHeight; y++) {
	__m128i y128 = _mm_set_epi32(y, y, y, y);
	for (int x = imageWidth%4; x < imageWidth; x++) {
	    __m128i x128 = _mm_set_epi32(x, x, x, x);

	    if ((y < featureHeight) || (y >= imageHeight - featureHeight) ||
		(x < featureWidth) || (x >= imageWidth - featureWidth)) {
		depth[y * imageWidth + x] = 0;
		continue;
	    }

	    float minimumSquaredDifference = -1;
	    int minimumDy = 0;
	    int minimumDx = 0;

	    for (int dy = -((2*maximumDisplacement+1)%4); dy <= maximumDisplacement; dy++) {
		__m128i dy128 = _mm_set_epi32(dy, dy, dy, dy);
		for (int dx = -((2*maximumDisplacement+1)%4); dx <= maximumDisplacement; dx++) {
		    __m128i dx128 = _mm_set_epi32(dx, dx, dx, dx);

		    if (y + dy - featureHeight < 0 || y + dy + featureHeight >= imageHeight ||
			x + dx - featureWidth < 0 || x + dx + featureWidth >= imageWidth) {
			continue;
		    }

		    float squaredDifference = 0;
		    __m128 squaredDifference128 = _mm_setzero_ps();

		    for (int boxY = -((2*featureHeight+1)%4); boxY <= featureHeight; boxY++) {
			__m128i boxY128 = _mm_set_epi32(boxY, boxY, boxY, boxY);
			for (int boxX = -((2*featureWidth+1)%16); boxX <= featureWidth; boxX += 16) {	
			    // Computing the squaredDifference in a box, 4 pixels at a time
			    __m128i boxX128 = _mm_set_epi32(boxX, boxX+4, boxX+8, boxX+12);
			    
			    __m128i leftX = _mm_add_epi32(x128, boxX128);
			    __m128i leftY = _mm_add_epi32(y128, boxY128);
			    __m128i rightX = _mm_add_epi32(dx128, leftX);
			    __m128i rightY = _mm_add_epi32(dy128, leftY);

			    __m128i left128i = _mm_add_epi32(_mm_mul_epi32(leftY, imageWidth128),
							      leftX);
			    __m128i right128i = _mm_add_epi32(_mm_mul_epi32(rightY, imageWidth128),
							       rightX);
			    float leftIndex[4] = {0, 0, 0, 0};
			    _mm_storeu_ps(leftIndex, (__m128) left128i);
			    float rightIndex[4] = {0, 0, 0, 0};
			    _mm_storeu_ps(rightIndex, (__m128) right128i);

			    
			    // error: invalid operands to binary + (have ‘float *’ and ‘float *’)
			    __m128 left1 = _mm_loadu_ps((float *)(left + (float *)(leftIndex)));
			    __m128 left2 = _mm_loadu_ps(&left + (float *) leftIndex[1]);
			    __m128 left3 = _mm_loadu_ps(left + leftIndex + 2);
			    __m128 left4 = _mm_loadu_ps(left + leftIndex + 3);

			    __m128 right1 = _mm_loadu_ps(*(right + 1));
			    __m128 right2 = _mm_loadu_ps(right + rightIndex + 1);
			    __m128 right3 = _mm_loadu_ps(right + rightIndex + 2);
			    __m128 right4 = _mm_loadu_ps(right + rightIndex + 3);

			    __m128 difference1 = _mm_sub_ps(left1, right1);
			    __m128 difference2 = _mm_sub_ps(left2, right2);
			    __m128 difference3 = _mm_sub_ps(left3, right3);
			    __m128 difference4 = _mm_sub_ps(left4, right4);

			    __m128 square1 = _mm_mul_ps(difference1, difference1);
			    __m128 square2 = _mm_mul_ps(difference2, difference2);
			    __m128 square3 = _mm_mul_ps(difference3, difference3);
			    __m128 square4 = _mm_mul_ps(difference4, difference4);

			    float addend[4] = {0, 0, 0, 0};
			    //Sum up the four differences
			    _mm_storeu_ps(addend, square1);
			    float sum1 =  *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    _mm_storeu_ps(addend, square2);
			    float sum2 = *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    _mm_storeu_ps(addend, square3);
			    float sum3 =  *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    _mm_storeu_ps(addend, square4);
			    float sum4 =  *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    __m128 sum = _mm_set_ps(sum1, sum2, sum3, sum4);
			    squaredDifference128 = _mm_add_ps(squaredDifference128, sum);
			}
		    }
		    float squaredSum[4] = {0, 0, 0, 0};
		    _mm_storeu_ps(squaredSum, squaredDifference128);
		    squaredDifference =  *squaredSum + *(squaredSum + 1) +
			*(squaredSum + 2) + *(squaredSum + 3);
		    
		    if ((minimumSquaredDifference == -1) ||
			(minimumSquaredDifference == squaredDifference &&
			 displacementNaive(dx, dy) < displacementNaive(minimumDx, minimumDy)) ||
			(minimumSquaredDifference > squaredDifference &&
			 minimumSquaredDifference != squaredDifference)) {
			
			minimumSquaredDifference = squaredDifference;
			minimumDx = dx;
			minimumDy = dy;
		    }

		}
	    }
	    int index = y * imageWidth + x;
	    if (minimumSquaredDifference != -1) {
		if (maximumDisplacement == 0) {
		    // Could be buggy
		    depth[index] = 0;
		} else {
		    depth[index] = displacementNaive(minimumDx, minimumDy);
		}
	    } else {
		depth[index] = 0;
	    }
	}
    }
}
*/
/*

void calcDepthOptimized(float *depth, float *left, float *right,
			int imageWidth, int imageHeight, int featureWidth,
			int featureHeight, int maximumDisplacement) {

    __m128i imageWidth128 = _mm_set_epi32(imageWidth, imageWidth, imageWidth, imageWidth);
    
    for (int y = imageHeight%4; y < imageHeight; y++) {
	__m128i y128 = _mm_set_epi32(y, y, y, y);
	for (int x = imageWidth%4; x < imageWidth; x++) {
	    __m128i x128 = _mm_set_epi32(x, x, x, x);

	    if ((y < featureHeight) || (y >= imageHeight - featureHeight) ||
		(x < featureWidth) || (x >= imageWidth - featureWidth)) {
		depth[y * imageWidth + x] = 0;
		continue;
	    }

	    float minimumSquaredDifference = -1;
	    int minimumDy = 0;
	    int minimumDx = 0;

	    for (int dy = -((2*maximumDisplacement+1)%4); dy <= maximumDisplacement; dy++) {
		__m128i dy128 = _mm_set_epi32(dy, dy, dy, dy);
		for (int dx = -((2*maximumDisplacement+1)%4); dx <= maximumDisplacement; dx++) {
		    __m128i dx128 = _mm_set_epi32(dx, dx, dx, dx);

		    if (y + dy - featureHeight < 0 || y + dy + featureHeight >= imageHeight ||
			x + dx - featureWidth < 0 || x + dx + featureWidth >= imageWidth) {
			continue;
		    }

		    float squaredDifference = 0;
		    __m128 squaredDifference128 = _mm_setzero_ps();

		    for (int boxY = -((2*featureHeight+1)%4); boxY <= featureHeight; boxY++) {
			__m128i boxY128 = _mm_set_epi32(boxY, boxY, boxY, boxY);
			for (int boxX = -((2*featureWidth+1)%16); boxX <= featureWidth; boxX += 16) {	
			    // Computing the squaredDifference in a box, 4 pixels at a time
			    __m128i boxX128 = _mm_set_epi32(boxX, boxX+4, boxX+8, boxX+12);
			    
			    __m128i leftX = _mm_add_epi32(x128, boxX128);
			    __m128i leftY = _mm_add_epi32(y128, boxY128);
			    __m128i rightX = _mm_add_epi32(dx128, leftX);
			    __m128i rightY = _mm_add_epi32(dy128, leftY);

			    __m128i left128i = _mm_add_epi32(_mm_mul_epi32(leftY, imageWidth128),
							      leftX);
			    __m128i right128i = _mm_add_epi32(_mm_mul_epi32(rightY, imageWidth128),
							       rightX);
			    float leftIndex[4] = {0, 0, 0, 0};
			    _mm_storeu_ps(leftIndex, (__m128) left128i);
			    float rightIndex[4] = {0, 0, 0, 0};
			    _mm_storeu_ps(rightIndex, (__m128) right128i);

			    
			    // error: invalid operands to binary + (have ‘float *’ and ‘float *’)
			    __m128 left1 = _mm_loadu_ps((float *)(left + (float *)(leftIndex)));
			    __m128 left2 = _mm_loadu_ps(&left + (float *) leftIndex[1]);
			    __m128 left3 = _mm_loadu_ps(left + leftIndex + 2);
			    __m128 left4 = _mm_loadu_ps(left + leftIndex + 3);

			    __m128 right1 = _mm_loadu_ps(*(right + 1));
			    __m128 right2 = _mm_loadu_ps(right + rightIndex + 1);
			    __m128 right3 = _mm_loadu_ps(right + rightIndex + 2);
			    __m128 right4 = _mm_loadu_ps(right + rightIndex + 3);

			    __m128 difference1 = _mm_sub_ps(left1, right1);
			    __m128 difference2 = _mm_sub_ps(left2, right2);
			    __m128 difference3 = _mm_sub_ps(left3, right3);
			    __m128 difference4 = _mm_sub_ps(left4, right4);

			    __m128 square1 = _mm_mul_ps(difference1, difference1);
			    __m128 square2 = _mm_mul_ps(difference2, difference2);
			    __m128 square3 = _mm_mul_ps(difference3, difference3);
			    __m128 square4 = _mm_mul_ps(difference4, difference4);

			    float addend[4] = {0, 0, 0, 0};
			    //Sum up the four differences
			    _mm_storeu_ps(addend, square1);
			    float sum1 =  *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    _mm_storeu_ps(addend, square2);
			    float sum2 = *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    _mm_storeu_ps(addend, square3);
			    float sum3 =  *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    _mm_storeu_ps(addend, square4);
			    float sum4 =  *addend + *(addend + 1) +
				*(addend + 2) + *(addend + 3);
			    __m128 sum = _mm_set_ps(sum1, sum2, sum3, sum4);
			    squaredDifference128 = _mm_add_ps(squaredDifference128, sum);
			}
		    }
		    float squaredSum[4] = {0, 0, 0, 0};
		    _mm_storeu_ps(squaredSum, squaredDifference128);
		    squaredDifference =  *squaredSum + *(squaredSum + 1) +
			*(squaredSum + 2) + *(squaredSum + 3);
		    
		    if ((minimumSquaredDifference == -1) ||
			(minimumSquaredDifference == squaredDifference &&
			 displacementNaive(dx, dy) < displacementNaive(minimumDx, minimumDy)) ||
			(minimumSquaredDifference > squaredDifference &&
			 minimumSquaredDifference != squaredDifference)) {
			
			minimumSquaredDifference = squaredDifference;
			minimumDx = dx;
			minimumDy = dy;
		    }

		}
	    }
	    int index = y * imageWidth + x;
	    if (minimumSquaredDifference != -1) {
		if (maximumDisplacement == 0) {
		    // Could be buggy
		    depth[index] = 0;
		} else {
		    depth[index] = displacementNaive(minimumDx, minimumDy);
		}
	    } else {
		depth[index] = 0;
	    }
	}
    }
}
*/
