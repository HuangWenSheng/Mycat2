package cn.lightfish.sqlEngine.persistent;

import cn.lightfish.sqlEngine.schema.DbTable;
import cn.lightfish.sqlEngine.schema.TableColumnDefinition;

import java.util.Iterator;

public class QueryPersistentImpl implements QueryPersistent {

  private final DbTable table;
  private final Iterator<Object[]> rows;


  public QueryPersistentImpl(DbTable table, Iterator<Object[]> rows) {
    this.table = table;
    this.rows = rows;
  }

  @Override
  public TableColumnDefinition[] columnDefList() {
    return table.getColumnDefinitions();
  }

  @Override
  public boolean hasNext() {
    return rows.hasNext();
  }

  @Override
  public Object[] next() {
    return rows.next();
  }
}