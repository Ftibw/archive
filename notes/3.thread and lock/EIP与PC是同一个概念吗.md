###程序计数器PC和寄存器EIP有什么关系吗，为什么它们都是存放下一条指令的地址？ 

先明白定义再说区别和原理：

 1、[程序存储器](https://www.baidu.com/s?wd=%E7%A8%8B%E5%BA%8F%E5%AD%98%E5%82%A8%E5%99%A8&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)(program storage) 在计算机的[主存储器](https://www.baidu.com/s?wd=%E4%B8%BB%E5%AD%98%E5%82%A8%E5%99%A8&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)中专门用来存放程序、子程序的一个区域。

 2、指令寄存器（IR ）：用来保存当前正在执行的一条指令。当执行一条指令时，先把它从内存取到数据寄存器（DR）中，然后再传送至IR。指令划分为操作码和地址码字段，由[二进制数字](https://www.baidu.com/s?wd=%E4%BA%8C%E8%BF%9B%E5%88%B6%E6%95%B0%E5%AD%97&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)组成。为了执行任何给定的指令，必须对操作码进行测试，以便识别所要求的操作。指令译码器就是做这项工作的。指令寄存器中操作码字段的输出就是指令译码器的输入。操作码一经译码后，即可向[操作控制器](https://www.baidu.com/s?wd=%E6%93%8D%E4%BD%9C%E6%8E%A7%E5%88%B6%E5%99%A8&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)发出具体操作的特定信号。 

3、[程序计数器](https://www.baidu.com/s?wd=%E7%A8%8B%E5%BA%8F%E8%AE%A1%E6%95%B0%E5%99%A8&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)（PC）：为了保证程序(在[操作系统](https://www.baidu.com/s?wd=%E6%93%8D%E4%BD%9C%E7%B3%BB%E7%BB%9F&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)中理解为进程)能够连续地执行下去，CPU必须具有某些手段来确定下一条指令的地址。而[程序计数器](https://www.baidu.com/s?wd=%E7%A8%8B%E5%BA%8F%E8%AE%A1%E6%95%B0%E5%99%A8&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)正是起到这种作用，所以通常又称为指令计数器。在程序开始执行前，必须将它的起始地址，即程序的一条指令所在的内存单元地址送入PC，因此[程序计数器](https://www.baidu.com/s?wd=%E7%A8%8B%E5%BA%8F%E8%AE%A1%E6%95%B0%E5%99%A8&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)（PC）的内容即是从内存提取的第一条指令的地址。当执行指令时，CPU将自动修改PC的内容，即每执行一条指令PC增加一个量，这个量等于指令所含的字节数，以便使其保持的总是将要执行的下一条指令的地址。由于大多数指令都是按顺序来执行的，所以修改的过程通常只是简单的对PC加1。 当程序转移时，转移指令执行的最终结果就是要改变PC的值，此PC值就是转去的地址，以此实现转移。有些机器中也称PC为指令指针IP（Instruction Pointer）

 4、地址寄存器：用来保存当前CPU所访问的内存单元的地址。由于在内存和CPU之间存在着操作速度上的差别，所以必须使用地址寄存器来保持地址信息，直到内存的读/写操作完成为止 。 当CPU和内存进行[信息交换](https://www.baidu.com/s?wd=%E4%BF%A1%E6%81%AF%E4%BA%A4%E6%8D%A2&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)，即CPU向内存存/取数据时，或者CPU从内存中读出指令时，都要使用地址寄存器和数据缓冲寄存器。同样，如果我们把[外围设备](https://www.baidu.com/s?wd=%E5%A4%96%E5%9B%B4%E8%AE%BE%E5%A4%87&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)的设备地址作为像内存的地址单元那样来看待，那么，当CPU和[外围设备](https://www.baidu.com/s?wd=%E5%A4%96%E5%9B%B4%E8%AE%BE%E5%A4%87&tn=SE_PcZhidaonwhc_ngpagmjz&rsv_dl=gh_pc_zhidao)交换信息时，我们同样使用 地址寄存器和数据缓冲寄存器 基本上定义就是区别和应用 

```
1.
EIP 是寄存器名称
每个芯片的pc寄存器不一样
2.
8086中的EIP就相当于你说的PC了
```

