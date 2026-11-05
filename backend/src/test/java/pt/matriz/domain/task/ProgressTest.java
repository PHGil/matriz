package pt.matriz.domain.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** US-07 acceptance criteria as unit tests. */
class ProgressTest {

    @Test
    void tenTasksWithFiveDoneIsFiftyPercent() {
        Progress p = new Progress(2, 3, 5);
        assertEquals(10, p.total());
        assertEquals(50, p.percentDone());
    }

    @Test
    void noTasksHasNoPercentage() {
        assertNull(new Progress(0, 0, 0).percentDone());
    }

    @Test
    void percentageIsRoundedToNearestWholeNumber() {
        assertEquals(33, new Progress(2, 0, 1).percentDone());
        assertEquals(67, new Progress(1, 0, 2).percentDone());
    }
}
