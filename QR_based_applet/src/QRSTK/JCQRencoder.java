package QRSTK;
// The Alan Turing Institute
// Oct. 2021
// C.Hicks 
// With help from https://codegolf.stackexchange.com/questions/19970/qr-codes-and-all-that-jazz
// and https://www.nayuki.io/page/creating-a-qr-code-step-by-step 

import javacard.framework.*;

public class JCQRencoder {

    // GF fields needed for computing reed solmon codes (ECC)
    static final short [] gf_exp = {1, 2, 4, 8, 16, 32, 64, 128, 29, 58, 116, 232, 205, 135, 19, 38, 76, 152, 45, 90, 180, 117, 234, 201, 143, 3, 6, 12, 24, 48, 96, 192, 157, 39, 78, 156, 37, 74, 148, 53, 106, 212, 181, 119, 238, 193, 159, 35, 70, 140, 5, 10, 20, 40, 80, 160, 93, 186, 105, 210, 185, 111, 222, 161, 95, 190, 97, 194, 153, 47, 94, 188, 101, 202, 137, 15, 30, 60, 120, 240, 253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223, 163, 91, 182, 113, 226, 217, 175, 67, 134, 17, 34, 68, 136, 13, 26, 52, 104, 208, 189, 103, 206, 129, 31, 62, 124, 248, 237, 199, 147, 59, 118, 236, 197, 151, 51, 102, 204, 133, 23, 46, 92, 184, 109, 218, 169, 79, 158, 33, 66, 132, 21, 42, 84, 168, 77, 154, 41, 82, 164, 85, 170, 73, 146, 57, 114, 228, 213, 183, 115, 230, 209, 191, 99, 198, 145, 63, 126, 252, 229, 215, 179, 123, 246, 241, 255, 227, 219, 171, 75, 150, 49, 98, 196, 149, 55, 110, 220, 165, 87, 174, 65, 130, 25, 50, 100, 200, 141, 7, 14, 28, 56, 112, 224, 221, 167, 83, 166, 81, 162, 89, 178, 121, 242, 249, 239, 195, 155, 43, 86, 172, 69, 138, 9, 18, 36, 72, 144, 61, 122, 244, 245, 247, 243, 251, 235, 203, 139, 11, 22, 44, 88, 176, 125, 250, 233, 207, 131, 27, 54, 108, 216, 173, 71, 142, 1, 2, 4, 8, 16, 32, 64, 128, 29, 58, 116, 232, 205, 135, 19, 38, 76, 152, 45, 90, 180, 117, 234, 201, 143, 3, 6, 12, 24, 48, 96, 192, 157, 39, 78, 156, 37, 74, 148, 53, 106, 212, 181, 119, 238, 193, 159, 35, 70, 140, 5, 10, 20, 40, 80, 160, 93, 186, 105, 210, 185, 111, 222, 161, 95, 190, 97, 194, 153, 47, 94, 188, 101, 202, 137, 15, 30, 60, 120, 240, 253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223, 163, 91, 182, 113, 226, 217, 175, 67, 134, 17, 34, 68, 136, 13, 26, 52, 104, 208, 189, 103, 206, 129, 31, 62, 124, 248, 237, 199, 147, 59, 118, 236, 197, 151, 51, 102, 204, 133, 23, 46, 92, 184, 109, 218, 169, 79, 158, 33, 66, 132, 21, 42, 84, 168, 77, 154, 41, 82, 164, 85, 170, 73, 146, 57, 114, 228, 213, 183, 115, 230, 209, 191, 99, 198, 145, 63, 126, 252, 229, 215, 179, 123, 246, 241, 255, 227, 219, 171, 75, 150, 49, 98, 196, 149, 55, 110, 220, 165, 87, 174, 65, 130, 25, 50, 100, 200, 141, 7, 14, 28, 56, 112, 224, 221, 167, 83, 166, 81, 162, 89, 178, 121, 242, 249, 239, 195, 155, 43, 86, 172, 69, 138, 9, 18, 36, 72, 144, 61, 122, 244, 245, 247, 243, 251, 235, 203, 139, 11, 22, 44, 88, 176, 125, 250, 233, 207, 131, 27, 54, 108, 216, 173, 71, 142, 1, 2};
    static final short [] gf_log = {0, 0, 1, 25, 2, 50, 26, 198, 3, 223, 51, 238, 27, 104, 199, 75, 4, 100, 224, 14, 52, 141, 239, 129, 28, 193, 105, 248, 200, 8, 76, 113, 5, 138, 101, 47, 225, 36, 15, 33, 53, 147, 142, 218, 240, 18, 130, 69, 29, 181, 194, 125, 106, 39, 249, 185, 201, 154, 9, 120, 77, 228, 114, 166, 6, 191, 139, 98, 102, 221, 48, 253, 226, 152, 37, 179, 16, 145, 34, 136, 54, 208, 148, 206, 143, 150, 219, 189, 241, 210, 19, 92, 131, 56, 70, 64, 30, 66, 182, 163, 195, 72, 126, 110, 107, 58, 40, 84, 250, 133, 186, 61, 202, 94, 155, 159, 10, 21, 121, 43, 78, 212, 229, 172, 115, 243, 167, 87, 7, 112, 192, 247, 140, 128, 99, 13, 103, 74, 222, 237, 49, 197, 254, 24, 227, 165, 153, 119, 38, 184, 180, 124, 17, 68, 146, 217, 35, 32, 137, 46, 55, 63, 209, 91, 149, 188, 207, 205, 144, 135, 151, 178, 220, 252, 190, 97, 242, 86, 211, 171, 20, 42, 93, 158, 132, 60, 57, 83, 71, 109, 65, 162, 31, 45, 67, 216, 183, 123, 164, 118, 196, 23, 73, 236, 127, 12, 111, 246, 108, 161, 59, 82, 41, 157, 85, 170, 251, 96, 134, 177, 187, 204, 62, 90, 203, 89, 95, 176, 156, 169, 160, 81, 11, 245, 22, 235, 122, 117, 44, 215, 79, 174, 213, 233, 230, 231, 173, 232, 116, 214, 244, 234, 168, 80, 88, 175};

