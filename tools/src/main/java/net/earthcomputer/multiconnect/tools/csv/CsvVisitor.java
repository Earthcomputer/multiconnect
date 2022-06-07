package net.earthcomputer.multiconnect.tools.csv;

import java.util.List;

public abstract class CsvVisitor {
    public void visitHeader(List<String> keys) {}
    public void visitRow(List<String> row) {}
}
