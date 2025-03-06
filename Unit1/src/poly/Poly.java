package poly;

import processString.ToString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Poly {
    private HashSet<Mono> monos;

    public List<Mono> getMonos() {
        ArrayList<Mono> sortedMonos = new ArrayList<>(this.monos);
        // 把正数项放在前面
        for (int i = 0; i < sortedMonos.size(); i++) {
            if (sortedMonos.get(i).getCoe().compareTo(BigInteger.ZERO) > 0) {
                Mono temp = sortedMonos.get(i);
                sortedMonos.set(i, sortedMonos.get(0));
                sortedMonos.set(0, temp);
            }
        }
        return sortedMonos;
    }

    public void setMonos(List<Mono> monos) {
        this.monos = new HashSet<>(monos);
    }

    public Poly() {
        this.monos = new HashSet<>();
    }

    public void addMono(Mono mono) {
        // TODO: Mono.negate()

        if (mono.isZero()) {
            return;
        }
        
        Mono existingMono = null;
        Mono toOneMono = null;
        for (Mono m : this.monos) {
            if (m.equals(mono)) {
                existingMono = m;
                break;
            } else if (m.toOne(mono)) {
                toOneMono = m;
            }
        }


        
        if (existingMono != null) {
            Add.monoAdd(existingMono, mono);
            if (existingMono.isZero()) {
                this.monos.remove(existingMono);
            }
        } else {
            if (toOneMono != null) {
                // 如果可以化简，那删掉符合条件的mono，补一个1
                this.monos.remove(toOneMono);
                Poly onePoly = new Poly();
                onePoly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
                Add.polyAdd(this, onePoly);
            }
            else {
                this.monos.add(mono.copy());
            }
            // 以前的逻辑，如果去除cos^2+sin^2优化，将其加回来
//            this.monos.add(mono.copy());
        }
    }

    public Poly copy() {
        Poly poly = new Poly();
        for (Mono mono : monos) {
            poly.addMono(mono.copy());
        }
        return poly;
    }

    public String toString() {
        return ToString.polyToString(this);
    }

    // private boolean allZero() {
    //     if (monos.isEmpty()) {
    //         return true;
    //     }
    //     for (Mono mono : monos) {
    //         if (!mono.isZero()) {
    //             return false;
    //         }
    //     }
    //     return true;
    // }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        Poly poly = (Poly) o;
        
        // 如果两个多项式的单项式数量不同，它们不相等
        if (this.monos.size() != poly.monos.size()) {
            return false;
        }
        
        // 检查每个单项式是否都存在于另一个多项式中
        for (Mono mono : this.monos) {
            boolean found = false;
            for (Mono otherMono : poly.monos) {
                if (mono.equals(otherMono) && 
                    mono.getCoe().equals(otherMono.getCoe())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        for (Mono mono : this.monos) {
            // 使用单项式的 hashCode 和系数计算哈希值
            result = 31 * result + mono.hashCode();
            result = 31 * result + (mono.getCoe() != null ? mono.getCoe().hashCode() : 0);
        }
        return result;
    }
    
    /**
     * 返回多项式的负形式
     * 用于三角函数同类项的合并
     * @return 多项式的负形式
     */
    public Poly negative() {
        Poly result = new Poly();
        for (Mono mono : this.monos) {
            Mono negMono = mono.copy();
            negMono.setCoe(mono.getCoe().negate());
            result.addMono(negMono);
        }
        return result;
    }

}
