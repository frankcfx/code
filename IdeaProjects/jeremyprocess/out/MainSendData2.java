package com.jeremy.process.multithread;

class Data
{
    public int value = 0;
}

class Work
{
    //... 表示的传入的参数可以随意，你传多少个参数都被放到一个数组里面
    public void process(Data data, Integer ... numbers)
    {
        for (int n : numbers)
        {
            data.value += n;
        }
    }
}

public class MainSendData extends Thread
{
    private Work work;
    public MainSendData(Work work)
    {
        this.work = work;
    }
    public void run()
    {
        java.util.Random random = new java.util.Random();
        Data data = new Data();
        int n1 = random.nextInt(1000);
        int n2 = random.nextInt(2000);
        int n3 = random.nextInt(3000);
        work.process(data, n1, n2, n3); // 使用回调函数
        System.out.println(String.valueOf(n1) + "+" + String.valueOf(n2) + "+"
                + String.valueOf(n3) + "=" + data.value);
    }
    public static void main(String[] args)
    {
        Thread thread = new MainSendData(new Work());
        thread.start();
    }
}