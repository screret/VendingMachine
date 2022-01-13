package screret.vendingmachine.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class ControlCardCapability {

    @CapabilityInject(IController.class)
    public static Capability<IController> VENDING_CONTROL_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IController.class, new Capability.IStorage<IController>()
        {
            @Override
            public INBT writeNBT(Capability<IController> capability, IController instance, Direction side)
            {
                ListNBT nbtTagList = new ListNBT();
                int size = instance.getMachines();
                CompoundNBT ownerTag = new CompoundNBT();

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
                    CompoundNBT tag = new CompoundNBT();
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
        }, Controller::new);
    }
}
