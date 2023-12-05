package zombie.scripting;

import java.util.ArrayList;
import java.util.Iterator;

public final class ScriptParser {
    private static StringBuilder stringBuilder = new StringBuilder();

    public ScriptParser() {
    }

    public static int readBlock(String blockStr, int auxVar, Block block) {
        int i;
        for(i = auxVar; i < blockStr.length(); ++i) {
            if (blockStr.charAt(i) == '{') {
                Block block1 = new Block();
                block.children.add(block1);
                block.elements.add(block1);
                String auxStr = blockStr.substring(auxVar, i).trim();
                String[] strArr = auxStr.split("\\s+");
                block1.type = strArr[0];
                block1.id = strArr.length > 1 ? strArr[1] : null;
                i = readBlock(blockStr, i + 1, block1);
                auxVar = i;
            } else {
                if (blockStr.charAt(i) == '}') {
                    return i + 1;
                }

                if (blockStr.charAt(i) == ',') {
                    Value var7 = new Value();
                    var7.string = blockStr.substring(auxVar, i);
                    block.values.add(var7);
                    block.elements.add(var7);
                    auxVar = i + 1;
                }
            }
        }

        return i;
    }

    public static Block parse(String var0) {
        Block block = new Block();
        readBlock(var0, 0, block);
        return block;
    }

    public static String stripComments(String var0) {
        stringBuilder.setLength(0);
        stringBuilder.append(var0);

        int var2;
        for(int var1 = stringBuilder.lastIndexOf("*/"); var1 != -1; var1 = stringBuilder.lastIndexOf("*/", var2)) {
            var2 = stringBuilder.lastIndexOf("/*", var1 - 1);
            if (var2 == -1) {
                break;
            }

            int var4;
            for(int var3 = stringBuilder.lastIndexOf("*/", var1 - 1); var3 > var2; var3 = stringBuilder.lastIndexOf("*/", var4 - 2)) {
                var4 = var2;
                var2 = stringBuilder.lastIndexOf("/*", var2 - 2);
                if (var2 == -1) {
                    break;
                }
            }

            if (var2 == -1) {
                break;
            }

            stringBuilder.replace(var2, var1 + 2, "");
        }

        var0 = stringBuilder.toString();
        stringBuilder.setLength(0);
        return var0;
    }

    public static ArrayList<String> parseTokens(String var0) {
        ArrayList var1 = new ArrayList();

        while(true) {
            int var2 = 0;
            int var3 = 0;
            int var4 = 0;
            if (var0.indexOf("}", var3 + 1) == -1) {
                if (var0.trim().length() > 0) {
                    var1.add(var0.trim());
                }

                return var1;
            }

            do {
                var3 = var0.indexOf("{", var3 + 1);
                var4 = var0.indexOf("}", var4 + 1);
                if ((var4 >= var3 || var4 == -1) && var3 != -1) {
                    var4 = var3;
                    ++var2;
                } else {
                    var3 = var4;
                    --var2;
                }
            } while(var2 > 0);

            var1.add(var0.substring(0, var3 + 1).trim());
            var0 = var0.substring(var3 + 1);
        }
    }

    public static class Block implements BlockElement {
        public String type;
        public String id;
        public final ArrayList<BlockElement> elements = new ArrayList();
        public final ArrayList<Value> values = new ArrayList();
        public final ArrayList<Block> children = new ArrayList();

        public Block() {
        }

        public Block asBlock() {
            return this;
        }

        public Value asValue() {
            return null;
        }

        public boolean isEmpty() {
            return this.elements.isEmpty();
        }

        public void prettyPrint(int var1, StringBuilder strB, String str) {
            int i;
            for(i = 0; i < var1; ++i) {
                strB.append('\t');
            }

            strB.append(this.type);
            if (this.id != null) {
                strB.append(" ");
                strB.append(this.id);
            }

            strB.append(str);

            for(i = 0; i < var1; ++i) {
                strB.append('\t');
            }

            strB.append('{');
            strB.append(str);
            this.prettyPrintElements(var1 + 1, strB, str);

            for(i = 0; i < var1; ++i) {
                strB.append('\t');
            }

            strB.append('}');
            strB.append(str);
        }

        public void prettyPrintElements(int var1, StringBuilder var2, String var3) {
            BlockElement var4 = null;

            BlockElement var6;
            for(Iterator var5 = this.elements.iterator(); var5.hasNext(); var4 = var6) {
                var6 = (BlockElement)var5.next();
                if (var6.asBlock() != null && var4 != null) {
                    var2.append(var3);
                }

                if (var6.asValue() != null && var4 instanceof Block) {
                    var2.append(var3);
                }

                var6.prettyPrint(var1, var2, var3);
            }

        }

        public Block addBlock(String type, String id) {
            Block block = new Block();
            block.type = type;
            block.id = id;
            this.elements.add(block);
            this.children.add(block);
            return block;
        }

        public Block getBlock(String type, String id) {
            Iterator var3 = this.children.iterator();

            Block block;
            do {
                do {
                    if (!var3.hasNext()) {
                        return null;
                    }

                    block = (Block)var3.next();
                } while(!block.type.equals(type));
            } while((block.id == null || !block.id.equals(id)) && (block.id != null || id != null));

            return block;
        }

        public Value getValue(String auxKey) {
            Iterator var2 = this.values.iterator();

            Value var3;
            int var4;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                var3 = (Value)var2.next();
                var4 = var3.string.indexOf(61);
            } while(var4 <= 0 || !var3.getKey().trim().equals(auxKey));

            return var3;
        }

        public void setValue(String var1, String var2) {
            Value var3 = this.getValue(var1);
            if (var3 == null) {
                this.addValue(var1, var2);
            } else {
                var3.string = var1 + " = " + var2;
            }

        }

        public Value addValue(String var1, String var2) {
            Value var3 = new Value();
            var3.string = var1 + " = " + var2;
            this.elements.add(var3);
            this.values.add(var3);
            return var3;
        }

        public void moveValueAfter(String var1, String var2) {
            Value var3 = this.getValue(var1);
            Value var4 = this.getValue(var2);
            if (var3 != null && var4 != null) {
                this.elements.remove(var3);
                this.values.remove(var3);
                this.elements.add(this.elements.indexOf(var4) + 1, var3);
                this.values.add(this.values.indexOf(var4) + 1, var3);
            }
        }
    }

    public static class Value implements BlockElement {
        public String string;

        public Value() {
        }

        public Block asBlock() {
            return null;
        }

        public Value asValue() {
            return this;
        }

        public void prettyPrint(int var1, StringBuilder var2, String var3) {
            for(int var4 = 0; var4 < var1; ++var4) {
                var2.append('\t');
            }

            var2.append(this.string.trim());
            var2.append(',');
            var2.append(var3);
        }

        public String getKey() {
            int var1 = this.string.indexOf(61);
            return var1 == -1 ? this.string : this.string.substring(0, var1);
        }

        public String getValue() {
            int var1 = this.string.indexOf(61);
            return var1 == -1 ? "" : this.string.substring(var1 + 1);
        }
    }

    public interface BlockElement {
        Block asBlock();

        Value asValue();

        void prettyPrint(int var1, StringBuilder var2, String var3);
    }
}