package me.friwi.arterion.plugin.combat.hook;

import java.util.Objects;

public class TriTuple<T, S, R> {
    private T firstValue;
    private S secondValue;
    private R thirdValue;

    public TriTuple(T firstValue, S secondValue, R thirdValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
        this.thirdValue = thirdValue;
    }

    public T getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(T firstValue) {
        this.firstValue = firstValue;
    }

    public S getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(S secondValue) {
        this.secondValue = secondValue;
    }

    public R getThirdValue() {
        return thirdValue;
    }

    public void setThirdValue(R thirdValue) {
        this.thirdValue = thirdValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriTuple<?, ?, ?> triTuple = (TriTuple<?, ?, ?>) o;
        return Objects.equals(firstValue, triTuple.firstValue) &&
                Objects.equals(secondValue, triTuple.secondValue) &&
                Objects.equals(thirdValue, triTuple.thirdValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstValue, secondValue, thirdValue);
    }

    @Override
    public String toString() {
        return "TriTuple{" +
                "firstValue=" + firstValue +
                ", secondValue=" + secondValue +
                ", thirdValue=" + thirdValue +
                '}';
    }
}
