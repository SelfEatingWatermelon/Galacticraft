package micdoodle8.mods.galacticraft.core.tile;

import io.netty.buffer.ByteBuf;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.IPacketReceiver;
import micdoodle8.mods.galacticraft.core.network.NetworkUtil;
import micdoodle8.mods.galacticraft.core.network.PacketDynamic;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.GCLog;
import micdoodle8.mods.miccore.Annotations.NetworkedField;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.*;

public abstract class TileEntityAdvanced extends TileEntity implements IPacketReceiver, ITickable
{
    public long ticks = 0;
    private LinkedHashSet<Field> fieldCacheClient;
    private LinkedHashSet<Field> fieldCacheServer;
    private Map<Field, Object> lastSentData = new HashMap<Field, Object>();
    private boolean networkDataChanged = false;

    @Override
    public void update()
    {
        if (this.ticks == 0)
        {
            this.initiate();
        }

        //Who coded this?  A server would need to run in excess of 14 billion years to reach this value
//        if (this.ticks >= Long.MAX_VALUE)
//        {
//            this.ticks = 1;
//        }

        this.ticks++;

        if (this.isNetworkedTile() && this.ticks % this.getPacketCooldown() == 0)
        {
            if (this.fieldCacheClient == null || this.fieldCacheServer == null)
            {
                try
                {
                    this.initFieldCache();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (this.worldObj.isRemote && this.fieldCacheServer.size() > 0)
            {
                PacketDynamic packet = new PacketDynamic(this);
//                if (networkDataChanged)
                {
                    GalacticraftCore.packetPipeline.sendToServer(packet);
                }
            }
            else if (!this.worldObj.isRemote && this.fieldCacheClient.size() > 0)
            {
                PacketDynamic packet = new PacketDynamic(this);
//                if (networkDataChanged)
                {
                    GalacticraftCore.packetPipeline.sendToAllAround(packet, new TargetPoint(GCCoreUtil.getDimensionID(this.worldObj), getPos().getX(), getPos().getY(), getPos().getZ(), this.getPacketRange()));
                }
            }
        }
    }

    private void initFieldCache() throws IllegalArgumentException, IllegalAccessException
    {
        this.fieldCacheClient = new LinkedHashSet<Field>();
        this.fieldCacheServer = new LinkedHashSet<Field>();

        for (Field field : this.getClass().getFields())
        {
            if (field.isAnnotationPresent(NetworkedField.class))
            {
                NetworkedField f = field.getAnnotation(NetworkedField.class);

                if (f.targetSide() == Side.CLIENT)
                {
                    this.fieldCacheClient.add(field);
                }
                else
                {
                    this.fieldCacheServer.add(field);
                }
            }
        }
    }

    public abstract double getPacketRange();

    public abstract int getPacketCooldown();

    public abstract boolean isNetworkedTile();

    public void addExtraNetworkedData(List<Object> networkedList)
    {
    }

    public void readExtraNetworkedData(ByteBuf dataStream)
    {
    }

    public void initiate()
    {
    }

    @Override
    public void getNetworkedData(ArrayList<Object> sendData)
    {
        Set<Field> fieldList = null;
        boolean changed = false;

        if (this.fieldCacheClient == null || this.fieldCacheServer == null)
        {
            try
            {
                this.initFieldCache();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (this.worldObj.isRemote)
        {
            fieldList = this.fieldCacheServer;
        }
        else
        {
            fieldList = this.fieldCacheClient;
        }

        for (Field f : fieldList)
        {
            boolean fieldChanged = false;
            try
            {
                Object data = f.get(this);
                Object lastData = lastSentData.get(f);

                if (!NetworkUtil.fuzzyEquals(lastData, data))
                {
                    fieldChanged = true;
                }

                sendData.add(data);

                if (fieldChanged)
                {
                    lastSentData.put(f, NetworkUtil.cloneNetworkedObject(data));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            changed |= fieldChanged;
        }

        if (changed)
        {
            this.addExtraNetworkedData(sendData);
        }
        else
        {
            ArrayList<Object> prevSendData = new ArrayList<Object>(sendData);

            this.addExtraNetworkedData(sendData);

            if (!prevSendData.equals(sendData))
            {
                changed = true;
            }
        }

        networkDataChanged = changed;
    }

    @Override
    public void decodePacketdata(ByteBuf buffer)
    {
        if (this.worldObj == null)
        {
            GCLog.severe("World is NULL! Connot decode packet data!");
            return;
        }

        if (this.fieldCacheClient == null || this.fieldCacheServer == null)
        {
            try
            {
                this.initFieldCache();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

//        if (this.worldObj.isRemote && this.fieldCacheClient.size() == 0)
//        {
//            return;
//        }
//        else if (!this.worldObj.isRemote && this.fieldCacheServer.size() == 0)
//        {
//            return;
//        }

        Set<Field> fieldSet = null;

        if (this.worldObj.isRemote)
        {
            fieldSet = this.fieldCacheClient;
        }
        else
        {
            fieldSet = this.fieldCacheServer;
        }

        for (Field field : fieldSet)
        {
            try
            {
                Object obj = NetworkUtil.getFieldValueFromStream(field, buffer, this.worldObj);
                field.set(this, obj);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        this.readExtraNetworkedData(buffer);
    }
}