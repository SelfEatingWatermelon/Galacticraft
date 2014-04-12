package micdoodle8.mods.galacticraft.core.oxygen;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;


/* Think this BlockVec3 is confusing with galacticraft.api.vector.Vector3?
 * 
 * For speed, in 95% of cases Galacticraft code could be using integer arithmetic not doubles,
 * for block coordinates, to avoid massive unnecessary type conversion between integers and doubles.
 * (Minecraft block coordinates are always integers, only entity coordinates are doubles.)
 * 
 * Most of Galacticraft could therefore be adapted to use this BlockVec3 instead.
 * To avoid a big diff, the methods here are as similar as possible to those in Vector3.
 *  (Though really, calls like vector3.intX() ought to be replaced by vector3.x, for maximum speed)
 *  
 * Note also when writing NBT data BlockVec3 writes and reads its coordinates as doubles, for 100% file and network compatibility with prior code.
 */
public class BlockVec3 implements Cloneable
{
		public int x;
		public int y;
		public int z;

		public BlockVec3()
		{
			this(0, 0, 0);
		}

		public BlockVec3(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public BlockVec3(Entity par1)
		{
			this.x = (int) Math.floor(par1.posX);
			this.y = (int) Math.floor(par1.posY);
			this.z = (int) Math.floor(par1.posZ);
		}

		public BlockVec3(TileEntity par1)
		{
			this.x = par1.xCoord;
			this.y = par1.yCoord;
			this.z = par1.zCoord;
		}
		
		/**
		 * Makes a new copy of this Vector. Prevents variable referencing problems.
		 */
		@Override
		public BlockVec3 clone()
		{
			return new BlockVec3(this.x, this.y, this.z);
		}

		public int getBlockID(IBlockAccess world)
		{
			return world.getBlockId(this.x, this.y, this.z);
		}

		public BlockVec3 add(BlockVec3 par1)
		{
			this.x += par1.x;
			this.y += par1.y;
			this.z += par1.z;
			return this;
		}

		public BlockVec3 translate(BlockVec3 par1)
		{
			this.x += par1.x;
			this.y += par1.y;
			this.z += par1.z;
			return this;
		}

		public static BlockVec3 add(BlockVec3 par1, BlockVec3 a)
		{
			return new BlockVec3(par1.x+a.x,par1.y+a.y,par1.z+a.z);
		}
		
		public BlockVec3 subtract(BlockVec3 par1)
		{
			this.x = this.x -= par1.x;
			this.y = this.y -= par1.y;
			this.z = this.z -= par1.z;
			
			return this;
		}
		
		public BlockVec3 modifyPositionFromSide(ForgeDirection side, int amount)
		{
			BlockVec3 vec = new BlockVec3(x,y,z);
			switch (side.ordinal())
			{
				case 0:
					vec.y -= amount;
					break;
				case 1:
					vec.y += amount;
					break;
				case 2:
					vec.z -= amount;
					break;
				case 3:
					vec.z += amount;
					break;
				case 4:
					vec.x -= amount;
					break;
				case 5:
					vec.x += amount;
					break;
			}
			return vec;
		}

		public BlockVec3 modifyPositionFromSide(ForgeDirection side)
		{
			return this.modifyPositionFromSide(side, 1);
		}


		@Override
		public int hashCode()
		{
			//Note: this is now copying the same hashCode calculation as in VecDirPair
			//It's a fairly weak hashCode - for example, increasing z by 37 will have the same hashCode as increasing y by 1 
			return ((17*37+x)*37+y)*37+z;//(this.x + "Y" + this.y + "Z" + this.z + "posit").hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof BlockVec3)
			{
				BlockVec3 vector = (BlockVec3) o;
				return this.x == vector.x && this.y == vector.y && this.z == vector.z;
			}

			return false;
		}

		@Override
		public String toString()
		{
			return "BlockVec3 [" + this.x + "," + this.y + "," + this.z + "]";
		}
		
		public TileEntity getTileEntity(IBlockAccess world)
		{
			return world.getBlockTileEntity(this.x, this.y, this.z);
		}

		public int getBlockMetadata(IBlockAccess world)
		{
			return world.getBlockMetadata(this.x, this.y, this.z);
		}


		
		public static BlockVec3 readFromNBT(NBTTagCompound nbtCompound)
		{
			BlockVec3 tempVector = new BlockVec3();
			tempVector.x = (int) Math.floor(nbtCompound.getDouble("x"));
			tempVector.y = (int) Math.floor(nbtCompound.getDouble("y"));
			tempVector.z = (int) Math.floor(nbtCompound.getDouble("z"));
			return tempVector;
		}

		public int distanceTo(BlockVec3 vector)
		{
			int var2 = vector.x - this.x;
			int var4 = vector.y - this.y;
			int var6 = vector.z - this.z;
			return (int) Math.floor(Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6));		}
		

		public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
		{
			par1NBTTagCompound.setDouble("x", (double) this.x);
			par1NBTTagCompound.setDouble("y", (double) this.y);
			par1NBTTagCompound.setDouble("z", (double) this.z);
			return par1NBTTagCompound;
		}

		public double getMagnitude()
		{
			return Math.sqrt((double) this.getMagnitudeSquared());
		}

		public int getMagnitudeSquared()
		{
			return x * x + y * y + z * z;
		}

		public void setBlock(World worldObj, int blockID)
		{
			worldObj.setBlock(x,y,z,blockID,0,3);
		}
		
		public int intX()
		{
			return x;
		}

		public int intY()
		{
			return x;
		}

		public int intZ()
		{
			return x;
		}
}