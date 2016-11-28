package io.smartcat.data.loader.rules;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.smartcat.data.loader.RangeUtil;
import io.smartcat.data.loader.util.Randomizer;

/**
 * Rule for creating random range values.
 */
public class RangeRuleDate implements Rule<Date> {

    private boolean exclusive;

    // definition of the range: e.g [a,b,c,d] : a < b <= c < d is a set of ranges: {[a,b),[c,d)}
    private List<Date> ranges = new ArrayList<>();

    private Randomizer random;

    private RangeRuleDate() {
    };

    public RangeRuleDate withRandom(Randomizer random) {
        this.random = random;
        return this;
    }

    public static RangeRuleDate withRanges(Date... rangeMarkers) {
        RangeRuleDate result = new RangeRuleDate();

        result.ranges.addAll(Arrays.asList(rangeMarkers));

        return result;
    }

    public static RangeRuleDate withRangesX(Date... rangeMarkers) {
        RangeRuleDate result = new RangeRuleDate();

        result.exclusive = true;
        result.ranges.addAll(Arrays.asList(rangeMarkers));

        return result;
    }

    public static RangeRuleDate withRanges(List<Date> rangeMarkers) {
        RangeRuleDate result = new RangeRuleDate();

        result.ranges.addAll(rangeMarkers);

        return result;
    }

    @Override
    public boolean isExclusive() {
        return this.exclusive;
    }

    @Override
    public Rule<Date> recalculatePrecedance(Rule<?> exclusiveRule) {
        if (!exclusiveRule.isExclusive()) {
            throw new IllegalArgumentException("no need to calculate rule precedance with non exclusive rule");
        }
        if (!(exclusiveRule instanceof RangeRuleDate)) {
            throw new IllegalArgumentException("cannot compare discrete and range rules");
        }
        RangeRuleDate otherRule = (RangeRuleDate) exclusiveRule;

        if (!RangeUtil.rangesIntersects(this.ranges, otherRule.getAllowedRanges())) {
            return this;
        }
        List<Date> newRanges = RangeUtil.recalculateRanges(this.ranges, otherRule.getAllowedRanges());

        return RangeRuleDate.withRanges(newRanges).withRandom(random);
    }

    private List<Date> getAllowedRanges() {
        return ranges;
    }

    @Override
    public Date getRandomAllowedValue() {
        // ranges = [a,b,c,d]
        // =>
        // (a,b],(c,d]
        // 0 , 1
        int randomRangeIndex = 0;
        if (ranges.size() > 2) {
            randomRangeIndex = random.nextInt(ranges.size() / 2);
        }
        Long randomValue = random.nextLong(ranges.get(randomRangeIndex * 2).getTime(),
                ranges.get((randomRangeIndex * 2) + 1).getTime());
        Instant radnomInstant = Instant.ofEpochMilli(randomValue);

        return Date.from(radnomInstant);
    }

}