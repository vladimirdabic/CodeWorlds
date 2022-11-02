package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemValue implements Value, ValueProperties {
    private final ItemStack itemStack;

    public ItemValue(final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public String getTypeName() {
        return "Item";
    }

    @Override
    public String toStringValue() {
        return "<Item id=" + itemStack.getType() + ">";
    }

    @Override
    public boolean equals(Value other) {
        if(other instanceof ItemValue) return itemStack.equals(((ItemValue)other).getBukkitItem());
        return false;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public Value getProperty(String propertyName) {
        ItemValue itemValue = this;
        switch (propertyName) {
            case "id": return new StringValue(itemStack.getType().toString());
            case "amount": return new NumberValue(itemStack.getAmount());
            case "unbreakable": return new BooleanValue(Objects.requireNonNull(itemStack.getItemMeta()).isUnbreakable());
            case "displayName": return new StringValue(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
            case "lore": {
                List<String> lore = Objects.requireNonNull(itemStack.getItemMeta()).getLore();

                if(lore == null)
                    return Interpreter.nullValue;

                ListValue loreList = new ListValue();

                for(String item : lore)
                    loreList.getValues().add(new StringValue(item));

                return loreList;
            }

            case "setId": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("item->setId", true, line, args, "string");
                    StringValue itemId = (StringValue)args[0];
                    Material material = Material.matchMaterial(itemId.getValue());
                    if(material == null) material = Material.AIR;
                    itemStack.setType(material);
                    return itemValue;
                }
            };

            case "setAmount": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("item->setAmount", true, line, args, "number");
                    NumberValue amount = (NumberValue) args[0];

                    if(amount.getValue() % 1 != 0)
                        throw interpreter.error(line, "Amount must be an integer");

                    int value = (int)amount.getValue();
                    itemStack.setAmount(value);
                    return itemValue;
                }
            };

            case "setUnbreakable": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("item->setUnbreakable", true, line, args, "boolean");
                    BooleanValue unbreakable = (BooleanValue)args[0];
                    ItemMeta meta = itemStack.getItemMeta();
                    assert meta != null;
                    meta.setUnbreakable(unbreakable.getValue());
                    itemStack.setItemMeta(meta);
                    return itemValue;
                }
            };

            case "setDisplayName": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("item->setDisplayName", true, line, args, "string");
                    StringValue name = (StringValue)args[0];
                    ItemMeta meta = itemStack.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(name.getValue());
                    itemStack.setItemMeta(meta);
                    return itemValue;
                }
            };

            case "setLore": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("item->setLore", true, line, args, "list");
                    ListValue lore = (ListValue)args[0];
                    ArrayList<String> loreList = new ArrayList<>();

                    for(Value value : lore.getValues())
                        loreList.add(value.toStringValue());

                    ItemMeta meta = itemStack.getItemMeta();
                    assert meta != null;
                    meta.setLore(loreList);
                    itemStack.setItemMeta(meta);
                    return itemValue;
                }
            };
        }

        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        return null;
    }

    public ItemStack getBukkitItem() {
        return itemStack;
    }
}
