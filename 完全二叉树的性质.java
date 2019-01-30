完全二叉树:

1 2 4 8...等比数列而已
第k层最多有2^(k-1)次方个结点

k项等比数列求和 a1(1-q^k)/(1-q)
当a1=1,q=2时有1*(1-2^k)/(1-2)=2^k-1

深度为K时,结点最多有2^k-1个
 
 设度为2的结点个数为n2,k-1层中度为2的结点个数为d2,则n2等于k-1层数之前的所有结点个数加上d2
 即如下:
 n2=2^(k-2)-1+d2		（1）
 
 设度为1的结点个数为n1(度为1的结点必定只在k-1层),设度为0的结点个数为n0,
 则n0等于k-1层中度为2的结点个数为d2的2倍,加上k-1层中度为1的结点个数,再加上k-1层中度为0的结点个数
 
 n0=2*d2+n1+[2^(k-2)-d2-n1]
 
 ==>n0=2^(k-2)+d2		（2）
 
 结合（1）（2）得
 
 n0=n2+1
 
 即完全二叉树中度为0的结点个数等于度为2的结点个数加1