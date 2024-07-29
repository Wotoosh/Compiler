package backend.mipsinstruction;

import backend.BackEnd;

import java.math.BigInteger;

import static java.lang.Math.*;

public class MipsDivInstruction extends MipsInstruction {
    private String left;
    private String right;
    private String target;
    private int num;

    @Override
    public void changeName() {
        if (left != null && left.charAt(0) == 'r') {
            left = left + "_" + counter;
        }
        if (right != null && right.charAt(0) == 'r') {
            right = right + "_" + counter;
        }
        if (target != null && target.charAt(0) == 'r') {
            target = target + "_" + counter;
        }
    }

    public int getNum() {
        return num;
    }

    public void replace(String old, String n) {
        if (left != null && left.equals(old)) {
            left = n;
        }
        if (right != null && right.equals(old)) {
            right = n;
        }
        if (target != null && target.equals(old)) {
            target = n;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MipsDivInstruction that = (MipsDivInstruction) o;

        if (num != that.num) return false;
        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        return right != null ? right.equals(that.right) : that.right == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + num;
        return result;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public String getTarget() {
        return target;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public MipsDivInstruction(String l, String r, String t) {
        left = l;
        right = r;
        target = t;
    }

    public MipsDivInstruction(String l, String t, int nu) {
        left = l;
        num = nu;
        right = null;
        target = t;
    }

    public String buildDivString() {
        StringBuilder sb = new StringBuilder();
        Boolean isNeg = false;
        if (num < 0) {
            isNeg = true;
            num = -num;
        }
        BigInteger base = new BigInteger("2").pow(32);
        BigInteger halfBase = new BigInteger("2").pow(31);
        BigInteger tar = new BigInteger(String.valueOf(num));
        int l = (int) (ceil((log(num) / log(2))));
        int shpost = l;
        BigInteger mlow = new BigInteger("2").pow(32 + l).divide(tar);
        BigInteger mhigh = new BigInteger("2").pow(32 + l);
        mhigh = mhigh.add(new BigInteger("2").pow(32 + l - 31)).divide(tar);
        while (mlow.divide(new BigInteger("2")).compareTo(mhigh.divide(new BigInteger("2"))) < 0 && shpost > 0) {
            mlow = mlow.divide(new BigInteger("2"));
            mhigh = mhigh.divide(new BigInteger("2"));
            shpost--;
        }
        BigInteger tmp = mhigh;
        l = shpost;
        if (tmp.compareTo(halfBase) >= 0) {
            tmp = tmp.subtract(base);
            sb.append("li $at, " + tmp + "\n");
            sb.append("\tmult  $at, " + left + "\n");
            sb.append("\tmfhi $at\n");
            sb.append("\taddu $at," + left + " , $at\n");
        } else {
            sb.append("li $at, " + tmp + "\n");
            sb.append("\tmult  $at, " + left + "\n");
            sb.append("\tmfhi $at\n");
        }
        sb.append("\tsra " + target + " , $at, " + l + "\n");
        if (isNeg) {
            sb.append("\tsubu " + target + " , $zero, " + target + "\n");
        }
        sb.append("\tslti $at, " + left + ", 0\n");
        sb.append("\taddu " + target + " ," + target + " , $at\n");
        return sb.toString();
    }

    public String check2N() {
        int d = abs(num);
        long base = 1;
        int l = -1;
        for (int i = 0; i < 32; i++) {
            if (base == d) {
                l = i;
            }
            base *= 2;
        }
        if (l == -1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (d == 1) {
            sb.append("move " + target + " ,  " + left + "\n");
            if (num < 0) {
                sb.append("\tsubu " + target + " , $zero, " + target + "\n");
            }
            return sb.toString();
        }
        int num1 = l - 1;
        String des = left;
        if (num1 != 0) {
            sb.append("sra $at" + " ,  " + left + ", " + num1 + "\n");
            des = "$at";
        }
        num1 = 32 - l;
        if (num1 != 0) {
            sb.append("\tsrl $at, " + des + " , " + num1 + "\n");
            des = "$at";
        }
        sb.append("\taddu " + target + " , " + des + ", " + left + "\n");
        sb.append("\tsra " + target + " ,  " + target + ", " + l + "\n");
        if (num < 0) {
            sb.append("\tsubu " + target + " , $zero, " + target + "\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        if (right == null) {
            if (BackEnd.isOpt) {
                if (check2N() != null) {
                    return check2N();
                }
                return buildDivString();
            } else {
                return "div " + target + ", " + left + ", " + num + "\n";
            }
        } else {
            return "div " + target + ", " + left + ", " + right + "\n";
        }
    }


}
