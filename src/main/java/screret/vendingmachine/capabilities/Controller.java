package screret.vendingmachine.capabilities;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class Controller implements IController {
    private final List<BlockPos> machines = Lists.newArrayList();
    private UUID owner;

    public Controller(UUID owner){
        this.owner = owner;
    }

    @Override
    public UUID getOwner(){
        return owner;
    }

    @Override
    public void setOwner(UUID owner){
        this.owner = owner;
    }

    @Override
    public int getMachines() {
        return machines != null ? machines.size() : 0;
    }

    @Nonnull
    @Override
    public BlockPos getMachine(int index) {
        return machines.get(index);
    }

    public BlockPos getMachine(BlockPos pos) {
        BlockPos machinePos;
        for (int i = 0; i < machines.size(); ++i){
            if(machines.get(i) == pos){
                return pos;
            }
        }
        throw new StackOverflowError("there isn't a machine at that position.");
    }

    public boolean hasMachine(BlockPos pos) {
        for (int i = 0; i < machines.size(); ++i){
            if(machines.get(i) == pos){
                return true;
            }
        }
        return false;
    }

    @Override
    public void addMachine(BlockPos pos) {
        machines.add(pos);
    }

    @Nonnull
    @Override
    public BlockPos removeMachine(int index) {
        return machines.remove(index);
    }

    @Override
    public void setMachineInIndex(int index, BlockPos pos){
        machines.set(index, pos);
    }

    @Override
    public int getMachineLimit(int index) {
        return 10;
    }

    @Override
    public boolean isMachineValid(int index) {
        return false;
    }

    @Override
    public Tag writeNBT(Capability<IController> capability, IController instance, Direction side)
    {
        ListTag nbtTagList = new ListTag();
        int size = instance.getMachines();
        CompoundTag ownerTag = new CompoundTag();

        Controller controller = (Controller)instance;
        if(controller.getOwner() != null){
            ownerTag.putUUID("Owner", controller.getOwner());
            nbtTagList.add(ownerTag);
        }else{
            nbtTagList.add(ownerTag);
        }
        for (int i = 0; i < size; i++)
        {
            BlockPos pos = instance.getMachine(i);
            CompoundTag tag = new CompoundTag();
            tag.putInt("Index", i);
            tag.putInt("PosX", pos.getX());
            tag.putInt("PosY", pos.getY());
            tag.putInt("PosZ", pos.getZ());
            nbtTagList.add(tag);
        }
        return nbtTagList;
    }

    @Override
    public void readNBT(Capability<IController> capability, IController instance, Direction side, Tag nbt) {
        if (instance == null){
            throw new RuntimeException("Controller instance is null");
        }
        Controller controller = (Controller) instance;
        ListTag tagList = (ListTag) nbt;

        CompoundTag ownerTag = tagList.getCompound(0);
        if(ownerTag.hasUUID("Owner")){
            controller.setOwner(ownerTag.getUUID("Owner"));
        }
        for (int i = 1; i < tagList.size(); i++)
        {
            CompoundTag pos = tagList.getCompound(i);
            int index = pos.getInt("Index");
            int x = pos.getInt("PosX");
            int y = pos.getInt("PosY");
            int z = pos.getInt("PosZ");

            if (index >= 0 && index < instance.getMachines())
            {
                controller.setMachineInIndex(index, new BlockPos(x, y, z));
            }
        }
    }
}
