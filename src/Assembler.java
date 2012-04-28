//
//  SNARL/ASSEMBLER. Write MIPS assembly instructions to a text file.
//
//    James Moen
//    30 Jan 12
//

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//  ASSEMBLER. Acts like an output stream that allows writing MIPS instructions
//  to a text file.

class Assembler
{
  private static final int         labelWidth = 12;  //  Columns per LABEL.
  private              PrintWriter writer;           //  Instructions go here.

//  Constructor. Open the file whose pathname is PATH so we can write assembler
//  instructions to it.

  public Assembler(String path)
  {
    try
    {
      writer = new PrintWriter(new FileWriter(path));
    }
    catch (IOException ignore)
    {
      System.out.println("Cannot open '" + path + "'.");
      System.exit(1);
    }
  }

//  CHECK. If RI is not used, then throw an exception.

  private void check(Allocator.Register Ri)
  {
    if (! Ri.isUsed())
    {
      throw new IllegalStateException("'" + Ri + "' is not used.");
    }
  }

//  CLOSE. Close the stream WRITER. This must be called before SNARL halts!

  public void close()
  {
    writer.close();
  }

//  PLACE. Write the LABEL L followed by a colon, using LABEL WIDTH columns. If
//  L does not appear, then just write LABEL WIDTH blanks.

  private void place()
  {
    int count = labelWidth;
    while (count > 0)
    {
      writer.print(" ");
      count -= 1;
    }
  }

  private void place(Label L)
  {
    L.place();
    writer.print(L + ": ");
    int count = labelWidth - L.toString().length() - 2;
    while (count > 0)
    {
      writer.print(" ");
      count -= 1;
    }
  }

//  EMIT. Write a MIPS instruction to WRITER. There are many ways to call EMIT,
//  shown in the table below. OP is a STRING that is the name of an opcode or a
//  pseudoinstruction. The L's are LABELs, the R's are REGISTERs, and IMM is an
//  integer.
//             ____________________________________________________
//            |                             |                      |
//            |  Call to EMIT               |  MIPS instruction    |
//            |_____________________________|______________________|
//            |                             |                      |
//            |  emit(op)                   |      op              |
//            |  emit(op, L)                |      op L            |
//            |  emit(op, Ri)               |      op Ri           |
//            |  emit(op, Ri, L)            |      op Ri, L        |
//            |  emit(op, Ri, imm)          |      op Ri, imm      |
//            |  emit(op, Ri, Rj)           |      op Ri, Rj       |
//            |  emit(op, Ri, Rj, L)        |      op Ri, Rj, L    |
//            |  emit(op, Ri, Rj, Rk)       |      op Ri, Rj, Rk   |
//            |  emit(op, Ri, Rj, imm)      |      op Ri, Rj, imm  |
//            |  emit(op, Ri, imm, Rj)      |      op Ri, imm(Rj)  |
//            |                             |                      |
//            |  emit(L)                    |  L:                  |
//            |  emit(L,  op)               |  L:  op              |
//            |  emit(L1, op, L2)           |  L1: op L2           |
//            |  emit(L,  op, Ri)           |  L:  op Ri           |
//            |  emit(L1, op, Ri, L2)       |  L1: op Ri, L2       |
//            |  emit(L,  op, Ri, imm)      |  L:  op Ri, imm      |
//            |  emit(L,  op, Ri, Rj)       |  L:  op Ri, Rj       |
//            |  emit(L1, op, Ri, Rj, L2)   |  L1: op Ri, Rj, L2   |
//            |  emit(L,  op, Ri, Rj, Rk    |  L:  op Ri, Rj, Rk   |
//            |  emit(L,  op, Ri, Rj, imm)  |  L:  op Ri, Rj, imm  |
//            |  emit(L,  op, Ri, imm, Rj)  |  L:  op Ri, imm(Rj)  |
//            |_____________________________|______________________|
//
//  Note that the version of EMIT that takes only a STRING argument can be used
//  to write arbitrary text to WRITER.

  public void emit(String op)
  {
    place();
    writer.println(op);
  }

  public void emit(String op, Label L)
  {
    place();
    writer.println(op + " " + L);
  }

  public void emit(String op, Allocator.Register Ri)
  {
    check(Ri);
    place();
    writer.println(op + " " + Ri);
  }

  public void emit(String op, Allocator.Register Ri, Label L)
  {
    check(Ri);
    place();
    writer.println(op + " " + Ri + ", " + L);
  }

  public void emit(String op, Allocator.Register Ri, int imm)
  {
    check(Ri);
    place();
    writer.println(op + " " + Ri + ", " + imm);
  }

  public void emit(String op, Allocator.Register Ri, Allocator.Register Rj)
  {
    check(Ri);
    check(Rj);
    place();
    writer.println(op + " " + Ri + ", " + Rj);
  }

  public void emit(
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj,
    Label L)
  {
    check(Ri);
    check(Rj);
    place();
    writer.println(op + " " + Ri + ", " + Rj + ", " + L);
  }

  public void emit(
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj,
    Allocator.Register Rk)
  {
    check(Ri);
    check(Rj);
    check(Rk);
    place();
    writer.println(op + " " + Ri + ", " + Rj + ", " + Rk);
  }

  public void emit(
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj,
    int imm)
  {
    check(Ri);
    check(Rj);
    place();
    writer.println(op + " " + Ri + ", " + Rj + ", " + imm);
  }

  public void emit(
    String op,
    Allocator.Register Ri,
    int imm,
    Allocator.Register Rj)
  {
    check(Ri);
    check(Rj);
    place();
    writer.println(op + " " + Ri + ", " + imm + "(" + Rj + ")");
  }

  public void emit(Label L)
  {
    place(L);
    writer.println();
  }

  public void emit(Label L, String op)
  {
    place(L);
    writer.println(op);
  }

  public void emit(Label L1, String op, Label L2)
  {
    place(L1);
    writer.println(op + " " + L2);
  }

  public void emit(Label L, String op, Allocator.Register Ri)
  {
    check(Ri);
    place(L);
    writer.println(op + " " + Ri);
  }

  public void emit(Label L1, String op, Allocator.Register Ri, Label L2)
  {
    check(Ri);
    place(L1);
    writer.println(op + " " + Ri + ", " + L2);
  }

  public void emit(Label L, String op, Allocator.Register Ri, int imm)
  {
    check(Ri);
    place(L);
    writer.println(op + " " + Ri + ", " + imm);
  }

  public void emit(
    Label L,
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj)
  {
    check(Ri);
    check(Rj);
    place(L);
    writer.println(op + " " + Ri + ", " + Rj);
  }

  public void emit(
    Label L1,
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj,
    Label L2)
  {
    check(Ri);
    check(Rj);
    place(L1);
    writer.println(op + " " + Ri + ", " + Rj + ", " + L2);
  }

  public void emit(
    Label L,
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj,
    Allocator.Register Rk)
  {
    check(Ri);
    check(Rj);
    check(Rk);
    place(L);
    writer.println(op + " " + Ri + ", " + Rj + ", " + Rk);
  }

  public void emit(
    Label L,
    String op,
    Allocator.Register Ri,
    Allocator.Register Rj,
    int imm)
  {
    check(Ri);
    check(Rj);
    place(L);
    writer.println(op + " " + Ri + ", " + Rj + ", " + imm);
  }

  public void emit(
    Label L,
    String op,
    Allocator.Register Ri,
    int imm,
    Allocator.Register Rj)
  {
    check(Ri);
    place(L);
    writer.println(op + " " + Ri + ", " + imm + "(" + Rj + ")");
  }
}
