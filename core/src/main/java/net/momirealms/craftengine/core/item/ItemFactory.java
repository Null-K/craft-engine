package net.momirealms.craftengine.core.item;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class ItemFactory<P extends Plugin, W extends ItemWrapper<I>, I> {
    protected final P plugin;

    protected ItemFactory(P plugin) {
        this.plugin = plugin;
    }

    public Item<I> wrap(I item) {
        Objects.requireNonNull(item, "item");
        return new AbstractItem<>(this, wrapInternal(item));
    }

    public abstract Object encodeJava(Key componentType, @Nullable Object component);

    protected abstract JsonElement encodeJson(Key type, Object component);

    protected abstract ItemWrapper<I> wrapInternal(I item);

    protected abstract Object getTag(ItemWrapper<I> item, Object... path);

    protected abstract void setTag(ItemWrapper<I> item, Object value, Object... path);

    protected abstract boolean hasTag(ItemWrapper<I> item, Object... path);

    protected abstract boolean removeTag(ItemWrapper<I> item, Object... path);

    protected abstract void setComponent(ItemWrapper<I> item, Key type, Object value);

    protected abstract Object getComponent(ItemWrapper<I> item, Key type);

    protected abstract boolean hasComponent(ItemWrapper<I> item, Key type);

    protected abstract void removeComponent(ItemWrapper<I> item, Key type);

    protected abstract void resetComponent(ItemWrapper<I> item, Key type);

    protected abstract void update(ItemWrapper<I> item);

    protected abstract I load(ItemWrapper<I> item);

    protected abstract I getItem(ItemWrapper<I> item);

    protected abstract I loadCopy(ItemWrapper<I> item);

    protected abstract void customModelData(ItemWrapper<I> item, Integer data);

    protected abstract Optional<Integer> customModelData(ItemWrapper<I> item);

    protected abstract void customName(ItemWrapper<I> item, String json);

    protected abstract Optional<String> customName(ItemWrapper<I> item);

    protected abstract void itemName(ItemWrapper<I> item, String json);

    protected abstract Optional<String> itemName(ItemWrapper<I> item);

    protected abstract void skull(ItemWrapper<I> item, String skullData);

    protected abstract Optional<List<String>> lore(ItemWrapper<I> item);

    protected abstract void lore(ItemWrapper<I> item, List<String> lore);

    protected abstract boolean unbreakable(ItemWrapper<I> item);

    protected abstract void unbreakable(ItemWrapper<I> item, boolean unbreakable);

    protected abstract Optional<Boolean> glint(ItemWrapper<I> item);

    protected abstract void glint(ItemWrapper<I> item, Boolean glint);

    protected abstract Optional<Integer> damage(ItemWrapper<I> item);

    protected abstract void damage(ItemWrapper<I> item, Integer damage);

    protected abstract Optional<Integer> maxDamage(ItemWrapper<I> item);

    protected abstract void maxDamage(ItemWrapper<I> item, Integer damage);

    protected abstract void enchantments(ItemWrapper<I> item, List<Enchantment> enchantments);

    protected abstract void storedEnchantments(ItemWrapper<I> item, List<Enchantment> enchantments);

    protected abstract void addEnchantment(ItemWrapper<I> item, Enchantment enchantment);

    protected abstract void addStoredEnchantment(ItemWrapper<I> item, Enchantment enchantment);

    protected abstract Optional<Enchantment> getEnchantment(ItemWrapper<I> item, Key key);

    protected abstract void itemFlags(ItemWrapper<I> item, List<String> flags);

    protected abstract Key id(ItemWrapper<I> item);

    protected abstract Optional<Key> customId(ItemWrapper<I> item);

    protected abstract Key vanillaId(ItemWrapper<I> item);

    protected abstract int maxStackSize(ItemWrapper<I> item);

    protected abstract void maxStackSize(ItemWrapper<I> item, Integer maxStackSize);

    protected abstract boolean is(ItemWrapper<I> item, Key itemTag);

    protected abstract boolean isBlockItem(ItemWrapper<I> item);

    protected abstract void repairCost(ItemWrapper<I> item, Integer data);

    protected abstract Optional<Integer> repairCost(ItemWrapper<I> item);

    protected abstract void trim(ItemWrapper<I> item, Trim trim);

    protected abstract Optional<Trim> trim(ItemWrapper<I> item);

    protected abstract ItemWrapper<I> mergeCopy(ItemWrapper<I> item1, ItemWrapper<I> item2);

    protected abstract void merge(ItemWrapper<I> item1, ItemWrapper<I> item2);
}
