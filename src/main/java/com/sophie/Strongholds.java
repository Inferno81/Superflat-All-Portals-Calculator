package com.sophie;

import com.google.firebase.database.utilities.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.sophie.UI.strongholdsVisitedLabel;

public class Strongholds {
    private static List<Double> distancesCache;

    //Store stronghold locations
    public static BPos[] strongholdPositions = {
            new BPos(-204, -1692),
            new BPos(2052, 884),
            new BPos(-1692, 1268),
            new BPos(-2316, 4324),
            new BPos(-4972, 148),
            new BPos(-3020, -4876),
            new BPos(2660, -4940),
            new BPos(5684, -172),
            new BPos(2548, 4116),
            new BPos(-7468, 2052),
            new BPos(-7084, -2668),
            new BPos(-4524, -6892),
            new BPos(420, -8780),
            new BPos(4804, -5996),
            new BPos(8052, -2204),
            new BPos(7556, 2852),
            new BPos(4692, 7140),
            new BPos(-412, 8820),
            new BPos(-5100, 6388),
            new BPos(3316, 11076),
            new BPos(-1484, 11572),
            new BPos(-6076, 10004),
            new BPos(-9292, 6436),
            new BPos(-11564, 2180),
            new BPos(-10508, -2476),
            new BPos(-8524, -6508),
            new BPos(-5692, -10412),
            new BPos(-940, -11500),
            new BPos(3828, -10892),
            new BPos(7412, -7852),
            new BPos(10356, -4316),
            new BPos(10628, 244),
            new BPos(9684, 4580),
            new BPos(7220, 8404),
            new BPos(-14268, -3900),
            new BPos(-12604, -8012),
            new BPos(-9596, -11260),
            new BPos(-5724, -13292),
            new BPos(-1484, -13836),
            new BPos(2660, -13708),
            new BPos(6980, -13036),
            new BPos(9748, -9644),
            new BPos(12932, -6748),
            new BPos(14452, -2652),
            new BPos(14516, 1716),
            new BPos(12772, 5652),
            new BPos(11092, 9652),
            new BPos(7876, 12660),
            new BPos(3492, 13268),
            new BPos(-588, 14196),
            new BPos(-4940, 13940),
            new BPos(-8780, 11796),
            new BPos(-12076, 8836),
            new BPos(-14012, 4836),
            new BPos(-13900, 452),
            new BPos(-17692, -492),
            new BPos(-17068, -4396),
            new BPos(-15804, -8172),
            new BPos(-13244, -11180),
            new BPos(-10428, -13868),
            new BPos(-7292, -16300),
            new BPos(-3388, -17068),
            new BPos(516, -18028),
            new BPos(4500, -17436),
            new BPos(7908, -15292),
            new BPos(10868, -12844),
            new BPos(13476, -10124),
            new BPos(15348, -6860),
            new BPos(17060, -3388),
            new BPos(18004, 516),
            new BPos(17460, 4516),
            new BPos(15780, 8148),
            new BPos(13300, 11236),
            new BPos(10276, 13668),
            new BPos(7012, 15668),
            new BPos(3492, 17508),
            new BPos(-476, 16836),
            new BPos(-4188, 16260),
            new BPos(-7900, 15300),
            new BPos(-10892, 12900),
            new BPos(-14268, 10740),
            new BPos(-15308, 6852),
            new BPos(-16828, 3348),
            new BPos(20484, 4724),
            new BPos(18596, 7892),
            new BPos(17588, 11412),
            new BPos(15092, 14068),
            new BPos(12644, 16772),
            new BPos(9044, 17732),
            new BPos(6052, 19764),
            new BPos(2500, 20324),
            new BPos(-1068, 20468),
            new BPos(-4604, 19988),
            new BPos(-7932, 18692),
            new BPos(-11132, 17156),
            new BPos(-14140, 15172),
            new BPos(-16172, 12196),
            new BPos(-17692, 9012),
            new BPos(-19820, 6068),
            new BPos(-20636, 2532),
            new BPos(-20588, -1068),
            new BPos(-19628, -4524),
            new BPos(-18716, -7948),
            new BPos(-17612, -11436),
            new BPos(-14668, -13676),
            new BPos(-11948, -15852),
            new BPos(-9164, -17980),
            new BPos(-6140, -20060),
            new BPos(-2460, -20060),
            new BPos(1076, -20364),
            new BPos(4660, -20188),
            new BPos(7956, -18748),
            new BPos(11060, -17020),
            new BPos(14116, -15132),
            new BPos(16612, -12508),
            new BPos(18708, -9532),
            new BPos(19684, -6012),
            new BPos(20340, -2492),
            new BPos(20212, 1060),
            new BPos(-21436, -11148),
            new BPos(-10572, -21196),
            new BPos(3780, -22636),
            new BPos(16964, -16684),
            new BPos(23460, -3516),
            new BPos(21252, 11060),
            new BPos(10660, 21348),
            new BPos(-3900, 23396),
            new BPos(-16620, 16356)
    };

