```
+---------------------------------------------------------+
|                    CSV Data Processor                   |
+---------------------------------------------------------+

                  +---------------------+
                  |   com.group5.csv.io |
                  |     (I/O layer)     |
                  +----------+----------+
                             |
                             | produces raw cells (List<String>)
                             v
                  +----------------------+
                  |  RowBuilder          |
                  |  (mutable helper)    |
                  +----------+-----------+
                             |
                             | builds immutable rows
                             v
      +-------------------------+       +----------------------+
      |         Row             | <---> |       Headers        |
      | - headers: Headers      |       | - names: List<String>|
      | - values:  List<Field>  |       | - index lookup       |
      +-----------+-------------+       +----------+-----------+
                  |                                ^
                  | accesses fields                |
                  v                                |
          +--------------------+                   |
          |       Field        |-------------------+
          | - raw, value, etc. |
          | - type: FieldType  |
          +---------+----------+
                    |
                    | uses parsing/formatting rules
                    v
               +-----------+
               | FieldType |
               |  (enum)   |
               +-----------+
```
High-level flow:
1. CsvReader parses a line into a List<String>.
2. RowBuilder accumulates those values against a Headers instance.
3. RowBuilder.build() creates an immutable Row.
4. Each cell is accessed via Field/FieldType for typed parsing & validation.
