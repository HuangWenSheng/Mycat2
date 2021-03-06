package cn.lightfish.sqlEngine.ast.expr.booleanExpr.compareExpr;

import cn.lightfish.sqlEngine.context.RootSessionContext;
import cn.lightfish.sqlEngine.ast.expr.booleanExpr.BooleanExpr;
import cn.lightfish.sqlEngine.ast.expr.ValueExpr;
import java.util.Objects;

public class BooleanEqualityExpr implements BooleanExpr {

  private final RootSessionContext context;
  private final ValueExpr left;
  private final ValueExpr right;

  public BooleanEqualityExpr(RootSessionContext context, ValueExpr left,
      ValueExpr right) {
    this.context = context;
    this.left = left;
    this.right = right;
  }

  @Override
  public Boolean test() {
    Object leftValue = left.getValue();
    Object rightValue = right.getValue();
    if (leftValue == null) {
      return null;
    }
    if (rightValue == null) {
      return null;
    }
    return  Objects.equals(leftValue, rightValue);
  }
}