    static final short[] v2_template = {(short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)1, (short)0, (short)1, (short)0, (short)1, (short)0, (short)1, (short)0, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)1, (short)0, (short)0, (short)1, (short)1, (short)1, (short)0, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)0, (short)1, (short)0, (short)1, (short)1, (short)1, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)1, (short)1, (short)1, (short)1, (short)2, (short)2, (short)2, (short)2, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)0, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)0, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)0, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)1, (short)1, (short)1, (short)1, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)1, (short)1, (short)1, (short)0, (short)1, (short)0, (short)0, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)1, (short)0, (short)1, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2, (short)2};
    static final boolean[] v2_mask_template = {false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false,  false,  false,  false,  false,  false,  false,  false,  false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true};
    static final boolean[] v2_mask4_template = {true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true,  true,  true,  false,  false,  false,  true};

    static final byte[] v2_x_steps = {1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1};
    static final byte[] v2_y_steps = {0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0};
    static final short[] v2_mask_cursor = {624,  623,  599,  598,  574,  573,  549,  548,  524,  523,  499,  498,  474,  473,  449,  448,  424,  423,  399,  398,  374,  373,  349,  348,  324,  323,  299,  298,  274,  273,  249,  248,  224,  223,  199,  198,  174,  173,  149,  148,  124,  123,  99,  98,  74,  73,  49,  48,  24,  23,  22,  21,  47,  46,  72,  71,  97,  96,  122,  121,  147,  146,  172,  171,  197,  196,  222,  221,  247,  246,  272,  271,  297,  296,  322,  321,  347,  346,  372,  371,  397,  396,  422,  421,  447,  446,  472,  471,  497,  496,  522,  521,  547,  546,  572,  571,  597,  596,  622,  621,  620,  619,  595,  594,  570,  569,  545,  544,  520,  519,  495,  494,  470,  469,  445,  444,  420,  419,  395,  394,  370,  369,  345,  344,  320,  319,  295,  294,  270,  269,  245,  244,  220,  219,  195,  194,  170,  169,  145,  144,  120,  119,  95,  94,  70,  69,  45,  44,  20,  19,  18,  17,  43,  42,  68,  67,  93,  92,  118,  117,  143,  142,  168,  167,  193,  192,  218,  217,  243,  242,  268,  267,  293,  292,  318,  317,  343,  342,  368,  367,  393,  392,  418,  417,  443,  442,  468,  467,  493,  492,  518,  517,  543,  542,  568,  567,  593,  592,  618,  617,  616,  615,  591,  590,  566,  565,  541,  540,  516,  515,  491,  490,  466,  465,  441,  440,  416,  415,  391,  390,  366,  365,  341,  340,  316,  315,  291,  290,  266,  265,  241,  240,  216,  215,  191,  190,  166,  165,  141,  140,  116,  115,  91,  90,  66,  65,  41,  40,  16,  15,  14,  13,  39,  38,  64,  63,  89,  88,  114,  113,  139,  138,  164,  163,  189,  188,  214,  213,  239,  238,  264,  263,  289,  288,  314,  313,  339,  338,  364,  363,  389,  388,  414,  413,  439,  438,  464,  463,  489,  488,  514,  513,  539,  538,  564,  563,  589,  588,  614,  613,  612,  611,  587,  586,  562,  561,  537,  536,  512,  511,  487,  486,  462,  461,  437,  436,  412,  411,  387,  386,  362,  361,  337,  336,  312,  311,  287,  286,  262,  261,  237,  236,  212,  211,  187,  186,  162,  161,  137,  136,  112,  111,  87,  86,  62,  61,  37,  36,  12,  11,  10,  9,  35,  34,  60,  59,  85,  84,  110,  109,  135,  134,  160,  159,  185,  184,  210,  209,  235,  234,  260,  259,  285,  284,  310,  309,  335,  334,  360,  359,  385,  384,  410,  409,  435,  434,  460,  459,  485,  484,  510,  509,  535,  534,  560,  559,  585,  584,  610,  609,  608,  607,  583,  582,  558,  557,  533,  532,  508,  507,  483,  482,  458,  457,  433,  432,  408,  407,  383,  382,  358,  357,  333,  332,  308,  307,  283,  282,  258,  257,  233,  232,  208,  207,  183,  182,  158,  157,  133,  132,  108,  107,  83,  82,  58,  57,  33,  32,  8,  7,  5,  4,  30,  29,  55,  54,  80,  79,  105,  104,  130,  129,  155,  154,  180,  179,  205,  204,  230,  229,  255,  254,  280,  279,  305,  304,  330,  329,  355,  354,  380,  379,  405,  404,  430,  429,  455,  454,  480,  479,  505,  504,  530,  529,  555,  554,  580,  579,  605,  604,  603,  602,  578,  577,  553,  552,  528,  527,  503,  502,  478,  477,  453,  452,  428,  427,  403,  402,  378,  377,  353,  352,  328,  327,  303,  302,  278,  277,  253,  252,  228,  227,  203,  202,  178,  177,  153,  152,  128,  127,  103,  102,  78,  77,  53,  52,  28,  27,  3,  2,  1,  0,  26,  25,  51,  50,  76,  75,  101,  100,  126,  125,  151,  150,  176,  175,  201,  200,  226,  225,  251,  250,  276,  275,  301,  300,  326,  325,  351,  350,  376,  375,  401,  400};

    static final short [] datamatrix = new short [625];
    static final short [] datamatrix_buf = new short [625];

    static final byte [] icon_bit_index = {2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,10,10,10,10,10,10,10,10,11,11,11,11,11,11,11,11,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,13,14,14,14,14,14,14,14,14,15,15,15,15,15,15,15,15,16,16,16,16,16,16,16,16,17,17,17,17,17,17,17,17,18,18,18,18,18,18,18,18,19,19,19,19,19,19,19,19,20,20,20,20,20,20,20,20,21,21,21,21,21,21,21,21,22,22,22,22,22,22,22,22,23,23,23,23,23,23,23,23,24,24,24,24,24,24,24,24,25,25,25,25,25,25,25,25,26,26,26,26,26,26,26,26,27,27,27,27,27,27,27,27,28,28,28,28,28,28,28,28,29,29,29,29,29,29,29,29,30,30,30,30,30,30,30,30,31,31,31,31,31,31,31,31,32,32,32,32,32,32,32,32,33,33,33,33,33,33,33,33,34,34,34,34,34,34,34,34,35,35,35,35,35,35,35,35,36,36,36,36,36,36,36,36,37,37,37,37,37,37,37,37,38,38,38,38,38,38,38,38,39,39,39,39,39,39,39,39,40,40,40,40,40,40,40,40,41,41,41,41,41,41,41,41,42,42,42,42,42,42,42,42,43,43,43,43,43,43,43,43,44,44,44,44,44,44,44,44,45,45,45,45,45,45,45,45,46,46,46,46,46,46,46,46,47,47,47,47,47,47,47,47,48,48,48,48,48,48,48,48,49,49,49,49,49,49,49,49,50,50,50,50,50,50,50,50,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,53,53,53,53,53,53,53,53,54,54,54,54,54,54,54,54,55,55,55,55,55,55,55,55,56,56,56,56,56,56,56,56,57,57,57,57,57,57,57,57,58,58,58,58,58,58,58,58,59,59,59,59,59,59,59,59,60,60,60,60,60,60,60,60,61,61,61,61,61,61,61,61,62,62,62,62,62,62,62,62,63,63,63,63,63,63,63,63,64,64,64,64,64,64,64,64,65,65,65,65,65,65,65,65,66,66,66,66,66,66,66,66,67,67,67,67,67,67,67,67,68,68,68,68,68,68,68,68,69,69,69,69,69,69,69,69,70,70,70,70,70,70,70,70,71,71,71,71,71,71,71,71,72,72,72,72,72,72,72,72,73,73,73,73,73,73,73,73,74,74,74,74,74,74,74,74,75,75,75,75,75,75,75,75,76,76,76,76,76,76,76,76,77,77,77,77,77,77,77,77,78,78,78,78,78,78,78,78,79,79,79,79,79,79,79,79,80,80,80,80,80,80,80,80};
    static final byte [] datamat_bitshift = {7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7};
    static final byte [] icon_bytes = new byte[81]; // Holds the final output icon bytes

    static short nsym = 10;
    static short gen [] = new short [11];   //nsym + 1
    static short r [] = new short [12];     //nsym + 2
    static short x = (short)0x1;

    static final short [] packed_msg_pad = {236, 17};   // Short messages are always padded with [0xec, 0x11]
    static final short [] packed_msg = new short[34];   // Version 2 QR max message capacity
    static final short [] encoded_msg = new short[44];  // packed_msg.length+nsym

    // Multiply x and y in GF
    static short gf_mul(short x, short y) {
        if (x == 0 || y==0) {
            return 0;
        } else {
            return gf_exp[(short)(gf_log[x] + gf_log[y])];
        }
    }

    // QR configuration
    static byte QR_mode = (byte)4; // byte mode
    static byte version = 2;
    static short dim = 25;         //(short)(17 + (4*version)); JC fix version

    // Sets icon_bytes class variable
    public static void encode(byte[] content) {

        // Spare this from being written every time a QR is encoded
        icon_bytes[0] = 0x19;                           // Icon width
        icon_bytes[1] = 0x19;                           // Icon height

        // Pack message to required length and in correct mode
        packed_msg[0] = (short)(QR_mode*16&0xff);       // Mode byte
        packed_msg[1] = (short)(content.length*16);     // Length byte
        for(short i = 0; i < content.length; i++) {
            packed_msg[(short)(i+2)] = (short)(content[i]<<4);
        }

        for(short i = 1; i < (short)(content.length+2); i++) {
            packed_msg[(short)(i-1)] += (short)(packed_msg[i]/256);
            packed_msg[i] = (short)(packed_msg[i] % 256);
        }

        for(short i = (short)(content.length+2); i<packed_msg.length; i+=2) {
            packed_msg[i] = packed_msg_pad[0];
            if ((short)(i+1)<packed_msg.length) {
                packed_msg[(short)(i+1)] = packed_msg_pad[1]; 
            }
        }

        // Encode message bytes
        gen[0] = (short)1; //gen = [1]
        short gen_ptr = 1;
        short r_ptr = 2;
        short q [] = new short[2];
        
        for(short i = 0; i < nsym; i++) {                   // for i in range(0, nsym):
            q[0] = (short)1;
            q[1] = gf_exp[i];                               // q = [1, gf_exp[i]]
            r_ptr = (short)(gen_ptr+q.length-1);
            for(short r_idx = 0; r_idx < r_ptr; r_idx++) {  // r = [0] * (len(gen)+len(q)-1) i.e., 3, 5, 7,...
                r[r_idx] = (short)0;
            }
            for(short j = 0; j < q.length; j++) {           // for j in range(0, len(q)):
                for(short k = 0; k < gen_ptr; k++) {        //  for i in range(0, len(gen)): i.e., 1, 3, 7,...
                    r[(short)(k+j)] ^= gf_mul(gen[k], q[j]);         //      r[i+j] ^= gf_mul(gen[i], q[j])
                }
            }
            for(short j = 0; j < r_ptr; j++) { 
                gen[j] = r[j];                          //gen=r
            }
            gen_ptr=r_ptr;
        }

        for(short i = 0; i < encoded_msg.length; i++) {
            encoded_msg[i] = 0;                                 // msg_enc = [0] * (len(msg) + nsym)
        }
        for(short i = 0; i < packed_msg.length; i++) {          // for i in range(0, len(msg)):
            encoded_msg[i] = packed_msg[i];                     //   msg_enc[i] = msg[i]
        }
        short coef;
        for(short i = 0; i < packed_msg.length; i++) {          // for i in range(0, len(msg)):
            coef = encoded_msg[i];                              //   coef = msg_enc[i]
            if(coef != 0) {                                     //   if coef != 0:
                for(short j = 0; j < gen.length; j++) {         //      for j in range(0, len(gen)):
                    encoded_msg[(short)(i+j)] ^= gf_mul(gen[j], coef);   //          -
                }
            }
        }
        
        for(short i = 0; i < packed_msg.length; i++) { 
            encoded_msg[i] = packed_msg[i];
        }

        
        // Copy QR template to working 
        // Preloads finder, timing, alignment and format bits
        //assert(datamatrix.length == v2_template.length);
        //Util.arrayCopy(v2_template, (short)0, datamatrix, (short)0, datamatrix.length);
        for(short i = 0; i < datamatrix.length; i++) {  
            datamatrix[i] = v2_template[i];     
        }

        
        // load the encoded message bit-wise (this should just be computed bit wise)
        // or just process it bit-wise, why even copy stuff around. Just remember to pad the end with 7 0's
        short k = 0;
        for(short i = 0; i < encoded_msg.length; i++) { 
            for(short j = 7; j >= 0; j-=1) { 
                short bit = (byte)(encoded_msg[i]>>j&0x1);
                boolean collision = true;
                while (collision) {
                    collision = !v2_mask_template[v2_mask_cursor[k++]];
                }
                datamatrix[v2_mask_cursor[(short)(k-1)]] = bit;
            }
        }

        
        // Final padding (7 '0' bits)
        //System.out.println('.');
        for(short i = 0; i < 7; i++) { 
            short bit = 0;
            boolean collision = true;
            while (collision) {
                collision = !v2_mask_template[v2_mask_cursor[k++]];
            }
            datamatrix[v2_mask_cursor[(short)(k-1)]] = bit;
        }
        
        // Apply mask pattern
        for(short i = 0; i < dim; i++) {
            for(short j = 0; j < dim; j++) {
                if(v2_mask_template[(short)(i*dim + j)]) {
                    if(v2_mask4_template[(short)(i*dim + j)]) {
                        datamatrix[(short)(i*dim + j)] ^= 1;
                    } else {
                        datamatrix[(short)(i*dim + j)] ^= 0;
                    }
                    
                }
            }
        }
        
        // For each 'byte' (i.e., bit) in datamatrix, write the corresponding bit in icon_bytes
        for(short i = 0; i < datamatrix.length; i++) {
            icon_bytes[icon_bit_index[i]] |= datamatrix[i]<<datamat_bitshift[i];
        }
        
    }

    /*public static void main(String[] args) {

        byte [] msg = {(byte)'h', (byte)'e', (byte)'l', (byte)'l', (byte)'o', (byte)' ', (byte)'c', (byte)'h', (byte)'r', (byte)'i', (byte)'s'};
        encode(msg);
        //System.out.println(Arrays.toString(encoded_content)); 
    }*/
}
