package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public class DispenseBehaviorItem implements IDispenseBehavior {

    public DispenseBehaviorItem() {}

    public final ItemStack a(ISourceBlock isourceblock, ItemStack itemstack) {
        ItemStack itemstack1 = this.b(isourceblock, itemstack);

        this.a(isourceblock);
        this.a(isourceblock, BlockDispenser.b(isourceblock.h()));
        return itemstack1;
    }

    protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        IPosition iposition = BlockDispenser.a(isourceblock);
        ItemStack itemstack1 = itemstack.a(1);

        // CraftBukkit start
        if (!a(isourceblock.k(), itemstack1, 6, enumfacing, isourceblock)) {
            itemstack.count++;
        }
        // CraftBukkit end

        return itemstack;
    }

    // CraftBukkit start - void -> boolean return, IPosition -> ISourceBlock last argument
    public static boolean a(World world, ItemStack itemstack, int i, EnumFacing enumfacing, ISourceBlock isourceblock) {
        IPosition iposition = BlockDispenser.a(isourceblock);
        // CraftBukkit end
        double d0 = iposition.getX();
        double d1 = iposition.getY();
        double d2 = iposition.getZ();
        EntityItem entityitem = new EntityItem(world, d0, d1 - 0.3D, d2, itemstack);
        double d3 = world.random.nextDouble() * 0.1D + 0.2D;

        entityitem.motX = (double) enumfacing.getAdjacentX() * d3;
        entityitem.motY = 0.20000000298023224D;
        entityitem.motZ = (double) enumfacing.getAdjacentZ() * d3;
        entityitem.motX += world.random.nextGaussian() * 0.007499999832361937D * (double) i;
        entityitem.motY += world.random.nextGaussian() * 0.007499999832361937D * (double) i;
        entityitem.motZ += world.random.nextGaussian() * 0.007499999832361937D * (double) i;
        // CraftBukkit start
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);

        if (!BlockDispenser.eventFired) {
            org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, entityitem.motX, entityitem.motY, entityitem.motZ);

            if (event.isCancelled()) {
                return false;
            }

            entityitem.setItemStack(CraftItemStack.asNMSCopy(event.getItem()));
            entityitem.motX = event.getVelocity().getX();
            entityitem.motY = event.getVelocity().getY();
            entityitem.motZ = event.getVelocity().getZ();

            if (!event.getItem().equals(craftItem)) {
                // Chain to handler for new item
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.a.get(eventStack.getItem());
                if (idispensebehavior != IDispenseBehavior.a && idispensebehavior.getClass() != DispenseBehaviorItem.class) {
                    idispensebehavior.a(isourceblock, eventStack);
                } else {
                    world.addEntity(entityitem);
                }
                return false;
            }
        }
        // CraftBukkit end
        world.addEntity(entityitem);
        return true; // CraftBukkit - return boolean
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.k().triggerEffect(1000, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
    }

    protected void a(ISourceBlock isourceblock, EnumFacing enumfacing) {
        isourceblock.k().triggerEffect(2000, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), this.a(enumfacing));
    }

    private int a(EnumFacing enumfacing) {
        return enumfacing.getAdjacentX() + 1 + (enumfacing.getAdjacentZ() + 1) * 3;
    }

    // CraftBukkit start
    protected ItemStack eventProcessing(BlockDispenseEvent event, ISourceBlock isourceblock, ItemStack nmsStack, CraftItemStack craftStack, boolean increment) {
        if (event.isCancelled()) {
            nmsStack.count += increment ? 1 : 0;
            return nmsStack;
        }

        org.bukkit.inventory.ItemStack bukkitStack = event.getItem();
        if (!bukkitStack.equals(craftStack)) {
            nmsStack.count += increment ? 1 : 0;
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(bukkitStack);
            IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.a.get(eventStack.getItem());
            if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                idispensebehavior.a(isourceblock, eventStack);
                return nmsStack;
            }
        }

        return nmsStack;
    }
    // CraftBukkit end
}
