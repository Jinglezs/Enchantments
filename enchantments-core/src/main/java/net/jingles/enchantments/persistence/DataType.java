package net.jingles.enchantments.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
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
      try {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(complex);

        // Serialize that array
        dataOutput.close();
        return outputStream.toByteArray();

      } catch (Exception e) {
        throw new IllegalStateException("Unable to save homes.", e);
      }
    }

    @NotNull
    @Override
    public UUID fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
      try {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(primitive);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        UUID id = (UUID) dataInput.readObject();
        dataInput.close();
        return id;

      } catch (ClassNotFoundException | IOException e) {
        throw new IllegalStateException("Unable to deserialize homes.", e);
      }
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
      try {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(complex);

        // Serialize that array
        dataOutput.close();
        return outputStream.toByteArray();

      } catch (Exception e) {
        throw new IllegalStateException("Unable to save homes.", e);
      }
    }

    @NotNull
    @Override
    public EnchantTeam fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
      try {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(primitive);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        EnchantTeam team = (EnchantTeam) dataInput.readObject();
        dataInput.close();
        return team;

      } catch (ClassNotFoundException | IOException e) {
        throw new IllegalStateException("Unable to deserialize homes.", e);
      }
    }
  };
  
}

