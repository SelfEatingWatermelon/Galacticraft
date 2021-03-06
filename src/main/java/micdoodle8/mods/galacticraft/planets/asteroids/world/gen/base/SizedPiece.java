package micdoodle8.mods.galacticraft.planets.asteroids.world.gen.base;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public abstract class SizedPiece extends Piece
{
    protected EnumFacing direction;
    protected int sizeX;
    protected int sizeY;
    protected int sizeZ;

    public SizedPiece()
    {
    }

    public SizedPiece(BaseConfiguration configuration, int sizeX, int sizeY, int sizeZ, EnumFacing direction)
    {
        super(configuration);
        this.direction = direction;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public EnumFacing getDirection()
    {
        return direction;
    }

    public void setDirection(EnumFacing direction)
    {
        this.direction = direction;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound)
    {
        super.writeStructureToNBT(tagCompound);

        tagCompound.setInteger("direction", this.direction.ordinal());
        tagCompound.setInteger("sizeX", this.sizeX);
        tagCompound.setInteger("sizeY", this.sizeY);
        tagCompound.setInteger("sizeZ", this.sizeZ);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound)
    {
        super.readStructureFromNBT(tagCompound);

        this.sizeX = tagCompound.getInteger("sizeX");
        this.sizeY = tagCompound.getInteger("sizeY");
        this.sizeZ = tagCompound.getInteger("sizeZ");

        if (tagCompound.hasKey("direction"))
        {
            this.direction = EnumFacing.getFront(tagCompound.getInteger("direction"));
        }
        else
        {
            this.direction = EnumFacing.NORTH;
        }
    }

    public int getSizeX()
    {
        return sizeX;
    }

    public int getSizeY()
    {
        return sizeY;
    }

    public int getSizeZ()
    {
        return sizeZ;
    }

    @Override
    protected int getXWithOffset(int x, int z)
    {
        if (this.coordBaseMode == null)
        {
            return x;
        }
        else
        {
            switch (this.coordBaseMode)
            {
                case NORTH:
                    return this.boundingBox.minX + x;
                case SOUTH:
                    return this.boundingBox.maxX - x;
                case WEST:
                    return this.boundingBox.maxX - z;
                case EAST:
                    return this.boundingBox.minX + z;
                default:
                    return x;
            }
        }
    }

    @Override
    protected int getZWithOffset(int x, int z)
    {
        if (this.coordBaseMode == null)
        {
            return z;
        }
        else
        {
            switch (this.coordBaseMode)
            {
                case NORTH:
                    return this.boundingBox.minZ + z;
                case SOUTH:
                    return this.boundingBox.maxZ - z;
                case WEST:
                    return this.boundingBox.minZ + x;
                case EAST:
                    return this.boundingBox.maxZ - x;
                default:
                    return z;
            }
        }
    }

    public void setBlockStateDirectional(World worldIn, IBlockState blockState, int xx, int yy, int zz)
    {
        this.coordBaseMode = this.direction;
//        int xtrue = xx;
//        int ztrue = zz;
//        if (getDirection() == EnumFacing.SOUTH)
//        {
//            xtrue = this.sizeX - xx;
//            ztrue = this.sizeZ - zz;
//        }
//        else if (getDirection() == EnumFacing.WEST)
//        {
//            xtrue = this.sizeX - zz;
//            ztrue = xx;
//        }
//        else if (getDirection() == EnumFacing.EAST)
//        {
//            xtrue = zz;
//            ztrue = this.sizeZ - xx;
//        }
        this.setBlockState(worldIn, blockState, xx, yy, zz, boundingBox);
        this.coordBaseMode = EnumFacing.SOUTH;
    }

    //Unused currently
    public Piece getDoorway(Random rand, BaseStart startPiece, int maxAttempts, boolean small)
    {
        EnumFacing randomDir;
        int blockX;
        int blockZ;
        int sizeX;
        int sizeZ;
        boolean valid;
        int attempts = maxAttempts;
        do
        {
            int randDir = rand.nextInt(4);
            randomDir = EnumFacing.getHorizontal((randDir == getDirection().getOpposite().getHorizontalIndex() ? randDir + 1 : randDir) % 4);
            StructureBoundingBox extension = getExtension(randomDir, 1, 3);
            blockX = extension.minX;
            blockZ = extension.minZ;
            sizeX = extension.maxX - extension.minX;
            sizeZ = extension.maxZ - extension.minZ;
            valid = !startPiece.checkIntersection(extension);
            attempts--;
        }
        while (!valid && attempts > 0);

        if (!valid)
        {
            return null;
        }

        return new BaseLinking(this.configuration, rand, blockX, blockZ, sizeX, 3, sizeZ, randomDir);
    }
}
