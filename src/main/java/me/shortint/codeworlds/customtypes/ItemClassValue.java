package me.shortint.codeworlds.customtypes;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ItemClassValue implements Value, ValueClass, ValueNewInstance {
    @Override
    public String getTypeName() {
        return "Item";
    }

    @Override
    public String toStringValue() {
        return "<Item class>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public boolean valueIsInstance(Value value) {
        return false;
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        return null;
    }

    @Override
    public Value newInstance(Interpreter interpreter, Value[] args, int line) {
        StringValue item_id;
        NumberValue amount;
        StringValue displayName;
        ListValue lore;
        BooleanValue unbreakable;

        if(args.length == 1) {
            interpreter.validateArguments("Item->new", true, line, args, "object");
            ObjectValue itemData = (ObjectValue)args[0];

            Value prop = itemData.getProperty("id");
            item_id = prop instanceof StringValue ? (StringValue)prop : new StringValue("air");

            prop = itemData.getProperty("amount");
            amount = prop instanceof NumberValue ? (NumberValue) prop : new NumberValue(1);

            prop = itemData.getProperty("displayName");
            displayName = prop instanceof StringValue ? (StringValue) prop : null;

            prop = itemData.getProperty("lore");
            lore = prop instanceof ListValue ? (ListValue) prop : null;

            prop = itemData.getProperty("unbreakable");
            unbreakable = prop instanceof BooleanValue ? (BooleanValue) prop : new BooleanValue(false);
        } else {
            throw interpreter.error(line, "Invalid item constructor");
        }

        Material material = Material.matchMaterial(item_id.getValue());
        if(material == null) material = Material.AIR; // should never happen
        ItemStack itemStack = new ItemStack(material, (int)amount.getValue());

        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;

        if(lore != null) {
            ArrayList<String> loreList = new ArrayList<>();

            for(Value value : lore.getValues())
                loreList.add(value.toStringValue());

            meta.setLore(loreList);
        }

        if(displayName != null)
            meta.setDisplayName(displayName.getValue());

        meta.setUnbreakable(unbreakable.getValue());
        itemStack.setItemMeta(meta);
        return new ItemValue(itemStack);
    }
}