    public static int strongholdsVisited = 0;

    public static void updateCoordsList(int strongholdIndex) {
        //Set stronghold position at index to completed
        Strongholds.strongholdPositions[strongholdIndex] = null;
        strongholdsVisited++;
        //Update strongholds visited display
        strongholdsVisitedLabel.setText(String.format("Strongholds visited: %s/128 (%.1f%%)", Strongholds.strongholdsVisited, Strongholds.strongholdsVisited / 128.0 * 100));
    }

    public static Integer getStrongholdInRadius(Pair<BPos, String> locationData) {
        BPos pos = locationData.getFirst();
        String dimension = locationData.getSecond();
        //Invalidate dimensions cache and return with no value if in the end, as all methods are useless here
        if (dimension.equals("minecraft:the_end")) {
            distancesCache = null;
            return null;
        }
        //Initialize value for the case of the player not being in a stronghold
        int strongholdInRadius = -1;
        //Initialize cache, to avoid having to reuse distance values later. Iterate through every stronghold here, even though not always required, for simplicity
        distancesCache = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            //If stronghold completed, mark as completed in cache as well
            if (strongholdPositions[i] == null) {
                distancesCache.add(Double.MAX_VALUE);
                continue;
            }
            //Accommodate for distance in both dimensions
            double distance = (dimension.equals("minecraft:overworld") ? pos : pos.toOverworldPos()).distanceTo(strongholdPositions[i]);
            distancesCache.add(distance);
            if (dimension.equals("minecraft:overworld") && strongholdInRadius == -1 && distance <= 150) {
                //Mark this stronghold as visited
                strongholdInRadius = i;
                distancesCache.set(strongholdInRadius, Double.MAX_VALUE);
            }
        }
        return strongholdInRadius;
    }

    public static List<Integer> getClosestStrongholds() {
        List<Integer> closestStrongholds = new ArrayList<>();
        //Iterate through the three strongholds with the smallest distance to the player (using distance cache) in ascending order
        for (double distance : distancesCache.stream().sorted().limit(3).toArray(Double[]::new)) {
            //If the stronghold isn't completed, mark as done (this check is so when there are less than 3 strongholds left, they don't cause display issues
            if (distance != Double.MAX_VALUE) {
                closestStrongholds.add(distancesCache.indexOf(distance));
            }
        }
        return closestStrongholds;
    }

    public static double getAngle(BPos playerPos, int strongholdIndex) {
        BPos strongholdPos = strongholdPositions[strongholdIndex];
        double rawAngle = Math.toDegrees(Math.atan2(strongholdPos.z() - playerPos.z(), strongholdPos.x() - playerPos.x())) - 90;
        if (rawAngle > 180) {
            rawAngle -= 360;
        } else if (rawAngle <= -180) {
            rawAngle += 360;
        }
        return Math.round(rawAngle * 10) / 10.0;
    }
}
