/*
 * This is the source code of the Hydrogen Atom Orbitals app for Android.
 * It is licensed under the MIT License.
 *
 * Copyright (c) 2015-2018 Volodymyr Vovchenko.
 */

package com.vlvolad.hydrogenatom;

/**
 * Created by Volodymyr on 28.04.2015.
 */
public class Poly {
    public int st;
    public double [] c;
    public Poly() {
        st = 0;
        c = new double[1];
        c[0] = 0;
    }
    public Poly(Poly op) {
        this.st = op.st;
        this.c = new double[op.st+1];
        for(int i=0;i<st+1;++i)
            this.c[i] = op.c[i];
    }
    public double F(double r)
    {
        double ret = 0;
        for(int i=st;i>=1;i--)
            ret = (ret + c[i])*r;
        ret += c[0];
        return ret;
    }
    public void multiply(double mn)
    {
        for(int i=0;i<st+1;++i) c[i] *= mn;
    }
    public void multiplyxs(int s) {
        Poly tmp = new Poly(this);
        st += s;
        double cc[] = new double[st+1];
        for(int i=0;i<st-s+1;++i) cc[i+s] = c[i];
        for(int i=0;i<s;++i) cc[i] = 0.;
        c = cc;
    }
    public void derivative(int n) {
        if (n>st)
        {
            st = 0;
            c = new double[1];
            c[0] = 0;
            return;
        }
        for(int i=0;i<n;++i)
        {
            for(int j=0;j<st;++j)
                c[j] = c[j+1]*(j+1);
            st--;
        }
    }
    public static Poly mult(final Poly p1, final Poly p2) {
        Poly ret = new Poly();
        ret.st = p1.st+p2.st;
        ret.c = new double[ret.st+1];
        for(int i=0;i<ret.st+1;++i)
            ret.c[i] = 0;
        for(int i=0;i<p1.st+1;++i)
            for(int j=0;j<p2.st+1;++j)
                ret.c[i+j] += p1.c[i]*p2.c[j];
        return ret;
    }
    public static Poly Laguerre(int k) {
        Poly ret = new Poly();
        Poly tmp = new Poly();
        ret.st = k;
        ret.c = new double[k+1];
        for(int i=0;i<k;++i) ret.c[i] = 0;
        ret.c[k] = 1;
        for(int i=0;i<k;++i)
        {
            tmp = ret;
            for(int j=0;j<tmp.st+1;++j)
                tmp.c[j] = 0;
            for(int j=0;j<i+1;++j)
            {
                tmp.c[k-j] += -ret.c[k-j];
                tmp.c[k-j-1] += (k-j)*ret.c[k-j];
            }
            ret = tmp;
        }
        return ret;
    }
}
