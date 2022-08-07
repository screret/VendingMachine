package screret.vendingmachine.capabilities;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface IController {

    int getMachines();

    BlockPos getMachine(int index);

    void addMachine(BlockPos pos);

    BlockPos removeMachine(int index);

    void setMachineInIndex(int index, BlockPos pos);

    boolean isMachineValid(int index);
}
