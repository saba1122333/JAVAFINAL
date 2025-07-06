/*
 * Decompiled with CFR 0.152.
 */
package Model;

public class Clock {
    private int hh;
    private int mm;
    private int ss;

    public Clock(int n, int n2, int n3) {
        this.hh = Math.max(0, n);
        this.mm = Math.max(0, Math.min(59, n2));
        this.ss = Math.max(0, Math.min(59, n3));
    }

    public boolean outOfTime() {
        return this.hh == 0 && this.mm == 0 && this.ss == 0;
    }

    public void decr() {
        if (this.outOfTime()) {
            return;
        }
        if (this.mm == 0 && this.ss == 0) {
            this.ss = 59;
            this.mm = 59;
            --this.hh;
        } else if (this.ss == 0) {
            this.ss = 59;
            --this.mm;
        } else {
            --this.ss;
        }
    }

    public String getTime() {
        String string = String.format("%02d", this.hh);
        String string2 = String.format("%02d", this.mm);
        String string3 = String.format("%02d", this.ss);
        String string4 = string + ":" + string2 + ":" + string3;
        return string4;
    }

    public int getHours() {
        return this.hh;
    }

    public int getMinutes() {
        return this.mm;
    }

    public int getSeconds() {
        return this.ss;
    }

    public void addTime(int n) {
        int n2 = this.toTotalSeconds() + n;
        this.fromTotalSeconds(n2);
    }

    private int toTotalSeconds() {
        return this.hh * 3600 + this.mm * 60 + this.ss;
    }

    private void fromTotalSeconds(int n) {
        n = Math.max(0, n);
        this.hh = n / 3600;
        this.mm = (n %= 3600) / 60;
        this.ss = n % 60;
    }
}
