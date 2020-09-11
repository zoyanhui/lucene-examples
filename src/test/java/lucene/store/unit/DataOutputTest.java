package lucene.store.unit;

import java.io.IOException;

import org.apache.lucene.store.DataOutput;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhouyanhui3 on 19-4-15.
 */
public class DataOutputTest {

  static class DataOutputDisp extends DataOutput {

    private StringBuilder buffer;

    DataOutputDisp() {
      buffer = new StringBuilder();
    }

    public void reset() {
      buffer = new StringBuilder();
    }

    private String toBinaryString(byte b){
      return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    private String toBinaryString(int i){
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(toBinaryString((byte)(i & 0xFF)));
      i >>>= 8;
      stringBuilder.append(toBinaryString((byte)(i & 0xFF)));
      i >>>= 8;
      stringBuilder.append(toBinaryString((byte)(i & 0xFF)));
      i >>>= 8;
      stringBuilder.append(toBinaryString((byte)(i & 0xFF)));
      return stringBuilder.toString();
    }

    private String toBinaryStringOrderLowerToHigher(int i){
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(toBinaryString((byte)((i >>> 24) & 0xFF)));
      stringBuilder.append(toBinaryString((byte)((i >>> 16) & 0xFF)));
      stringBuilder.append(toBinaryString((byte)((i >>> 8) & 0xFF)));
      stringBuilder.append(toBinaryString((byte)((i) & 0xFF)));
      return stringBuilder.toString();
    }

    @Override
    public void writeByte(byte b) throws IOException {
      String binary = toBinaryString(b);
      buffer.append(binary);
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
      if (offset + length > b.length) {
        throw new IOException("byte array out of bound");
      }
      for (int i = 0; i < length; i++) {
        writeByte(b[i + offset]);
      }
    }

    public String display() {
      return buffer.toString();
    }
  }

  @Test
  public void writeLess_128() throws IOException {
    DataOutputDisp dataOutputDisp = new DataOutputDisp();
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(0);
    String disp = dataOutputDisp.display();
    Assert.assertEquals("00000000", disp);
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(1);
    disp = dataOutputDisp.display();
    Assert.assertEquals("00000001", disp);
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(127);
    disp = dataOutputDisp.display();
    Assert.assertEquals("01111111", disp);
  }

  @Test
  public void writeGreat_128() throws IOException {
    DataOutputDisp dataOutputDisp = new DataOutputDisp();
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(128);
    String disp = dataOutputDisp.display();
    Assert.assertEquals("1000000000000001", disp);
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(129);
    disp = dataOutputDisp.display();
    Assert.assertEquals("1000000100000001", disp);
  }

  @Test
  public void writeNegative() throws IOException {
    DataOutputDisp dataOutputDisp = new DataOutputDisp();
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(-1);
    String disp = dataOutputDisp.display();
    Assert.assertEquals("1111111111111111111111111111111100001111", disp);
//    System.out.println(disp);
    decode(disp);
    Assert.assertEquals(-1, decode(disp));
    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(-2);
    disp = dataOutputDisp.display();
    Assert.assertEquals("1111111011111111111111111111111100001111", disp);
//    System.out.println(disp);
    Assert.assertEquals(-2, decode(disp));

    dataOutputDisp.reset();
    dataOutputDisp.writeVInt(Integer.MIN_VALUE);
    disp = dataOutputDisp.display();
//    System.out.println(disp);
    Assert.assertEquals("1000000010000000100000001000000000001000", disp);
    Assert.assertEquals(Integer.MIN_VALUE, decode(disp));
  }


  private int decode(String disp) {
    int value = 0;
    for (int i = disp.length(); i>=8 ; i-=8) {
      String substr = disp.substring(i-7, i);
      byte b = Byte.parseByte(substr, 2);
      value = value << 7;
      value |= b;
    }
    return value;
  }

//  @Test
  public void test(){
//    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(-2));
//    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(-2 >> 31));
//    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(-2 << 1));
//    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher((-2 >> 31) ^ (-2 << 1) ));
    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(-(1)));

    int a = -2;// Integer.MIN_VALUE;
    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(a));
    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(a >> 31));
    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher(a << 1));
    System.out.println(new DataOutputDisp().toBinaryStringOrderLowerToHigher((a >> 31) ^ (a << 1) ));
  }
}
