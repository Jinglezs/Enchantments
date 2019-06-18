package net.jingles.enchantments.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleUtil {

  public static void sphere(Location location, double radius, Particle particle, Object data) {
    int d = 3;
    for (double inc = (Math.random()*Math.PI)/5; inc < Math.PI; inc += Math.PI/d){
      for (double azi = (Math.random()*Math.PI)/d; azi < 2*Math.PI; azi += Math.PI/d){
        double[] spher = new double[2];
        spher[0] = inc;
        spher[1] = azi;
        Location e = location.clone().add(spherToVec(spher, radius));

        if (data == null) location.getWorld().spawnParticle(particle, e, 1);
        else location.getWorld().spawnParticle(particle, e, 1, 0, 0, 0, data);

      }
    }
  }

  private static Vector spherToVec(double[] spher, double radius){
    double inc = spher[0];
    double azi = spher[1];
    double x = radius*Math.sin(inc)*Math.cos(azi);
    double z = radius*Math.sin(inc)*Math.sin(azi);
    double y = radius*Math.cos(inc);
    return new Vector(x, y, z);
  }

  public static Vector bounceFromSource(Vector original, Location source) {
    Vector direction = source.toVector().subtract(original).normalize();
    double multiplier = 1 + (Math.random() * 5);

    return direction.multiply((original.dot(direction)))
        .multiply(-2).add(original).multiply(multiplier);
  }

}
