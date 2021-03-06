package screret.vendingmachine.capabilities;

import com.google.common.collect.Lists;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class Controller implements IController {
    private final List<BlockPos> machines = Lists.newArrayList();

    public Controller(){ }

    @Override
    public int getMachines() {
        return machines != null ? machines.size() : 0;
    }

    @Nonnull
    @Override
    public BlockPos getMachine(int index) {
        return machines.get(index);
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
    public boolean isMachineValid(int index) {
        return false;
    }
}
