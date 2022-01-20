package screret.vendingmachine.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IController {

    @Nonnull
    UUID getOwner();

    @Nonnull
    void setOwner(UUID owner);

    int getMachines();

    @Nonnull
    BlockPos getMachine(int index);

    void addMachine(BlockPos pos);

    @Nonnull
    BlockPos removeMachine(int index);

    void setMachineInIndex(int index, BlockPos pos);

    int getMachineLimit(int index);

    boolean isMachineValid(int index);

    void readNBT(Capability<IController> capability, IController instance, Direction side, Tag nbt);

    Tag writeNBT(Capability<IController> capability, IController instance, Direction side);
}
