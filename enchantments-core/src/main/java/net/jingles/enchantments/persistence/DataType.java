package net.jingles.enchantments.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public interface DataType {

  PersistentDataType<byte[], UUID> UUID = new PersistentDataType<byte[], java.util.UUID>() {
    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
      return byte[].class;
    }

    @NotNull
    @Override
    public Class<UUID> getComplexType() {
      return UUID.class;
    }

    @NotNull
    @Override
    public byte[] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
      return toByteArray(complex);
    }

    @NotNull
    @Override
    public UUID fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
      return fromByteArray(primitive, UUID.class);
    }
  };

  PersistentDataType<byte[], EnchantTeam> ENCHANT_TEAM = new PersistentDataType<byte[], EnchantTeam>() {

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
      return byte[].class;
    }

    @NotNull
    @Override
    public Class<EnchantTeam> getComplexType() {
      return EnchantTeam.class;
    }

    @NotNull
    @Override
    public byte[] toPrimitive(@NotNull EnchantTeam complex, @NotNull PersistentDataAdapterContext context) {
      return toByteArray(complex);
    }

    @NotNull
    @Override
    public EnchantTeam fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
      return fromByteArray(primitive, EnchantTeam.class);
    }
  };

  PersistentDataType<byte[], PotionData> POTION_DATA = new PersistentDataType<byte[], PotionData>() {

    @Override
    public Class<byte[]> getPrimitiveType() {
      return byte[].class;
    }

    @Override
    public Class<PotionData> getComplexType() {
      return PotionData.class;
    }

    @Override
    public byte[] toPrimitive(PotionData complex, PersistentDataAdapterContext context) {
      return toByteArray(complex);
    }

    @Override
    public PotionData fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
      return fromByteArray(primitive, PotionData.class);
    }

  };

  static byte[] toByteArray(Object object) {

    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

      dataOutput.writeObject(object);

      // Serialize that array
      dataOutput.close();
      return outputStream.toByteArray();

    } catch (Exception e) {
      throw new IllegalStateException("Unable to save the UUID.", e);
    }

  }

  static <T> T fromByteArray(byte[] primitive, Class<T> type) {
    try {

      ByteArrayInputStream inputStream = new ByteArrayInputStream(primitive);
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

      T object = (T) dataInput.readObject();
      dataInput.close();
      return object;

    } catch (ClassNotFoundException | IOException e) {
      throw new IllegalStateException("Unable to deserialize the UUID.", e);
    }
  }
  
}

